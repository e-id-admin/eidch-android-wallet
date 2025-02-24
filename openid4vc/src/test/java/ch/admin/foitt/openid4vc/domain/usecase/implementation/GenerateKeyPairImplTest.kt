package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.ProofType
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.ProofTypeSigningAlgorithms
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.usecase.CreateJWSKeyPair
import ch.admin.foitt.openid4vc.domain.usecase.GenerateKeyPair
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockCredentialOffer.validKeyPairES256
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockCredentialOffer.validKeyPairES512
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockIssuerCredentialConfiguration.credentialConfigurationWithOtherProofTypeSigningAlgorithms
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockIssuerCredentialConfiguration.credentialConfigurationWithoutProofTypesSupported
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockIssuerCredentialConfiguration.vcSdJwtCredentialConfiguration
import ch.admin.foitt.openid4vc.util.assertErrorType
import ch.admin.foitt.openid4vc.util.assertOk
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.spyk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class GenerateKeyPairImplTest {

    @MockK
    private lateinit var mockCreateJWSKeyPair: CreateJWSKeyPair

    private lateinit var spyGenerateKeyPair: GenerateKeyPair

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        val generateKeyPair = GenerateKeyPairImpl(mockCreateJWSKeyPair)
        spyGenerateKeyPair = spyk(generateKeyPair)

        every { spyGenerateKeyPair["getPreferredSigningAlgorithms"]() } returns listOf(SigningAlgorithm.ES256)
        coEvery { mockCreateJWSKeyPair(SigningAlgorithm.ES256, any()) } returns Ok(validKeyPairES256)
        coEvery { mockCreateJWSKeyPair(SigningAlgorithm.ES512, any()) } returns Ok(validKeyPairES512)
    }

    @Test
    fun `valid supported credential returns key pair and binding method`() = runTest {
        spyGenerateKeyPair(vcSdJwtCredentialConfiguration).assertOk()

        coVerify(exactly = 1) {
            mockCreateJWSKeyPair(SigningAlgorithm.ES256, any())
        }
    }

    @ParameterizedTest
    @MethodSource("generateSigningAlgorithmInputs")
    fun `Supported proof type with multiple algorithms returns a key pair using the first matching algorithm of the preference list`(
        issuerAlgorithms: List<SigningAlgorithm>,
        appAlgorithms: List<SigningAlgorithm>,
        expected: SigningAlgorithm,
    ) = runTest {
        every {
            spyGenerateKeyPair["getPreferredSigningAlgorithms"]()
        } returns appAlgorithms

        val credentialConfiguration = vcSdJwtCredentialConfiguration.copy(
            proofTypesSupported = mapOf(ProofType.JWT to ProofTypeSigningAlgorithms(issuerAlgorithms))
        )

        val result = spyGenerateKeyPair(credentialConfiguration).assertOk()

        assertEquals(expected, result.algorithm)

        coVerify(exactly = 1) {
            mockCreateJWSKeyPair(expected, any())
        }
    }

    @Test
    fun `Supported proof type with supported algorithm that is not in the preference list returns an unsupported cryptographic suite error`() = runTest {
        // issuer: proofType: JWT, algorithms: [ES256],
        // app: [ES512]
        // -> error
        every {
            spyGenerateKeyPair["getPreferredSigningAlgorithms"]()
        } returns listOf(SigningAlgorithm.ES512)

        spyGenerateKeyPair(vcSdJwtCredentialConfiguration).assertErrorType(CredentialOfferError.UnsupportedCryptographicSuite::class)

        coVerify(exactly = 0) {
            mockCreateJWSKeyPair(SigningAlgorithm.ES256, any())
        }
    }

    @Test
    fun `No provided proof types returns an unsupported cryptographic suite error`() = runTest {
        // issuer: -
        // app: [ES256]
        // -> error
        spyGenerateKeyPair(
            credentialConfigurationWithoutProofTypesSupported
        ).assertErrorType(CredentialOfferError.UnsupportedCryptographicSuite::class)

        coVerify(exactly = 0) {
            mockCreateJWSKeyPair(any(), any())
        }
    }

    @Test
    fun `No supported proof types returns an unsupported cryptographic suite error`() = runTest {
        // issuer: proofType: UnsupportedType, algorithms: [ES256],
        // app: [ES256]
        // -> error
        spyGenerateKeyPair(
            credentialConfigurationWithOtherProofTypeSigningAlgorithms
        ).assertErrorType(CredentialOfferError.UnsupportedCryptographicSuite::class)

        coVerify(exactly = 0) {
            mockCreateJWSKeyPair(any(), any())
        }
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    private companion object {
        @JvmStatic
        fun generateSigningAlgorithmInputs(): Stream<Arguments> = Stream.of(
            // Argument(issuer algorithms, app algorithms, expected result)
            Arguments.of(
                listOf(SigningAlgorithm.ES256, SigningAlgorithm.ES512),
                listOf(SigningAlgorithm.ES512, SigningAlgorithm.ES256),
                SigningAlgorithm.ES512
            ),
            Arguments.of(
                listOf(SigningAlgorithm.ES512, SigningAlgorithm.ES256),
                listOf(SigningAlgorithm.ES256, SigningAlgorithm.ES512),
                SigningAlgorithm.ES256
            ),
            Arguments.of(
                listOf(SigningAlgorithm.ES256, SigningAlgorithm.ES512),
                listOf(SigningAlgorithm.ES256),
                SigningAlgorithm.ES256
            ),
            Arguments.of(
                listOf(SigningAlgorithm.ES256, SigningAlgorithm.ES512),
                listOf(SigningAlgorithm.ES512),
                SigningAlgorithm.ES512
            )
        )
    }
}

package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.usecase.CreateJWSKeyPair
import ch.admin.foitt.openid4vc.domain.usecase.GenerateKeyPair
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockCredentialOffer.validKeyPair
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockIssuerCredentialConfiguration.credentialConfigurationWithMultipleCryptographicSuites
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockIssuerCredentialConfiguration.credentialConfigurationWithoutProofTypesSupported
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockIssuerCredentialConfiguration.vcSdJwtCredentialConfiguration
import ch.admin.foitt.openid4vc.util.assertErrorType
import ch.admin.foitt.openid4vc.util.assertOk
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GenerateKeyPairImplTest {

    @MockK
    private lateinit var mockCreateJWSKeyPair: CreateJWSKeyPair

    private lateinit var generateKeyPair: GenerateKeyPair

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        generateKeyPair = GenerateKeyPairImpl(mockCreateJWSKeyPair)

        coEvery { mockCreateJWSKeyPair(any(), any()) } returns Ok(validKeyPair)
    }

    @Test
    fun `valid supported credential returns key pair and binding method`() = runTest {
        generateKeyPair(vcSdJwtCredentialConfiguration).assertOk()

        coVerify(exactly = 1) {
            mockCreateJWSKeyPair(any(), any())
        }
    }

    @Test
    fun `use first cryptographic suite when multiple are available`() = runTest {
        val result = generateKeyPair(credentialConfigurationWithMultipleCryptographicSuites).assertOk()

        assertEquals(SigningAlgorithm.ES512, result.algorithm)

        coVerify(exactly = 1) {
            mockCreateJWSKeyPair(any(), any())
        }
    }

    @Test
    fun `no supported proof types  returns an unsupported cryptographic suite error`() = runTest {
        generateKeyPair(
            credentialConfigurationWithoutProofTypesSupported
        ).assertErrorType(CredentialOfferError.UnsupportedCryptographicSuite::class)

        coVerify(exactly = 0) {
            mockCreateJWSKeyPair(any(), any())
        }
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }
}

package ch.admin.foitt.openid4vc.domain.usecase.implementation

import android.annotation.SuppressLint
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.repository.CredentialOfferRepository
import ch.admin.foitt.openid4vc.domain.usecase.CreateCredentialRequestProofJwt
import ch.admin.foitt.openid4vc.domain.usecase.CreateDidJwk
import ch.admin.foitt.openid4vc.domain.usecase.DeleteKeyPair
import ch.admin.foitt.openid4vc.domain.usecase.GenerateKeyPair
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockCredentialOffer.jwtProof
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockCredentialOffer.offerWithPreAuthorizedCode
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockCredentialOffer.offerWithoutMatchingCredentialIdentifier
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockCredentialOffer.offerWithoutPreAuthorizedCode
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockCredentialOffer.validCredentialResponse
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockCredentialOffer.validIssuerConfig
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockCredentialOffer.validIssuerCredentialInformation
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockCredentialOffer.validTokenResponse
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockCredentialOffer.validVerifiableCredential
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockIssuerCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockIssuerCredentialConfiguration.credentialConfigurationWithOtherProofTypeSigningAlgorithms
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockIssuerCredentialConfiguration.credentialConfigurationWithoutProofTypesSupported
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockIssuerCredentialConfiguration.vcSdJwtCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockKeyPairs.VALID_KEY_PAIR
import ch.admin.foitt.openid4vc.util.assertErrorType
import ch.admin.foitt.openid4vc.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.Ordering
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll

class FetchVerifiableCredentialImplTest {

    @MockK
    private lateinit var mockCredentialOfferRepository: CredentialOfferRepository

    @MockK
    private lateinit var mockGenerateKeyPair: GenerateKeyPair

    @MockK
    private lateinit var mockCreateDidJwk: CreateDidJwk

    @MockK
    private lateinit var mockCreateCredentialRequestProofJwt: CreateCredentialRequestProofJwt

    @MockK
    private lateinit var mockDeleteKeyPair: DeleteKeyPair

    private lateinit var fetchCredentialUseCase: FetchVerifiableCredentialImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        success()

        fetchCredentialUseCase = FetchVerifiableCredentialImpl(
            mockCredentialOfferRepository,
            mockGenerateKeyPair,
            mockCreateDidJwk,
            mockCreateCredentialRequestProofJwt,
            mockDeleteKeyPair,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @SuppressLint("CheckResult")
    @Test
    fun `valid credential offer returns a verifiable credential`() = runTest {
        val credential = fetchCredentialUseCase(
            credentialConfiguration = vcSdJwtCredentialConfiguration,
            credentialOffer = offerWithPreAuthorizedCode,
        ).assertOk()

        assertEquals(validVerifiableCredential, credential)

        coVerify(ordering = Ordering.SEQUENCE) {
            mockCredentialOfferRepository.fetchIssuerConfiguration(any(), any())
            mockCredentialOfferRepository.fetchIssuerCredentialInformation(any(), any())
            mockGenerateKeyPair(any())
            mockCredentialOfferRepository.fetchAccessToken(any(), any())
            mockCreateDidJwk(any(), any(), any())
            mockCreateCredentialRequestProofJwt(any(), any(), any(), any())
            mockCredentialOfferRepository.fetchCredential(any(), any(), any(), any())
        }

        coVerify(exactly = 0) {
            mockDeleteKeyPair(any())
        }
    }

    @SuppressLint("CheckResult")
    @Test
    fun `credential offer with non-matching issuer credential identifier should return an invalid credential offer error, access token not fetched`() = runTest {
        fetchCredentialUseCase(
            credentialConfiguration = vcSdJwtCredentialConfiguration,
            credentialOffer = offerWithoutMatchingCredentialIdentifier,
        ).assertErrorType(CredentialOfferError.InvalidCredentialOffer::class)

        coVerify(exactly = 0) {
            mockCredentialOfferRepository.fetchAccessToken(any(), any())
        }
    }

    @SuppressLint("CheckResult")
    @Test
    fun `when the proof type is not supported return an unsupported proof type error, access token not fetched`() = runTest {
        fetchCredentialUseCase(
            credentialConfiguration = credentialConfigurationWithOtherProofTypeSigningAlgorithms,
            credentialOffer = offerWithPreAuthorizedCode,
        ).assertErrorType(CredentialOfferError.UnsupportedProofType::class)

        coVerify(exactly = 0) {
            mockCredentialOfferRepository.fetchAccessToken(any(), any())
        }
    }

    @SuppressLint("CheckResult")
    @Test
    fun `when generating the key pair fails return an unsupported cryptographic suite error, access token not fetched`() = runTest {
        coEvery {
            mockGenerateKeyPair(any())
        } returns Err(CredentialOfferError.UnsupportedCryptographicSuite)

        fetchCredentialUseCase(
            credentialConfiguration = vcSdJwtCredentialConfiguration,
            credentialOffer = offerWithPreAuthorizedCode,
        ).assertErrorType(CredentialOfferError.UnsupportedCryptographicSuite::class)

        coVerify(exactly = 0) {
            mockCredentialOfferRepository.fetchAccessToken(any(), any())
        }
    }

    @SuppressLint("CheckResult")
    @Test
    fun `when creating the did jwk fails return an unsupported cryptographic suite error and delete the key pair, token not fetched`() = runTest {
        coEvery {
            mockCreateDidJwk(any(), any(), false)
        } returns Err(CredentialOfferError.UnsupportedCryptographicSuite)

        fetchCredentialUseCase(
            credentialConfiguration = vcSdJwtCredentialConfiguration,
            credentialOffer = offerWithPreAuthorizedCode,
        ).assertErrorType(CredentialOfferError.UnsupportedCryptographicSuite::class)

        coVerify {
            mockDeleteKeyPair(any())
        }

        coVerify(exactly = 0) {
            mockCreateCredentialRequestProofJwt(any(), any(), any(), any())
        }
    }

    @Test
    fun `when multiple cryptographic binding methods are given use first`() = runTest {
        val methods = listOf(MockIssuerCredentialConfiguration.DID_JWK_BINDING_METHOD, "other")
        val result = fetchCredentialUseCase(
            credentialConfiguration = vcSdJwtCredentialConfiguration.copy(cryptographicBindingMethodsSupported = methods),
            credentialOffer = offerWithPreAuthorizedCode,
        )

        result.assertOk()

        coVerify {
            mockCreateDidJwk(VALID_KEY_PAIR.keyPair, VALID_KEY_PAIR.algorithm, false)
        }
    }

    @Test
    fun `when no cryptographic binding method is given use did jwk`() = runTest {
        fetchCredentialUseCase(
            credentialConfiguration = vcSdJwtCredentialConfiguration.copy(cryptographicBindingMethodsSupported = emptyList()),
            credentialOffer = offerWithPreAuthorizedCode,
        ).assertOk()

        coVerify {
            mockCreateDidJwk(VALID_KEY_PAIR.keyPair, VALID_KEY_PAIR.algorithm, false)
        }
    }

    @Test
    fun `when null cryptographic binding method is given use did jwk`() = runTest {
        fetchCredentialUseCase(
            credentialConfiguration = vcSdJwtCredentialConfiguration.copy(cryptographicBindingMethodsSupported = null),
            credentialOffer = offerWithPreAuthorizedCode,
        ).assertOk()

        coVerify {
            mockCreateDidJwk(VALID_KEY_PAIR.keyPair, VALID_KEY_PAIR.algorithm, false)
        }
    }

    @Test
    fun `when the cryptographic binding method is not supported return an unsupported cryptographic suite error, access token not fetched`() = runTest {
        fetchCredentialUseCase(
            credentialConfiguration = vcSdJwtCredentialConfiguration.copy(cryptographicBindingMethodsSupported = listOf("other")),
            credentialOffer = offerWithPreAuthorizedCode,
        ).assertErrorType(CredentialOfferError.UnsupportedCryptographicSuite::class)

        coVerify(exactly = 0) {
            mockCredentialOfferRepository.fetchAccessToken(any(), any())
        }
    }

    @SuppressLint("CheckResult")
    @Test
    fun `credential offer without pre-authorized code should return an unsupported grant type error and delete the key pair, token not fetched`() = runTest {
        fetchCredentialUseCase(
            credentialConfiguration = vcSdJwtCredentialConfiguration,
            credentialOffer = offerWithoutPreAuthorizedCode,
        ).assertErrorType(CredentialOfferError.UnsupportedGrantType::class)

        coVerify {
            mockDeleteKeyPair(any())
        }

        coVerify(exactly = 0) {
            mockCredentialOfferRepository.fetchAccessToken(any(), any())
        }
    }

    @SuppressLint("CheckResult")
    @Test
    fun `when fetching the token fails return an invalid credential offer error and delete the key pair`() = runTest {
        coEvery {
            mockCredentialOfferRepository.fetchAccessToken(any(), any())
        } returns Err(CredentialOfferError.InvalidCredentialOffer)

        fetchCredentialUseCase(
            credentialConfiguration = vcSdJwtCredentialConfiguration,
            credentialOffer = offerWithPreAuthorizedCode,
        ).assertErrorType(CredentialOfferError.InvalidCredentialOffer::class)

        coVerify {
            mockDeleteKeyPair(any())
        }
    }

    @SuppressLint("CheckResult")
    @Test
    fun `if an error is thrown when creating a proof, it should return an unexpected error and delete the key pair`() = runTest {
        coEvery {
            mockCreateCredentialRequestProofJwt(any(), any(), any(), any())
        } returns Err(CredentialOfferError.Unexpected(IllegalStateException()))

        fetchCredentialUseCase(
            credentialConfiguration = vcSdJwtCredentialConfiguration,
            credentialOffer = offerWithPreAuthorizedCode,
        ).assertErrorType(CredentialOfferError.Unexpected::class)

        coVerify {
            mockDeleteKeyPair(any())
        }
    }

    @SuppressLint("CheckResult")
    @Test
    fun `failed fetch of credential should return a network error and delete the key pair`() = runTest {
        coEvery {
            mockCredentialOfferRepository.fetchCredential(any(), any(), any(), any())
        } returns Err(CredentialOfferError.NetworkInfoError)

        fetchCredentialUseCase(
            credentialConfiguration = vcSdJwtCredentialConfiguration,
            credentialOffer = offerWithPreAuthorizedCode,
        ).assertErrorType(CredentialOfferError.NetworkInfoError::class)

        coVerify {
            mockDeleteKeyPair(any())
        }
    }

    @SuppressLint("CheckResult")
    @Test
    fun `when credential has no key binding (empty proof type) it returns a VerifiableCredential without signing key and algorithm`() = runTest {
        val result = fetchCredentialUseCase(
            credentialConfiguration = credentialConfigurationWithoutProofTypesSupported,
            credentialOffer = offerWithPreAuthorizedCode,
        ).assertOk()

        coVerify(exactly = 0) {
            mockCreateDidJwk(any(), any(), any())
            mockCreateCredentialRequestProofJwt(any(), any(), any(), any())
        }

        assertAll(
            "Assert all VerifiableCredential properties",
            { assertEquals(CredentialFormat.VC_SD_JWT, result.format) },
            { assertEquals("credential", result.credential) },
            { assertEquals(null, result.signingKeyId) },
            { assertEquals(null, result.signingAlgorithm) }
        )
    }

    private val jwk = """
    {
        "crv": "P-256",
        "kty": "EC",
        "x": "Q7HpY9d8GlvGqfHtw-9jLLPZaIX9Lc91Q-Hfsz_WbBo",
        "y": "647ttGFFCBoy17NspJszfIW2pEwuzqdep69Av5Mprb8"
    }
    """

    private fun success() {
        coEvery {
            mockGenerateKeyPair(any())
        } returns Ok(VALID_KEY_PAIR)

        coEvery {
            mockCreateDidJwk(any(), any(), false)
        } returns Ok(jwk)
//        } returns Ok("did:jwk:publicKey")

        coEvery {
            mockCreateCredentialRequestProofJwt(any(), any(), any(), any())
        } returns Ok(jwtProof)

        coEvery {
            mockCredentialOfferRepository.fetchIssuerCredentialInformation(offerWithPreAuthorizedCode.credentialIssuer)
        } returns Ok(validIssuerCredentialInformation)

        coEvery {
            mockCredentialOfferRepository.fetchIssuerConfiguration(offerWithPreAuthorizedCode.credentialIssuer)
        } returns Ok(validIssuerConfig)

        coEvery {
            mockCredentialOfferRepository.fetchAccessToken(validIssuerConfig.tokenEndpoint, any())
        } returns Ok(validTokenResponse)

        coEvery {
            mockCredentialOfferRepository.fetchCredential(validIssuerCredentialInformation.credentialEndpoint, any(), any(), any())
        } returns Ok(validCredentialResponse)

        coEvery {
            mockDeleteKeyPair(any())
        } returns Ok(Unit)
    }
}

package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.RawAndParsedIssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.repository.CredentialOfferRepository
import ch.admin.foitt.openid4vc.domain.usecase.FetchRawAndParsedIssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.usecase.ValidateIssuerMetadataJwt
import ch.admin.foitt.openid4vc.domain.usecase.implementation.FetchRawAndParsedIssuerCredentialInfoImpl.Companion.TYPE
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockCredentialOffer.CREDENTIAL_ISSUER
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockCredentialOffer.TEST_JWT
import ch.admin.foitt.openid4vc.util.assertErrorType
import ch.admin.foitt.openid4vc.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URL

class FetchRawAndParsedIssuerCredentialInfoImplTest {

    @MockK
    private lateinit var mockCredentialOfferRepository: CredentialOfferRepository

    @MockK
    private lateinit var mockValidateIssuerMetadataJwt: ValidateIssuerMetadataJwt

    @MockK
    private lateinit var mockRawAndParsedIssuerCredentialInfo: RawAndParsedIssuerCredentialInfo

    private lateinit var useCase: FetchRawAndParsedIssuerCredentialInfo

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = FetchRawAndParsedIssuerCredentialInfoImpl(
            credentialOfferRepository = mockCredentialOfferRepository,
            validateIssuerMetadataJwt = mockValidateIssuerMetadataJwt,
        )
        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Suppress("CheckResult")
    @Test
    fun `UseCase should return the raw and parsed info when metadata is signed`() = runTest {
        val result = useCase(
            issuerEndpoint = CREDENTIAL_ISSUER,
            forceRefresh = true,
        ).assertOk()

        assertEquals(result, mockRawAndParsedIssuerCredentialInfo)
        coVerify(exactly = 1) {
            mockCredentialOfferRepository.fetchRawAndParsedIssuerCredentialInformation(
                issuerEndpoint = CREDENTIAL_ISSUER,
                forceRefresh = true,
            )
            mockValidateIssuerMetadataJwt(CREDENTIAL_ISSUER.toString(), TEST_JWT, TYPE)
        }
    }

    @Test
    fun `UseCase passes correct refresh argument along`() = runTest {
        useCase(
            issuerEndpoint = CREDENTIAL_ISSUER,
            forceRefresh = false,
        ).assertOk()

        coVerify(exactly = 1) {
            mockCredentialOfferRepository.fetchRawAndParsedIssuerCredentialInformation(
                issuerEndpoint = any(),
                forceRefresh = false,
            )
        }
    }

    @Test
    fun `UseCase should return an error when the issuer endpoint does not match the credential issuer in metadata`() = runTest {
        every {
            mockRawAndParsedIssuerCredentialInfo.issuerCredentialInfo.credentialIssuer
        } returns URL("https://issuer.other.com")

        val result = useCase(
            issuerEndpoint = CREDENTIAL_ISSUER,
        )

        result.assertErrorType(CredentialOfferError.InvalidSignedMetadata::class)
    }

    @Test
    fun `UseCase should return an error when the CredentialOffer repository returns an error`() = runTest {
        coEvery {
            mockCredentialOfferRepository.fetchRawAndParsedIssuerCredentialInformation(any())
        } returns Err(CredentialOfferError.NetworkInfoError)

        val result = useCase(
            issuerEndpoint = CREDENTIAL_ISSUER,
        )

        result.assertErrorType(CredentialOfferError.NetworkInfoError::class)
    }

    @Test
    fun `UseCase should return an error when the jwt validation returns an error`() = runTest {
        coEvery {
            mockValidateIssuerMetadataJwt(any(), any(), any())
        } returns Err(CredentialOfferError.InvalidSignedMetadata("test"))

        val result = useCase(
            issuerEndpoint = CREDENTIAL_ISSUER,
        )

        result.assertErrorType(CredentialOfferError.InvalidSignedMetadata::class)
    }

    private fun setupDefaultMocks() {
        coEvery {
            mockCredentialOfferRepository.fetchRawAndParsedIssuerCredentialInformation(
                issuerEndpoint = CREDENTIAL_ISSUER,
                forceRefresh = any(),
            )
        } returns Ok(mockRawAndParsedIssuerCredentialInfo)
        every {
            mockRawAndParsedIssuerCredentialInfo.issuerCredentialInfo.credentialIssuer
        } returns CREDENTIAL_ISSUER
        every {
            mockRawAndParsedIssuerCredentialInfo.rawIssuerCredentialInfo
        } returns TEST_JWT
        coEvery {
            mockValidateIssuerMetadataJwt(credentialIssuerIdentifier = CREDENTIAL_ISSUER.toString(), jwt = any(), any())
        } returns Ok(Unit)
    }
}

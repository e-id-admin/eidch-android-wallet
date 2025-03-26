package ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.implementation

import android.annotation.SuppressLint
import ch.admin.foitt.openid4vc.domain.model.VerifiableCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOffer
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.VcSdJwtCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.mock.TypeMetadataMocks.TYPE_METADATA_INVALID
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.mock.TypeMetadataMocks.VCT_INTEGRITY
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.mock.TypeMetadataMocks.typeMetadataVctOther
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.mock.TypeMetadataMocks.typeMetadataVctUrl
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.mock.VcSdJwtMocks
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.mock.VcSdJwtMocks.VC_SD_JWT_FULL_SAMPLE
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.mock.VcSdJwtMocks.VC_SD_JWT_VCT_NO_URL
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.mock.VcSdJwtMocks.VC_SD_JWT_WITHOUT_VCT_INTEGRITY
import ch.admin.foitt.openid4vc.domain.repository.TypeMetadataRepository
import ch.admin.foitt.openid4vc.domain.usecase.FetchVerifiableCredential
import ch.admin.foitt.openid4vc.domain.usecase.VerifyJwtSignature
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.FetchVcSdJwtCredential
import ch.admin.foitt.openid4vc.util.SafeJsonTestInstance
import ch.admin.foitt.openid4vc.util.assertErrorType
import ch.admin.foitt.openid4vc.util.assertOk
import ch.admin.foitt.sriValidator.domain.SRIValidator
import ch.admin.foitt.sriValidator.domain.model.SRIError
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
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError as OpenIdCredentialOfferError

class FetchVcSdJwtCredentialImplTest {

    @MockK
    private lateinit var mockFetchVerifiableCredential: FetchVerifiableCredential

    @MockK
    private lateinit var mockCredentialConfig: VcSdJwtCredentialConfiguration

    @MockK
    private lateinit var mockCredentialOffer: CredentialOffer

    @MockK
    private lateinit var mockVerifyJwtSignature: VerifyJwtSignature

    @MockK
    private lateinit var mockTypeMetadataRepository: TypeMetadataRepository

    @MockK
    private lateinit var mockSRIValidator: SRIValidator

    private lateinit var useCase: FetchVcSdJwtCredential

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = FetchVcSdJwtCredentialImpl(
            fetchVerifiableCredential = mockFetchVerifiableCredential,
            verifyJwtSignature = mockVerifyJwtSignature,
            typeMetadataRepository = mockTypeMetadataRepository,
            safeJson = SafeJsonTestInstance.safeJson,
            sriValidator = mockSRIValidator,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Fetching jwt vc json credential which is valid returns a credential`(): Unit = runTest {
        val result = useCase(mockCredentialConfig, mockCredentialOffer)

        val credential = result.assertOk()
        assertEquals(KEY_BINDING_IDENTIFIER, credential.keyBindingIdentifier)
        assertEquals(PAYLOAD, credential.payload)
        assertEquals(CredentialFormat.VC_SD_JWT, credential.format)
        assertEquals(
            SafeJsonTestInstance.json.parseToJsonElement(VcSdJwtMocks.VC_SD_JWT_FULL_SAMPLE_JSON),
            credential.getClaimsToSave()
        )
    }

    @SuppressLint("CheckResult")
    @Test
    fun `Fetching jwt vc json credential where the vct claim is no url returns the credential but does not fetch type metadata`() = runTest {
        coEvery {
            mockFetchVerifiableCredential(mockCredentialConfig, mockCredentialOffer)
        } returns Ok(mockVerifiableCredentialValidVctNoUrl)

        val result = useCase(mockCredentialConfig, mockCredentialOffer)

        val credential = result.assertOk()
        assertEquals(KEY_BINDING_IDENTIFIER, credential.keyBindingIdentifier)
        assertEquals(VC_SD_JWT_VCT_NO_URL, credential.payload)
        assertEquals(CredentialFormat.VC_SD_JWT, credential.format)
        assertEquals(
            SafeJsonTestInstance.json.parseToJsonElement(VcSdJwtMocks.VC_SD_JWT_FULL_SAMPLE_JSON),
            credential.getClaimsToSave()
        )

        coVerify(exactly = 0) {
            mockTypeMetadataRepository.fetchTypeMetadata(any())
            mockSRIValidator.validate(any(), any())
        }
    }

    @Test
    fun `Fetching jwt vc json credential maps errors from fetching verifiable credential`(): Unit = runTest {
        val exception = IllegalStateException()
        coEvery {
            mockFetchVerifiableCredential(any(), any())
        } returns Err(OpenIdCredentialOfferError.Unexpected(exception))

        val result = useCase(mockCredentialConfig, mockCredentialOffer)

        val error = result.assertErrorType(CredentialOfferError.Unexpected::class)
        assertEquals(exception, error.cause)
    }

    @Test
    fun `Fetching jwt vc json credential returns an error if the jwt payload contains non-reserved claim names`() = runTest {
        coEvery {
            mockFetchVerifiableCredential(mockCredentialConfig, mockCredentialOffer)
        } returns Ok(mockVerifiableCredentialInvalid)

        useCase(mockCredentialConfig, mockCredentialOffer).assertErrorType(CredentialOfferError.InvalidCredentialOffer::class)
    }

    @Test
    fun `Fetching jwt vc json credential maps errors from verifying jwt`(): Unit = runTest {
        coEvery {
            mockVerifyJwtSignature(any(), any(), any())
        } returns Err(VcSdJwtError.InvalidJwt)

        useCase(mockCredentialConfig, mockCredentialOffer).assertErrorType(CredentialOfferError.IntegrityCheckFailed::class)
    }

    @Test
    fun `Fetching jwt vc json credential maps errors from fetching type metadata`() = runTest {
        val exception = IllegalStateException()
        coEvery {
            mockTypeMetadataRepository.fetchTypeMetadata(any())
        } returns Err(VcSdJwtError.Unexpected(exception))

        val result = useCase(mockCredentialConfig, mockCredentialOffer)

        val error = result.assertErrorType(CredentialOfferError.Unexpected::class)
        assertEquals(exception, error.cause)
    }

    @Test
    fun `Fetching jwt vc json credential returns an error if type metadata can not be deserialized`() = runTest {
        coEvery { mockTypeMetadataRepository.fetchTypeMetadata(any()) } returns Ok(TYPE_METADATA_INVALID)

        useCase(mockCredentialConfig, mockCredentialOffer).assertErrorType(CredentialOfferError.Unexpected::class)
    }

    @Test
    fun `Fetching jwt vc json credential returns an error if type metadata vct and credential vct are not equal`() = runTest {
        coEvery { mockTypeMetadataRepository.fetchTypeMetadata(any()) } returns Ok(typeMetadataVctOther)

        useCase(mockCredentialConfig, mockCredentialOffer).assertErrorType(CredentialOfferError.InvalidCredentialOffer::class)
    }

    @Test
    fun `Fetching jwt vc json credential returns an error if vct is url but vct#integrity is missing`() = runTest {
        coEvery {
            mockFetchVerifiableCredential(mockCredentialConfig, mockCredentialOffer)
        } returns Ok(mockVerifiableCredentialVctUrlVctIntegrityMissing)

        useCase(mockCredentialConfig, mockCredentialOffer).assertErrorType(CredentialOfferError.InvalidCredentialOffer::class)
    }

    @Test
    fun `Fetching jwt vc json credential returns an error if the subresource validation returns false`() = runTest {
        coEvery { mockSRIValidator.validate(any(), any()) } returns false

        useCase(mockCredentialConfig, mockCredentialOffer).assertErrorType(CredentialOfferError.InvalidCredentialOffer::class)
    }

    @Test
    fun `Fetching jwt vc json credential maps errors from subresource validation`() = runTest {
        coEvery { mockSRIValidator.validate(any(), any()) } throws SRIError.MalformedIntegrity

        useCase(mockCredentialConfig, mockCredentialOffer).assertErrorType(CredentialOfferError.InvalidCredentialOffer::class)
    }

    private fun setupDefaultMocks() {
        every { mockCredentialOffer.credentialIssuer } returns CREDENTIAL_ISSUER

        coEvery {
            mockFetchVerifiableCredential(mockCredentialConfig, mockCredentialOffer)
        } returns Ok(mockVerifiableCredentialValid)

        coEvery { mockVerifyJwtSignature(any(), any(), any()) } returns Ok(Unit)

        coEvery { mockTypeMetadataRepository.fetchTypeMetadata(any()) } returns Ok(typeMetadataVctUrl)

        coEvery {
            mockSRIValidator.validate(any(), VCT_INTEGRITY)
        } returns true
    }

    @Test
    fun `Fetching jwt vc json credential maps DidDocumentDeactivated error from verifying jwt to IntegrityCheckFailed`(): Unit = runTest {
        coEvery {
            mockVerifyJwtSignature(any(), any(), any())
        } returns Err(VcSdJwtError.DidDocumentDeactivated)

        val result = useCase(mockCredentialConfig, mockCredentialOffer)

        result.assertErrorType(CredentialOfferError.IntegrityCheckFailed::class)
    }

    private companion object {
        const val CREDENTIAL_ISSUER = "credentialIssuer"
        const val KEY_BINDING_IDENTIFIER = "signingKeyId"

        const val PAYLOAD = VC_SD_JWT_FULL_SAMPLE
        val mockVerifiableCredentialValid = VerifiableCredential(
            keyBindingIdentifier = KEY_BINDING_IDENTIFIER,
            credential = PAYLOAD,
            format = CredentialFormat.VC_SD_JWT,
            keyBindingAlgorithm = SigningAlgorithm.ES512,
        )

        val mockVerifiableCredentialInvalid = VerifiableCredential(
            keyBindingIdentifier = KEY_BINDING_IDENTIFIER,
            credential = VcSdJwtMocks.VC_SD_JWT_NON_DISCLOSABLE_CLAIMS,
            format = CredentialFormat.VC_SD_JWT,
            keyBindingAlgorithm = SigningAlgorithm.ES512,
        )

        val mockVerifiableCredentialValidVctNoUrl = mockVerifiableCredentialValid.copy(
            credential = VC_SD_JWT_VCT_NO_URL,
        )

        val mockVerifiableCredentialVctUrlVctIntegrityMissing = mockVerifiableCredentialValid.copy(
            credential = VC_SD_JWT_WITHOUT_VCT_INTEGRITY
        )
    }
}

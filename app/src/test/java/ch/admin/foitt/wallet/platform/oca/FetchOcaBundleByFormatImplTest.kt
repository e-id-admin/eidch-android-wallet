package ch.admin.foitt.wallet.platform.oca

import android.annotation.SuppressLint
import ch.admin.foitt.jsonSchema.domain.JsonSchema
import ch.admin.foitt.jsonSchema.domain.model.JsonSchemaError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtError
import ch.admin.foitt.openid4vc.domain.repository.TypeMetadataRepository
import ch.admin.foitt.openid4vc.domain.repository.VcSchemaRepository
import ch.admin.foitt.sriValidator.domain.SRIValidator
import ch.admin.foitt.sriValidator.domain.model.SRIError
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaError
import ch.admin.foitt.wallet.platform.oca.domain.usecase.FetchOcaBundleByFormat
import ch.admin.foitt.wallet.platform.oca.domain.usecase.implementation.FetchOcaBundleByFormatImpl
import ch.admin.foitt.wallet.platform.oca.mock.CredentialMocks.VC_SD_JWT_FULL_SAMPLE
import ch.admin.foitt.wallet.platform.oca.mock.CredentialMocks.VC_SD_JWT_VCT_NO_URL
import ch.admin.foitt.wallet.platform.oca.mock.CredentialMocks.VC_SD_JWT_WITHOUT_VCT_INTEGRITY
import ch.admin.foitt.wallet.platform.oca.mock.OcaMocks.ocaResponse
import ch.admin.foitt.wallet.platform.oca.mock.TypeMetadataMocks.OCA_DATA_URI
import ch.admin.foitt.wallet.platform.oca.mock.TypeMetadataMocks.OCA_DATA_URI_INTEGRITY
import ch.admin.foitt.wallet.platform.oca.mock.TypeMetadataMocks.OCA_URL
import ch.admin.foitt.wallet.platform.oca.mock.TypeMetadataMocks.OCA_URL_INTEGRITY
import ch.admin.foitt.wallet.platform.oca.mock.TypeMetadataMocks.VCT_INTEGRITY
import ch.admin.foitt.wallet.platform.oca.mock.TypeMetadataMocks.VCT_URL
import ch.admin.foitt.wallet.platform.oca.mock.TypeMetadataMocks.VC_SCHEMA_URL
import ch.admin.foitt.wallet.platform.oca.mock.TypeMetadataMocks.VC_SCHEMA_URL_INTEGRITY
import ch.admin.foitt.wallet.platform.oca.mock.TypeMetadataMocks.typeMetadataFullExample
import ch.admin.foitt.wallet.platform.oca.mock.TypeMetadataMocks.typeMetadataInvalid
import ch.admin.foitt.wallet.platform.oca.mock.TypeMetadataMocks.typeMetadataVctOther
import ch.admin.foitt.wallet.platform.oca.mock.TypeMetadataMocks.typeMetadataWithOcaDataUri
import ch.admin.foitt.wallet.platform.oca.mock.TypeMetadataMocks.typeMetadataWithOcaDataUriMissingIntegrity
import ch.admin.foitt.wallet.platform.oca.mock.TypeMetadataMocks.typeMetadataWithOcaInvalidUri
import ch.admin.foitt.wallet.platform.oca.mock.TypeMetadataMocks.typeMetadataWithOcaMissingUrlIntegrity
import ch.admin.foitt.wallet.platform.oca.mock.TypeMetadataMocks.typeMetadataWithOcaMultipleRenderings
import ch.admin.foitt.wallet.platform.oca.mock.TypeMetadataMocks.typeMetadataWithOcaUrl
import ch.admin.foitt.wallet.platform.oca.mock.TypeMetadataMocks.typeMetadataWithoutOcaRendering
import ch.admin.foitt.wallet.platform.oca.mock.TypeMetadataMocks.typeMetadataWithoutSchemaUrl
import ch.admin.foitt.wallet.platform.oca.mock.TypeMetadataMocks.typeMetadataWithoutSchemaUrlIntegrity
import ch.admin.foitt.wallet.platform.oca.mock.VcSchemaMocks.vcSchema
import ch.admin.foitt.wallet.util.SafeJsonTestInstance
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import ch.admin.foitt.wallet.util.assertOkNullable
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URL

class FetchOcaBundleByFormatImplTest {

    @MockK
    private lateinit var mockTypeMetadataRepository: TypeMetadataRepository

    @MockK
    private lateinit var mockVcSchemaRepository: VcSchemaRepository

    @MockK
    private lateinit var mockOcaRepository: ch.admin.foitt.wallet.platform.oca.domain.repository.OcaRepository

    @MockK
    private lateinit var mockSRIValidator: SRIValidator

    @MockK
    private lateinit var mockJsonSchema: JsonSchema

    private lateinit var useCase: FetchOcaBundleByFormat

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = FetchOcaBundleByFormatImpl(
            typeMetadataRepository = mockTypeMetadataRepository,
            vcSchemaRepository = mockVcSchemaRepository,
            ocaRepository = mockOcaRepository,
            safeJson = SafeJsonTestInstance.safeJson,
            sriValidator = mockSRIValidator,
            jsonSchema = mockJsonSchema,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @SuppressLint("CheckResult")
    @Test
    fun `Fetching oca for VcSdJwt credential returns oca bundle`() = runTest {
        val result = useCase(mockVcSdJwtCredential)

        val oca = result.assertOk()
        assertEquals(ocaResponse, oca)

        coVerify {
            mockTypeMetadataRepository.fetchTypeMetadata(URL(VCT_URL))
            mockSRIValidator.validate(typeMetadataFullExample.encodeToByteArray(), VCT_INTEGRITY)
            mockVcSchemaRepository.fetchVcSchema(URL(VC_SCHEMA_URL))
            mockSRIValidator.validate(vcSchema.encodeToByteArray(), VC_SCHEMA_URL_INTEGRITY)
            mockJsonSchema.validate(any(), vcSchema.encodeToByteArray())
            mockOcaRepository.fetchVcSdJwtOcaBundle(URL(OCA_URL))
            mockSRIValidator.validate(ocaResponse.encodeToByteArray(), OCA_URL_INTEGRITY)
        }
    }

    @SuppressLint("CheckResult")
    @Test
    fun `Fetching oca for VcSdJwt credential where the vct claim is no url does not fetch the oca`() = runTest {
        val result = useCase(mockVcSdJwtCredentialValidVctNoUrl)

        val oca = result.assertOkNullable()
        assertNull(oca)

        coVerify(exactly = 0) {
            mockTypeMetadataRepository.fetchTypeMetadata(any())
            mockSRIValidator.validate(any(), any())
        }
    }

    @Test
    fun `Fetching oca for VcSdJwt credential maps errors from fetching type metadata`() = runTest {
        val exception = IllegalStateException()
        coEvery {
            mockTypeMetadataRepository.fetchTypeMetadata(any())
        } returns Err(VcSdJwtError.Unexpected(exception))

        val result = useCase(mockVcSdJwtCredential)

        val error = result.assertErrorType(OcaError.Unexpected::class)
        assertEquals(exception, error.cause)
    }

    @Test
    fun `Fetching oca for VcSdJwt credential returns an error if type metadata can not be deserialized`() = runTest {
        coEvery { mockTypeMetadataRepository.fetchTypeMetadata(any()) } returns Ok(typeMetadataInvalid)

        useCase(mockVcSdJwtCredential).assertErrorType(OcaError.Unexpected::class)
    }

    @Test
    fun `Fetching oca for VcSdJwt credential returns an error if type metadata vct and credential vct are not equal`() = runTest {
        coEvery { mockTypeMetadataRepository.fetchTypeMetadata(any()) } returns Ok(typeMetadataVctOther)

        useCase(mockVcSdJwtCredential).assertErrorType(OcaError.InvalidOca::class)
    }

    @Test
    fun `Fetching oca for VcSdJwt credential returns an error if vct is url but vct#integrity is missing`() = runTest {
        useCase(mockVcSdJwtCredentialVctUrlVctIntegrityMissing).assertErrorType(OcaError.InvalidOca::class)
    }

    @Test
    fun `Fetching oca for VcSdJwt credential returns an error if the subresource validation returns false`() = runTest {
        coEvery { mockSRIValidator.validate(any(), any()) } returns false

        useCase(mockVcSdJwtCredential).assertErrorType(OcaError.InvalidOca::class)
    }

    @Test
    fun `Fetching oca for VcSdJwt credential maps errors from subresource validation`() = runTest {
        coEvery { mockSRIValidator.validate(any(), any()) } throws SRIError.MalformedIntegrity

        useCase(mockVcSdJwtCredential).assertErrorType(OcaError.InvalidOca::class)
    }

    @Test
    fun `Fetching oca for VcSdJwt credential does not fetch vc schema if type metadata schemaUrl is not provided`() = runTest {
        coEvery { mockTypeMetadataRepository.fetchTypeMetadata(any()) } returns Ok(typeMetadataWithoutSchemaUrl)

        val result = useCase(mockVcSdJwtCredential).assertOkNullable()
        assertNull(result)

        coVerify(exactly = 0) {
            mockVcSchemaRepository.fetchVcSchema(any())
        }
    }

    @Test
    fun `Fetching oca for VcSdJwt credential maps error from vc schema repository`() = runTest {
        val exception = IllegalStateException()
        coEvery {
            mockVcSchemaRepository.fetchVcSchema(any())
        } returns Err(VcSdJwtError.Unexpected(exception))

        val result = useCase(mockVcSdJwtCredential)

        val error = result.assertErrorType(OcaError.Unexpected::class)
        assertEquals(exception, error.cause)
    }

    @Test
    fun `Fetching oca for VcSdJwt credential does not call SRI validator for vc schema if type metadata schemaUrlIntegrity not provided`() = runTest {
        coEvery { mockTypeMetadataRepository.fetchTypeMetadata(any()) } returns Ok(typeMetadataWithoutSchemaUrlIntegrity)

        val result = useCase(mockVcSdJwtCredential).assertOkNullable()
        assertEquals(ocaResponse, result)

        coVerify(exactly = 0) {
            mockSRIValidator.validate(any(), vcSchema)
        }
    }

    @Test
    fun `Fetching oca for VcSdJwt credential where json schema validation fails returns an error`() = runTest {
        coEvery {
            mockJsonSchema.validate(any(), any())
        } returns Err(JsonSchemaError.Unexpected)

        useCase(mockVcSdJwtCredential).assertErrorType(OcaError.InvalidOca::class)
    }

    @Test
    fun `Fetching oca for VcSdJwt credential does not fetch oca if type metadata does not contain a oca rendering`() = runTest {
        coEvery { mockTypeMetadataRepository.fetchTypeMetadata(any()) } returns Ok(typeMetadataWithoutOcaRendering)

        val result = useCase(mockVcSdJwtCredential).assertOkNullable()
        assertNull(result)

        coVerify(exactly = 0) {
            mockOcaRepository.fetchVcSdJwtOcaBundle(any())
        }
    }

    @Test
    fun `Fetching oca for VcSdJwt credential takes the first oca rendering`() = runTest {
        coEvery { mockTypeMetadataRepository.fetchTypeMetadata(any()) } returns Ok(typeMetadataWithOcaMultipleRenderings)

        val result = useCase(mockVcSdJwtCredential).assertOk()
        assertEquals(ocaResponse, result)

        coVerify(exactly = 1) {
            mockOcaRepository.fetchVcSdJwtOcaBundle(URL(OCA_URL))
        }
    }

    @SuppressLint("CheckResult")
    @Test
    fun `Fetching oca for VcSdJwt credential gets the oca from the url if the schema is https`() = runTest {
        coEvery { mockTypeMetadataRepository.fetchTypeMetadata(any()) } returns Ok(typeMetadataWithOcaUrl)

        val result = useCase(mockVcSdJwtCredential).assertOk()
        assertEquals(ocaResponse, result)

        coVerify(exactly = 1) {
            mockOcaRepository.fetchVcSdJwtOcaBundle(URL(OCA_URL))
            mockSRIValidator.validate(ocaResponse.encodeToByteArray(), OCA_URL_INTEGRITY)
        }
    }

    @Test
    fun `Fetching oca for VcSdJwt credential maps errors from the oca repo`() = runTest {
        coEvery { mockOcaRepository.fetchVcSdJwtOcaBundle(any()) } returns Err(OcaError.NetworkError)

        useCase(mockVcSdJwtCredential).assertErrorType(OcaError.NetworkError::class)
    }

    @Test
    fun `Fetching oca for VcSdJwt credential where oca is https but uri#integrity is missing returns an error`() = runTest {
        coEvery { mockTypeMetadataRepository.fetchTypeMetadata(any()) } returns Ok(typeMetadataWithOcaMissingUrlIntegrity)

        useCase(mockVcSdJwtCredential).assertErrorType(OcaError.InvalidOca::class)
    }

    @Test
    fun `Fetching oca for VcSdJwt credential maps errors from oca subresource validation`() = runTest {
        coEvery { mockSRIValidator.validate(any(), OCA_URL_INTEGRITY) } returns false

        useCase(mockVcSdJwtCredential).assertErrorType(OcaError.InvalidOca::class)
    }

    @SuppressLint("CheckResult")
    @Test
    fun `Fetching oca for VcSdJwt credential gets the oca from the uri if the schema is data`() = runTest {
        coEvery { mockTypeMetadataRepository.fetchTypeMetadata(any()) } returns Ok(typeMetadataWithOcaDataUri)

        val result = useCase(mockVcSdJwtCredential).assertOk()
        assertNotNull(result)

        coVerify(exactly = 0) {
            mockOcaRepository.fetchVcSdJwtOcaBundle(URL(OCA_URL))
        }

        coVerify(exactly = 1) {
            mockSRIValidator.validate(OCA_DATA_URI.encodeToByteArray(), OCA_DATA_URI_INTEGRITY)
        }
    }

    @SuppressLint("CheckResult")
    @Test
    fun `Fetching oca for VcSdJwt credential does not call SRI validator if data uri but uri#integrity is missing`() = runTest {
        coEvery { mockTypeMetadataRepository.fetchTypeMetadata(any()) } returns Ok(typeMetadataWithOcaDataUriMissingIntegrity)

        val result = useCase(mockVcSdJwtCredential).assertOk()
        assertNotNull(result)

        coVerify(exactly = 0) {
            mockOcaRepository.fetchVcSdJwtOcaBundle(URL(OCA_URL))
            mockSRIValidator.validate(OCA_DATA_URI.encodeToByteArray(), OCA_DATA_URI_INTEGRITY)
        }
    }

    @Test
    fun `Fetching oca for VcSdJwt credential where the oca uri is not of https or data schema returns an error`() = runTest {
        coEvery { mockTypeMetadataRepository.fetchTypeMetadata(any()) } returns Ok(typeMetadataWithOcaInvalidUri)

        useCase(mockVcSdJwtCredential).assertErrorType(OcaError.InvalidOca::class)

        coVerify(exactly = 0) {
            mockOcaRepository.fetchVcSdJwtOcaBundle(any())
        }
    }

    private fun setupDefaultMocks() {
        coEvery { mockTypeMetadataRepository.fetchTypeMetadata(any()) } returns Ok(typeMetadataFullExample)
        coEvery { mockVcSchemaRepository.fetchVcSchema(any()) } returns Ok(vcSchema)
        coEvery { mockOcaRepository.fetchVcSdJwtOcaBundle(any()) } returns Ok(ocaResponse)

        coEvery { mockSRIValidator.validate(any(), VCT_INTEGRITY) } returns true
        coEvery { mockSRIValidator.validate(any(), VC_SCHEMA_URL_INTEGRITY) } returns true
        coEvery { mockSRIValidator.validate(any(), OCA_URL_INTEGRITY) } returns true

        coEvery { mockJsonSchema.validate(any(), any()) } returns Ok(Unit)
    }

    private companion object {
        const val KEY_BINDING_IDENTIFIER = "signingKeyId"

        const val PAYLOAD = VC_SD_JWT_FULL_SAMPLE
        val mockVcSdJwtCredential = VcSdJwtCredential(
            keyBindingIdentifier = KEY_BINDING_IDENTIFIER,
            keyBindingAlgorithm = SigningAlgorithm.ES512,
            payload = PAYLOAD,
        )

        val mockVcSdJwtCredentialValidVctNoUrl = VcSdJwtCredential(
            keyBindingIdentifier = KEY_BINDING_IDENTIFIER,
            keyBindingAlgorithm = SigningAlgorithm.ES512,
            payload = VC_SD_JWT_VCT_NO_URL,
        )

        val mockVcSdJwtCredentialVctUrlVctIntegrityMissing = VcSdJwtCredential(
            keyBindingIdentifier = KEY_BINDING_IDENTIFIER,
            keyBindingAlgorithm = SigningAlgorithm.ES512,
            payload = VC_SD_JWT_WITHOUT_VCT_INTEGRITY,
        )
    }
}

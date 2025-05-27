package ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.implementation

import android.annotation.SuppressLint
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.TypeMetadata
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.TypeMetadataError
import ch.admin.foitt.openid4vc.domain.repository.TypeMetadataRepository
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.FetchTypeMetadata
import ch.admin.foitt.openid4vc.util.SafeJsonTestInstance
import ch.admin.foitt.openid4vc.util.assertErrorType
import ch.admin.foitt.openid4vc.util.assertOk
import ch.admin.foitt.sriValidator.domain.SRIValidator
import ch.admin.foitt.sriValidator.domain.model.SRIError
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URL

class FetchTypeMetadataImplTest {

    @MockK
    private lateinit var mockTypeMetadataRepository: TypeMetadataRepository

    @MockK
    private lateinit var mockSRIValidator: SRIValidator

    private val credentialVctUrl = URL(CREDENTIAL_VCT_URL)

    @MockK
    private lateinit var mockTypeMetadata: TypeMetadata

    private lateinit var useCase: FetchTypeMetadata

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = FetchTypeMetadataImpl(
            typeMetadataRepository = mockTypeMetadataRepository,
            safeJson = SafeJsonTestInstance.safeJson,
            sriValidator = mockSRIValidator,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() = runTest {
        unmockkAll()
    }

    @SuppressLint("CheckResult")
    @Test
    fun `Fetching type metadata for VcSdJwt runs specific steps`() = runTest {
        useCase(credentialVctUrl, CREDENTIAL_VCT_INTEGRITY).assertOk()

        coEvery {
            mockTypeMetadataRepository.fetchTypeMetadata(URL(CREDENTIAL_VCT_URL))
            mockSRIValidator(TYPE_METADATA_STRING.encodeToByteArray(), CREDENTIAL_VCT_INTEGRITY)
        }
    }

    @Test
    fun `Fetching type metadata for VcSdJwt maps errors from type metadata repo`() = runTest {
        coEvery { mockTypeMetadataRepository.fetchTypeMetadata(any()) } returns Err(TypeMetadataError.NetworkError)

        useCase(credentialVctUrl, CREDENTIAL_VCT_INTEGRITY).assertErrorType(TypeMetadataError.NetworkError::class)
    }

    @Test
    fun `Fetching type metadata for VcSdJwt maps errors from json decoding`() = runTest {
        coEvery { mockTypeMetadataRepository.fetchTypeMetadata(any()) } returns Ok("invalid type metadata json")

        useCase(credentialVctUrl, CREDENTIAL_VCT_INTEGRITY).assertErrorType(TypeMetadataError.Unexpected::class)
    }

    @Test
    fun `Fetching type metadata for VcSdJwt where credential vct is not equal to type metadata vct returns an error`() = runTest {
        coEvery { mockTypeMetadataRepository.fetchTypeMetadata(any()) } returns Ok(TYPE_METADATA_STRING_OTHER_VCT)

        useCase(credentialVctUrl, CREDENTIAL_VCT_INTEGRITY).assertErrorType(TypeMetadataError.InvalidData::class)
    }

    @Test
    fun `Fetching type metadata for VcSdJwt where credential vct integrity is null returns an error`() = runTest {
        useCase(credentialVctUrl, null).assertErrorType(TypeMetadataError.InvalidData::class)
    }

    @Test
    fun `Fetching type metadata for VcSdJwt maps error from SRI validation`() = runTest {
        coEvery { mockSRIValidator(any(), any()) } returns Err(SRIError.ValidationFailed)

        useCase(credentialVctUrl, CREDENTIAL_VCT_INTEGRITY).assertErrorType(TypeMetadataError.InvalidData::class)
    }

    private fun setupDefaultMocks() = runTest {
        every { mockTypeMetadata.vct } returns CREDENTIAL_VCT_URL

        coEvery { mockTypeMetadataRepository.fetchTypeMetadata(URL(CREDENTIAL_VCT_URL)) } returns Ok(TYPE_METADATA_STRING)

        coEvery { mockSRIValidator(TYPE_METADATA_STRING.encodeToByteArray(), CREDENTIAL_VCT_INTEGRITY) } returns Ok(Unit)
        coEvery {
            mockSRIValidator(TYPE_METADATA_STRING_OTHER_VCT.encodeToByteArray(), CREDENTIAL_VCT_INTEGRITY)
        } returns Ok(Unit)
    }

    private companion object {
        const val CREDENTIAL_VCT_URL = "https://example.com/vct"
        const val CREDENTIAL_VCT_INTEGRITY = "sha256-vctIntegrity"

        val TYPE_METADATA_STRING = """
            {
              "vct": "$CREDENTIAL_VCT_URL"
            }
        """.trimIndent()

        val TYPE_METADATA_STRING_OTHER_VCT = """
            {
              "vct": "otherVct"
            }
        """.trimIndent()
    }
}

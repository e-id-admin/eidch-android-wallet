package ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.implementation

import android.annotation.SuppressLint
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSchemaError
import ch.admin.foitt.openid4vc.domain.repository.VcSchemaRepository
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.FetchVcSchema
import ch.admin.foitt.openid4vc.util.assertErrorType
import ch.admin.foitt.openid4vc.util.assertOk
import ch.admin.foitt.sriValidator.domain.SRIValidator
import ch.admin.foitt.sriValidator.domain.model.SRIError
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URL
import kotlin.jvm.Throws

class FetchVcSchemaImplTest {

    @MockK
    private lateinit var mockVcSchemaRepository: VcSchemaRepository

    @MockK
    private lateinit var mockSRIValidator: SRIValidator

    private lateinit var useCase: FetchVcSchema

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = FetchVcSchemaImpl(
            vcSchemaRepository = mockVcSchemaRepository,
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
    fun `Fetching vc schema runs specific steps`() = runTest {
        useCase(URL(SCHEMA_URL), SCHEMA_URL_INTEGRITY).assertOk()

        coVerify {
            mockVcSchemaRepository.fetchVcSchema(URL(SCHEMA_URL))
            mockSRIValidator(VC_SCHEMA_STRING.encodeToByteArray(), SCHEMA_URL_INTEGRITY)
        }
    }

    @Test
    @Throws(IllegalStateException::class)
    fun `Fetching vc schema maps errors from vc schema repo`() = runTest {
        coEvery { mockVcSchemaRepository.fetchVcSchema(any()) } returns Err(VcSchemaError.NetworkError)

        useCase(URL(SCHEMA_URL), SCHEMA_URL_INTEGRITY).assertErrorType(VcSchemaError.NetworkError::class)
    }

    @Test
    fun `Fetching vc schema where schema uri integrity is not provided does not call SRI validator`() = runTest {
        useCase(URL(SCHEMA_URL), null).assertOk()

        coVerify(exactly = 0) {
            mockSRIValidator(any(), any())
        }
    }

    @Test
    fun `Fetching vc schema maps error from SRI validation`() = runTest {
        coEvery { mockSRIValidator(any(), any()) } returns Err(SRIError.ValidationFailed)

        useCase(URL(SCHEMA_URL), SCHEMA_URL_INTEGRITY).assertErrorType(VcSchemaError.InvalidVcSchema::class)
    }

    private fun setupDefaultMocks() = runTest {
        coEvery { mockVcSchemaRepository.fetchVcSchema(URL(SCHEMA_URL)) } returns Ok(VC_SCHEMA_STRING)

        coEvery { mockSRIValidator(VC_SCHEMA_STRING.encodeToByteArray(), SCHEMA_URL_INTEGRITY) } returns Ok(Unit)
    }

    private companion object {
        const val SCHEMA_URL = "https://example.com/schema"
        const val SCHEMA_URL_INTEGRITY = "sha256-schemaUrlIntegrity"

        const val VC_SCHEMA_STRING = "schema"
    }
}

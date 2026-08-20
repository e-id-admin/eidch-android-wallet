package ch.admin.foitt.wallet.platform.eIdApplicationProcess

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdAvRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.CreateAutoVerificationDPoP
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation.UploadFileToCaseImpl
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.ktor.http.ContentType
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URL

class UploadFileToCaseTest {

    @MockK
    private lateinit var mockEIdAvRepository: EIdAvRepository

    @MockK
    private lateinit var mockCreateAutoVerificationDPoP: CreateAutoVerificationDPoP

    private val caseId = "caseId1"
    private val accessToken = "accessToken"
    private val fileName = "fileName"
    private val uploadFileUrl = URL("https://example.com")
    private val avDPoP = "avDPoP"
    private val contentType = ContentType.Video.MP4
    private val documentData = byteArrayOf(1, 2, 3)

    private lateinit var useCase: UploadFileToCaseImpl

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        useCase = UploadFileToCaseImpl(
            eIdAvRepository = mockEIdAvRepository,
            createAutoVerificationDPoP = mockCreateAutoVerificationDPoP,
        )

        coEvery {
            mockEIdAvRepository.uploadFileToCase(
                caseId = any(),
                fileName = any(),
                contentType = any(),
                documentData = any(),
                accessToken = any(),
                dpop = any(),
            )
        } returns Ok(Unit)

        coEvery {
            mockEIdAvRepository.getUploadFileUrl(any())
        } returns uploadFileUrl

        coEvery {
            mockCreateAutoVerificationDPoP(url = any(), accessToken = any(), requestBody = any())
        } returns Ok(avDPoP)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `A successful upload returns Ok`() = runTest {
        useCase(
            caseId = caseId,
            accessToken = accessToken,
            fileName = fileName,
            contentType = contentType,
            documentData = documentData,
        ).assertOk()

        coVerifyOrder {
            mockCreateAutoVerificationDPoP(
                url = uploadFileUrl,
                accessToken = accessToken,
                requestBody = documentData,
            )

            mockEIdAvRepository.uploadFileToCase(
                caseId = caseId,
                fileName = fileName,
                contentType = contentType,
                documentData = documentData,
                accessToken = accessToken,
                dpop = avDPoP,
            )
        }
    }

    @Test
    fun `An Av DPoP error is propagated`() = runTest {
        val exception = Exception("error in dpop")
        coEvery {
            mockCreateAutoVerificationDPoP(url = any(), accessToken = any(), requestBody = any())
        } returns Err(EIdRequestError.Unexpected(exception))

        val result = useCase(
            caseId = caseId,
            accessToken = accessToken,
            fileName = fileName,
            contentType = contentType,
            documentData = documentData,
        )

        val error = result.assertErrorType(EIdRequestError.Unexpected::class)
        assertEquals(exception, error.cause)
    }

    @Test
    fun `A repository error is propagated`() = runTest {
        coEvery {
            mockEIdAvRepository.uploadFileToCase(
                caseId = any(),
                fileName = any(),
                contentType = any(),
                documentData = any(),
                accessToken = any(),
                dpop = any(),
            )
        } returns Err(EIdRequestError.DeclinedProcessData(""))

        val result = useCase(
            caseId = caseId,
            accessToken = accessToken,
            fileName = fileName,
            contentType = contentType,
            documentData = documentData,
        )

        result.assertErrorType(EIdRequestError.DeclinedProcessData::class)
    }
}

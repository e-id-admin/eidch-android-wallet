package ch.admin.foitt.wallet.feature.eIdRequestVerification

import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.model.FileUploadConfig
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.SubmitCaseId
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.implementation.SubmitCaseIdImpl
import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestFile
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EidRequestSubmitFile
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdAvRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestCaseRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestFileRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.CreateAutoVerificationDPoP
import ch.admin.foitt.wallet.util.SafeJsonTestInstance
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URL

class SubmitCaseIdImplTest {
    @MockK
    private lateinit var mockEIdAvRepository: EIdAvRepository

    @MockK
    private lateinit var mockEIdRequestCaseRepository: EIdRequestCaseRepository

    @MockK
    private lateinit var mockEIdRequestFileRepository: EIdRequestFileRepository

    @MockK
    private lateinit var mockCreateAutoVerificationDPoP: CreateAutoVerificationDPoP

    @MockK
    private lateinit var mockEidRequestFile: EIdRequestFile

    private val submitCaseUrl = URL("https://example.com")

    private lateinit var eidRequestFiles: List<EIdRequestFile>

    private val fileName = FileUploadConfig.filesToUpload.first().fileName
    private val fileData = byteArrayOf(1, 2, 3)
    private val fileHash = "A5BYxvLAy0ksUzsKTRTvd8wPeKvMztUofYShogEc+4E="
    private val submitFile = EidRequestSubmitFile(
        filename = fileName,
        hash = fileHash,
    )
    private val submitFiles = listOf(submitFile)

    lateinit var submitCaseId: SubmitCaseId

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        submitCaseId = SubmitCaseIdImpl(
            eIdAvRepository = mockEIdAvRepository,
            eIdRequestCaseRepository = mockEIdRequestCaseRepository,
            eIdRequestFileRepository = mockEIdRequestFileRepository,
            createAutoVerificationDPoP = mockCreateAutoVerificationDPoP,
            safeJson = SafeJsonTestInstance.safeJson,
        )

        setupDefaultMocks()
    }

    private fun setupDefaultMocks() {
        eidRequestFiles = listOf(mockEidRequestFile)

        coEvery {
            mockEidRequestFile.fileName
        } returns fileName

        coEvery {
            mockEidRequestFile.data
        } returns fileData

        coEvery {
            mockEIdAvRepository.submitCase(
                caseId = any(),
                accessToken = any(),
                dpop = any(),
                files = any(),
            )
        } returns Ok(Unit)

        coEvery {
            mockCreateAutoVerificationDPoP(
                url = any(),
                accessToken = any(),
                requestBody = any(),
            )
        } returns Ok(DPOP)

        coEvery {
            mockEIdRequestCaseRepository.setFilesSubmitted(any())
        } returns Ok(Unit)

        coEvery {
            mockEIdRequestFileRepository.getEIdRequestFilesByCaseId(any())
        } returns Ok(eidRequestFiles)

        coEvery {
            mockEIdAvRepository.getSubmitCaseUrl(any())
        } returns submitCaseUrl
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Successfully submit a case returns an Ok`() = runTest {
        submitCaseId(caseId = CASE_ID, accessToken = ACCESS_TOKEN).assertOk()

        coVerifyOrder {
            mockEIdRequestFileRepository.getEIdRequestFilesByCaseId(CASE_ID)

            mockEIdAvRepository.submitCase(
                caseId = CASE_ID,
                accessToken = ACCESS_TOKEN,
                dpop = DPOP,
                files = submitFiles,
            )
            mockEIdRequestCaseRepository.setFilesSubmitted(caseId = CASE_ID)
        }
    }

    @Test
    fun `Error when creating the Av DPoP is propagated`() = runTest {
        val exception = Exception("error in dpop")
        coEvery {
            mockCreateAutoVerificationDPoP(
                url = any(),
                accessToken = any(),
                requestBody = any(),
            )
        } returns Err(EIdRequestError.Unexpected(exception))

        submitCaseId(caseId = CASE_ID, accessToken = ACCESS_TOKEN).assertErrorType(EIdRequestError.Unexpected::class)
    }

    @Test
    fun `Error when submitting a case from the repository is propagated`() = runTest {
        val exception = Exception("error in db")
        coEvery {
            mockEIdAvRepository.submitCase(
                caseId = any(),
                accessToken = any(),
                dpop = any(),
                files = any(),
            )
        } returns Err(EIdRequestError.Unexpected(exception))

        submitCaseId(caseId = CASE_ID, accessToken = ACCESS_TOKEN).assertErrorType(EIdRequestError.Unexpected::class)
    }

    @Test
    fun `Error when setting files submitted is propagated`() = runTest {
        val exception = Exception("error in db")
        coEvery {
            mockEIdRequestCaseRepository.setFilesSubmitted(any(), any())
        } returns Err(EIdRequestError.Unexpected(exception))

        submitCaseId(caseId = CASE_ID, accessToken = ACCESS_TOKEN).assertErrorType(EIdRequestError.Unexpected::class)
    }

    private companion object {
        const val CASE_ID = "caseId"
        const val ACCESS_TOKEN = "accessToken"
        const val DPOP = "DPoP"
    }
}

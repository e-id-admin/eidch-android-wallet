package ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.DigestAlgorithm
import ch.admin.foitt.openid4vc.utils.createBase64Digest
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.model.FileUploadConfig
import ch.admin.foitt.wallet.feature.eIdRequestVerification.domain.usecase.SubmitCaseId
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.AvRepositoryError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.AvSubmitCaseError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.CreateAutoVerificationDPoPError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestCaseRepositoryError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestFileRepositoryError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EidRequestSubmitFile
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.toAvSubmitCaseError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdAvRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestCaseRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestFileRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.CreateAutoVerificationDPoP
import ch.admin.foitt.wallet.platform.utils.JsonParsingError
import ch.admin.foitt.wallet.platform.utils.SafeJson
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.binding
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class SubmitCaseIdImpl @Inject constructor(
    private val eIdAvRepository: EIdAvRepository,
    private val eIdRequestCaseRepository: EIdRequestCaseRepository,
    private val eIdRequestFileRepository: EIdRequestFileRepository,
    private val createAutoVerificationDPoP: CreateAutoVerificationDPoP,
    private val safeJson: SafeJson,
) : SubmitCaseId {
    override suspend fun invoke(
        caseId: String,
        accessToken: String
    ): Result<Unit, AvSubmitCaseError> = coroutineBinding {
        val files = generateFileList(caseId).bind()
        val requestBody: ByteArray = generateRequestBody(files).bind()

        val dPop = createAutoVerificationDPoP(
            url = eIdAvRepository.getSubmitCaseUrl(caseId),
            accessToken = accessToken,
            requestBody = requestBody,
        ).mapError(CreateAutoVerificationDPoPError::toAvSubmitCaseError).bind()

        eIdAvRepository.submitCase(
            caseId = caseId,
            accessToken = accessToken,
            dpop = dPop,
            files = files,
        ).mapError(AvRepositoryError::toAvSubmitCaseError).bind()

        eIdRequestCaseRepository.setFilesSubmitted(
            caseId = caseId,
        ).mapError(EIdRequestCaseRepositoryError::toAvSubmitCaseError).bind()
    }

    private suspend fun generateFileList(
        caseId: String,
    ): Result<List<EidRequestSubmitFile>, AvSubmitCaseError> = coroutineBinding {
        val dbFiles = eIdRequestFileRepository.getEIdRequestFilesByCaseId(caseId)
            .mapError(EIdRequestFileRepositoryError::toAvSubmitCaseError).bind()

        FileUploadConfig.filesToUpload.mapNotNull { file ->
            dbFiles.firstOrNull { it.fileName == file.fileName }?.let { dbFile ->
                val fileHash = dbFile.data.createBase64Digest(DigestAlgorithm.SHA256)
                EidRequestSubmitFile(
                    filename = file.serverFileName,
                    hash = fileHash,
                )
            }
        }
    }

    private fun generateRequestBody(files: List<EidRequestSubmitFile>): Result<ByteArray, AvSubmitCaseError> = binding {
        val bodyJson = safeJson.safeEncodeObjectToString(files)
            .mapError(JsonParsingError::toAvSubmitCaseError).bind()
        bodyJson.toByteArray(Charsets.ISO_8859_1)
    }
}

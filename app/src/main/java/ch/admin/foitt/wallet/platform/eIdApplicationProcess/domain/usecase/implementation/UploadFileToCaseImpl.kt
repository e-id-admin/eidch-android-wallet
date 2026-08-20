package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.CreateAutoVerificationDPoPError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.toAvRepositoryError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdAvRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.CreateAutoVerificationDPoP
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.UploadFileToCase
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import io.ktor.http.ContentType
import javax.inject.Inject

class UploadFileToCaseImpl @Inject constructor(
    private val eIdAvRepository: EIdAvRepository,
    private val createAutoVerificationDPoP: CreateAutoVerificationDPoP,
) : UploadFileToCase {
    override suspend fun invoke(
        caseId: String,
        fileName: String,
        accessToken: String,
        contentType: ContentType,
        documentData: ByteArray,
    ) = coroutineBinding {
        val dPoP = createAutoVerificationDPoP(
            url = eIdAvRepository.getUploadFileUrl(caseId),
            accessToken = accessToken,
            requestBody = documentData,
        ).mapError(CreateAutoVerificationDPoPError::toAvRepositoryError).bind()

        eIdAvRepository.uploadFileToCase(
            caseId = caseId,
            fileName = fileName,
            contentType = contentType,
            documentData = documentData,
            accessToken = accessToken,
            dpop = dPoP,
        ).bind()
    }
}

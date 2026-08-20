package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.AvRepositoryError
import com.github.michaelbull.result.Result
import io.ktor.http.ContentType

interface UploadFileToCase {
    suspend operator fun invoke(
        caseId: String,
        fileName: String,
        accessToken: String,
        contentType: ContentType,
        documentData: ByteArray,
    ): Result<Unit, AvRepositoryError>
}

package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository

import ch.admin.foitt.openid4vc.domain.model.TokenType
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.AvRepositoryError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EidRequestSubmitFile
import com.github.michaelbull.result.Result
import io.ktor.http.ContentType
import java.net.URL

interface EIdAvRepository {
    suspend fun uploadFileToCase(
        caseId: String,
        fileName: String,
        contentType: ContentType,
        documentData: ByteArray,
        accessToken: String,
        dpop: String?,
        tokenType: TokenType = TokenType.DPOP,
    ): Result<Unit, AvRepositoryError>

    suspend fun submitCase(
        caseId: String,
        accessToken: String,
        dpop: String?,
        files: List<EidRequestSubmitFile>,
        tokenType: TokenType = TokenType.DPOP,
    ): Result<Unit, AvRepositoryError>

    fun getUploadFileUrl(caseId: String): URL

    fun getSubmitCaseUrl(caseId: String): URL
}

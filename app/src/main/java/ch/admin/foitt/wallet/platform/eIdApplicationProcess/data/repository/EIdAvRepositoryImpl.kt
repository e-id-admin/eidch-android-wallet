package ch.admin.foitt.wallet.platform.eIdApplicationProcess.data.repository

import ch.admin.foitt.openid4vc.di.OpenId4VcModule.Companion.NAMED_DEFAULT_HTTP_CLIENT
import ch.admin.foitt.openid4vc.domain.model.TokenType
import ch.admin.foitt.openid4vc.utils.authorizationHeader
import ch.admin.foitt.openid4vc.utils.dpopHeader
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.AvRepositoryError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EidRequestSubmitFile
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.toAvRepositoryError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdAvRepository
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentLength
import io.ktor.http.contentType
import java.net.URL
import javax.inject.Inject
import javax.inject.Named

class EIdAvRepositoryImpl @Inject constructor(
    @param:Named(NAMED_DEFAULT_HTTP_CLIENT) private val httpClient: HttpClient,
    private val environmentSetupRepository: EnvironmentSetupRepository
) : EIdAvRepository {
    override suspend fun uploadFileToCase(
        caseId: String,
        fileName: String,
        contentType: ContentType,
        documentData: ByteArray,
        accessToken: String,
        dpop: String?,
        tokenType: TokenType,
    ): Result<Unit, AvRepositoryError> = runSuspendCatching<Unit> {
        httpClient.post(getUploadFileUrl(caseId)) {
            authorizationHeader(tokenType, accessToken)
            dpop?.let { dpopHeader(dpop) }
            header(HttpHeaders.ContentDisposition, "attachment; filename=\"${fileName}\"")
            contentLength()
            contentType(contentType)
            setBody(documentData)
        }
    }.mapError { throwable ->
        throwable.toAvRepositoryError("uploadFileToCase error")
    }

    override suspend fun submitCase(
        caseId: String,
        accessToken: String,
        dpop: String?,
        files: List<EidRequestSubmitFile>,
        tokenType: TokenType,
    ): Result<Unit, AvRepositoryError> = runSuspendCatching<Unit> {
        httpClient.post(getSubmitCaseUrl(caseId)) {
            authorizationHeader(tokenType, accessToken)
            dpop?.let { dpopHeader(dpop) }
            contentType(ContentType.Application.Json)
            setBody(files)
        }
    }.mapError { throwable ->
        throwable.toAvRepositoryError("submit case error")
    }

    override fun getUploadFileUrl(caseId: String) = URL(environmentSetupRepository.avBackendUrl + "cases/v2/$caseId/files")

    override fun getSubmitCaseUrl(caseId: String) = URL(environmentSetupRepository.avBackendUrl + "cases/v2/$caseId/submit")
}

package ch.admin.foitt.openid4vc.data

import ch.admin.foitt.openid4vc.di.OpenId4VcModule.Companion.NAMED_DEFAULT_HTTP_CLIENT
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationResponseConfig
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationResponseErrorBody
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationResponseResponse
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationResponseType
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.FetchPresentationRequestError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.SubmitAnyCredentialPresentationError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.SubmitPresentationErrorError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.toSubmitAnyCredentialPresentationError
import ch.admin.foitt.openid4vc.domain.repository.PresentationRequestRepository
import ch.admin.foitt.openid4vc.utils.Constants
import ch.admin.foitt.openid4vc.utils.ContentType
import ch.admin.foitt.openid4vc.utils.JsonParsingError
import ch.admin.foitt.openid4vc.utils.SafeJson
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.recoverIf
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.contentType
import io.ktor.http.parameters
import timber.log.Timber
import java.io.IOException
import java.net.URL
import javax.inject.Inject
import javax.inject.Named

internal class PresentationRequestRepositoryImpl @Inject constructor(
    @param:Named(NAMED_DEFAULT_HTTP_CLIENT) private val httpClient: HttpClient,
    private val safeJson: SafeJson
) : PresentationRequestRepository {
    override suspend fun fetchPresentationRequest(url: URL) = runSuspendCatching<String> {
        httpClient.get(url) {
            contentType(ContentType.applicationJwt)
        }.body()
    }.mapError(Throwable::toFetchPresentationRequestError)

    override suspend fun submitPresentation(
        url: URL,
        authorizationResponseConfig: AuthorizationResponseConfig,
    ): Result<AuthorizationResponseResponse, SubmitAnyCredentialPresentationError> = coroutineBinding {
        runSuspendCatching<AuthorizationResponseResponse> {
            val response = httpClient.submitForm(
                url = url.toExternalForm(),
                formParameters = parameters {
                    authorizationResponseConfig.params.forEach { append(it.key.jsonName, it.value) }
                }
            ) {
                header(
                    Constants.SWIYU_API_VERSION_HEADER,
                    when (authorizationResponseConfig.type) {
                        AuthorizationResponseType.DCQL -> Constants.SWIYU_API_VERSION_2
                    }
                )
            }
            val payload = response.bodyAsText()
            if (payload.isBlank()) {
                AuthorizationResponseResponse()
            } else {
                safeJson.safeDecodeStringTo<AuthorizationResponseResponse>(payload)
                    .mapError(JsonParsingError::toSubmitAnyCredentialPresentationError).bind()
            }
        }.recoverIf(
            predicate = { it is ResponseException },
            transform = { throwable ->
                // Any http response means the data reached the verifier, so the submission is
                // considered successful, independently of status code and body
                Timber.d(
                    t = throwable,
                    message = "Ignoring a ${(throwable as ResponseException).response.status} response after submitting the presentation",
                )
                AuthorizationResponseResponse(redirectUri = null)
            }
        ).mapError { throwable ->
            throwable.toSubmitAnyCredentialPresentationError()
        }.bind()
    }

    override suspend fun submitPresentationError(
        url: String,
        body: AuthorizationResponseErrorBody,
    ) = runSuspendCatching<AuthorizationResponseResponse> {
        httpClient.submitForm(
            url = url,
            formParameters = parameters {
                append("error", body.error.key)
                body.errorDescription?.let { append("error_description", body.errorDescription) }
                body.state?.let { append("state", body.state) }
            }
        ).body()
    }.mapError(Throwable::toSubmitPresentationErrorError)
}

private fun Throwable.toFetchPresentationRequestError(): FetchPresentationRequestError = when (this) {
    is IOException -> PresentationRequestError.NetworkError
    else -> PresentationRequestError.Unexpected(this)
}

private fun Throwable.toSubmitAnyCredentialPresentationError(): SubmitAnyCredentialPresentationError =
    when (this) {
        is ConnectTimeoutException -> PresentationRequestError.NetworkError
        is SocketTimeoutException -> PresentationRequestError.SocketTimeoutError
        is IOException -> PresentationRequestError.NetworkError
        else -> PresentationRequestError.Unexpected(this)
    }

private fun Throwable.toSubmitPresentationErrorError(): SubmitPresentationErrorError = when (this) {
    is IOException -> PresentationRequestError.NetworkError
    else -> PresentationRequestError.Unexpected(this)
}

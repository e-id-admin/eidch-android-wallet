package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.FetchPresentationRequestError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.JsonPresentationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.JwtPresentationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.toFetchPresentationRequestError
import ch.admin.foitt.openid4vc.domain.repository.PresentationRequestRepository
import ch.admin.foitt.openid4vc.domain.usecase.FetchPresentationRequest
import ch.admin.foitt.openid4vc.utils.JsonParsingError
import ch.admin.foitt.openid4vc.utils.SafeJson
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapBoth
import com.github.michaelbull.result.mapError
import com.nimbusds.jwt.SignedJWT
import java.net.URL
import javax.inject.Inject

internal class FetchPresentationRequestImpl @Inject constructor(
    private val safeJson: SafeJson,
    private val presentationRequestRepository: PresentationRequestRepository,
) : FetchPresentationRequest {
    override suspend fun invoke(url: URL): Result<PresentationRequest, FetchPresentationRequestError> = coroutineBinding {
        val presentationRequestPayload = presentationRequestRepository.fetchPresentationRequest(url)
            .bind()

        // First try if this if a valid Jwt, then fallback to try as a plain Json
        val presentationRequestJwt = runSuspendCatching {
            SignedJWT.parse(presentationRequestPayload)
        }.mapError(Throwable::toFetchPresentationRequestError)

        val presentationRequest = presentationRequestJwt.mapBoth(
            success = { mapToJwtPresentationRequest(presentationRequestJwt.value).bind() },
            failure = { mapToJsonPresentationRequest(presentationRequestPayload).bind() }
        )

        presentationRequest
    }

    private suspend fun mapToJwtPresentationRequest(
        presentationRequestJWT: SignedJWT
    ): Result<JwtPresentationRequest, FetchPresentationRequestError> = coroutineBinding {
        val payload = presentationRequestJWT.payload
            ?: Err(PresentationRequestError.Unexpected(IllegalArgumentException("payload must not be null"))).bind<JwtPresentationRequest>()
        JwtPresentationRequest(
            presentationRequest = payload.toString().decodeToPresentationRequest().bind(),
            signedJWT = presentationRequestJWT
        )
    }

    private suspend fun mapToJsonPresentationRequest(requestPayload: String) = coroutineBinding {
        JsonPresentationRequest(
            presentationRequest = requestPayload.decodeToPresentationRequest().bind()
        )
    }

    private fun String.decodeToPresentationRequest() =
        safeJson.safeDecodeStringTo<PresentationRequest>(
            string = this,
        ).mapError(JsonParsingError::toFetchPresentationRequestError)
}

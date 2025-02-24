package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.FetchPresentationRequestError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestContainer
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.toFetchPresentationRequestError
import ch.admin.foitt.openid4vc.domain.repository.PresentationRequestRepository
import ch.admin.foitt.openid4vc.domain.usecase.FetchPresentationRequest
import ch.admin.foitt.openid4vc.utils.JsonParsingError
import ch.admin.foitt.openid4vc.utils.SafeJson
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapBoth
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.onFailure
import timber.log.Timber
import java.net.URL
import javax.inject.Inject

internal class FetchPresentationRequestImpl @Inject constructor(
    private val safeJson: SafeJson,
    private val presentationRequestRepository: PresentationRequestRepository,
) : FetchPresentationRequest {
    override suspend fun invoke(url: URL): Result<PresentationRequestContainer, FetchPresentationRequestError> = coroutineBinding {
        val presentationRequestPayload = presentationRequestRepository.fetchPresentationRequest(url)
            .bind()

        // First try if this if a valid Jwt, then fallback to try as a plain Json
        val presentationRequestJwt = runSuspendCatching {
            Jwt(presentationRequestPayload)
        }.onFailure {
            Timber.d(message = "Presentation is not a valid Jwt")
        }

        val presentationRequestContainer = presentationRequestJwt.mapBoth(
            success = { PresentationRequestContainer.Jwt(jwt = it) },
            failure = { mapToJsonPresentationRequest(presentationRequestPayload).bind() }
        )

        presentationRequestContainer
    }

    private suspend fun mapToJsonPresentationRequest(requestPayload: String) = coroutineBinding {
        val jsonElement = safeJson.safeDecodeToJsonObject(requestPayload)
            .mapError(JsonParsingError::toFetchPresentationRequestError).bind()
        PresentationRequestContainer.Json(json = jsonElement)
    }
}

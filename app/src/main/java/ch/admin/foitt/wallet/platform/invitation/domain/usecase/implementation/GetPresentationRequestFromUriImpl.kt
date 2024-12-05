package ch.admin.foitt.wallet.platform.invitation.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.FetchPresentationRequestError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequest
import ch.admin.foitt.openid4vc.domain.usecase.FetchPresentationRequest
import ch.admin.foitt.wallet.platform.invitation.domain.model.GetPresentationRequestError
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError
import ch.admin.foitt.wallet.platform.invitation.domain.model.toGetPresentationRequestError
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.GetPresentationRequestFromUri
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import java.net.URI
import javax.inject.Inject

internal class GetPresentationRequestFromUriImpl @Inject constructor(
    private val fetchPresentationRequest: FetchPresentationRequest,
) : GetPresentationRequestFromUri {
    override suspend fun invoke(uri: URI): Result<PresentationRequest, GetPresentationRequestError> = coroutineBinding {
        val url = runSuspendCatching {
            uri.toURL()
        }.mapError {
            InvitationError.InvalidUri("Invalid uri: $uri")
        }.bind()

        val presentationRequest = fetchPresentationRequest(url).mapError(
            FetchPresentationRequestError::toGetPresentationRequestError
        ).bind()

        presentationRequest
    }
}

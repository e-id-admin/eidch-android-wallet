package ch.admin.foitt.wallet.platform.invitation.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.FetchPresentationRequestError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestContainer
import ch.admin.foitt.openid4vc.domain.usecase.FetchPresentationRequest
import ch.admin.foitt.wallet.platform.invitation.domain.model.GetPresentationRequestError
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError
import ch.admin.foitt.wallet.platform.invitation.domain.model.toGetPresentationRequestError
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.GetPresentationRequestFromUri
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import timber.log.Timber
import java.net.URI
import javax.inject.Inject

internal class GetPresentationRequestFromUriImpl @Inject constructor(
    private val fetchPresentationRequest: FetchPresentationRequest,
) : GetPresentationRequestFromUri {
    override suspend fun invoke(uri: URI): Result<PresentationRequestContainer, GetPresentationRequestError> = coroutineBinding {
        val url = runSuspendCatching {
            uri.toURL()
        }.mapError {
            Timber.d("Invalid uri: $uri")
            InvitationError.InvalidUri
        }.bind()

        val presentationRequest = fetchPresentationRequest(url).mapError(
            FetchPresentationRequestError::toGetPresentationRequestError
        ).bind()

        presentationRequest
    }
}

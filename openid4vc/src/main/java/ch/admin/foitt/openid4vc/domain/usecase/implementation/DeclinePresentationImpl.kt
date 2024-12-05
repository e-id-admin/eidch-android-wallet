package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestErrorBody
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.SubmitPresentationErrorError
import ch.admin.foitt.openid4vc.domain.repository.PresentationRequestRepository
import ch.admin.foitt.openid4vc.domain.usecase.DeclinePresentation
import com.github.michaelbull.result.Result
import javax.inject.Inject

internal class DeclinePresentationImpl @Inject constructor(
    private val presentationRequestRepository: PresentationRequestRepository,
) : DeclinePresentation {
    override suspend fun invoke(url: String, reason: PresentationRequestErrorBody.ErrorType): Result<Unit, SubmitPresentationErrorError> {
        val body = PresentationRequestErrorBody(reason)
        return presentationRequestRepository.submitPresentationError(url, body)
    }
}

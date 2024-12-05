package ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequest
import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.GetPresentationRequestFlowError
import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.PresentationRequestUi
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.PresentationRequestField
import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.Flow

interface GetPresentationRequestFlow {
    operator fun invoke(
        id: Long,
        requestedFields: List<PresentationRequestField>,
        presentationRequest: PresentationRequest,
    ): Flow<Result<PresentationRequestUi, GetPresentationRequestFlowError>>
}

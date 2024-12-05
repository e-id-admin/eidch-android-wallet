package ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase

import androidx.annotation.CheckResult
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequest
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.ProcessPresentationRequestError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.ProcessPresentationRequestResult
import com.github.michaelbull.result.Result

fun interface ProcessPresentationRequest {
    @CheckResult
    suspend operator fun invoke(
        presentationRequest: PresentationRequest
    ): Result<ProcessPresentationRequestResult, ProcessPresentationRequestError>
}
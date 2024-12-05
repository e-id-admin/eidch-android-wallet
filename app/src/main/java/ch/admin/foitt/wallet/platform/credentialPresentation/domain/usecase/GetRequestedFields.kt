package ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase

import androidx.annotation.CheckResult
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.InputDescriptor
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.GetRequestedFieldsError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.PresentationRequestField
import com.github.michaelbull.result.Result

fun interface GetRequestedFields {
    @CheckResult
    suspend operator fun invoke(
        credentialJson: String,
        inputDescriptors: List<InputDescriptor>
    ): Result<List<PresentationRequestField>, GetRequestedFieldsError>
}

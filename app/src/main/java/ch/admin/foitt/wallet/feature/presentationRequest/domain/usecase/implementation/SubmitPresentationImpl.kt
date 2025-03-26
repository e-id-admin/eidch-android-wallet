package ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.SubmitAnyCredentialPresentationError
import ch.admin.foitt.openid4vc.domain.usecase.SubmitAnyCredentialPresentation
import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.SubmitPresentationError
import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.toSubmitPresentationError
import ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase.SubmitPresentation
import ch.admin.foitt.wallet.platform.credential.domain.model.GetAnyCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GetAnyCredential
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CompatibleCredential
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class SubmitPresentationImpl @Inject constructor(
    private val getAnyCredential: GetAnyCredential,
    private val submitAnyCredentialPresentation: SubmitAnyCredentialPresentation,
) : SubmitPresentation {
    override suspend fun invoke(
        presentationRequest: PresentationRequest,
        compatibleCredential: CompatibleCredential,
    ): Result<Unit, SubmitPresentationError> =
        getAnyCredential(compatibleCredential.credentialId)
            .mapError(GetAnyCredentialError::toSubmitPresentationError)
            .andThen { credential ->
                submitAnyCredentialPresentation(
                    anyCredential = credential,
                    requestedFields = compatibleCredential.requestedFields.map { it.key },
                    presentationRequest = presentationRequest,
                ).mapError(SubmitAnyCredentialPresentationError::toSubmitPresentationError)
            }
}

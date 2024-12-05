package ch.admin.foitt.openid4vc.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.CreateAnyVerifiablePresentationError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequest
import com.github.michaelbull.result.Result

internal fun interface CreateAnyVerifiablePresentation {
    suspend operator fun invoke(
        anyCredential: AnyCredential,
        requestedFields: List<String>,
        presentationRequest: PresentationRequest,
    ): Result<String, CreateAnyVerifiablePresentationError>
}

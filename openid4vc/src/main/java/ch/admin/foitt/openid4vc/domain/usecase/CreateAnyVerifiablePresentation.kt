package ch.admin.foitt.openid4vc.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.CreateAnyVerifiablePresentationError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationFlowContext
import com.github.michaelbull.result.Result

internal interface CreateAnyVerifiablePresentation {
    suspend operator fun invoke(
        anyCredential: AnyCredential,
        authorizationRequest: AuthorizationRequest,
        presentationContext: PresentationFlowContext
    ): Result<String, CreateAnyVerifiablePresentationError>
}

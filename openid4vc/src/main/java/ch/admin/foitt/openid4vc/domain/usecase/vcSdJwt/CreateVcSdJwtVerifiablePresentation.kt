package ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt

import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBinding
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.CreateVcSdJwtVerifiablePresentationError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationFlowContext
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import com.github.michaelbull.result.Result

internal interface CreateVcSdJwtVerifiablePresentation {
    suspend operator fun invoke(
        credential: VcSdJwtCredential,
        keyBinding: KeyBinding?,
        authorizationRequest: AuthorizationRequest,
        presentationContext: PresentationFlowContext
    ): Result<String, CreateVcSdJwtVerifiablePresentationError>
}

package ch.admin.foitt.openid4vc.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationResponseConfig
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.GetAuthorizationResponseConfigError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationFlowContext
import com.github.michaelbull.result.Result

interface GetAuthorizationResponseConfig {
    suspend operator fun invoke(
        anyCredential: AnyCredential,
        authorizationRequest: AuthorizationRequest,
        presentationContext: PresentationFlowContext
    ): Result<AuthorizationResponseConfig, GetAuthorizationResponseConfigError>
}

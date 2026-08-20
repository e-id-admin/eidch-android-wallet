package ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationRequest
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.FetchTrustForVerificationError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustCheckResult
import com.github.michaelbull.result.Result

fun interface FetchTrustForVerification {
    suspend operator fun invoke(
        authorizationRequest: AuthorizationRequest
    ): Result<TrustCheckResult, FetchTrustForVerificationError>
}

package ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationRequest
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.FetchAndCacheVerifierDisplayDataError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.VerificationProcessType
import com.github.michaelbull.result.Result

fun interface FetchAndCacheVerifierDisplayData {
    suspend operator fun invoke(
        authorizationRequest: AuthorizationRequest,
        verificationProcessType: VerificationProcessType,
        verifierAttestationTrusted: Boolean?,
    ): Result<Unit, FetchAndCacheVerifierDisplayDataError>
}

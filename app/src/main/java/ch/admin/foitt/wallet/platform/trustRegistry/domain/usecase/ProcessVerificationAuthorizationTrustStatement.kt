package ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointer
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.PresentationRequestWithRaw
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ProcessVerificationAuthorizationTrustStatementError
import com.github.michaelbull.result.Result

interface ProcessVerificationAuthorizationTrustStatement {
    suspend operator fun invoke(
        authorizationRequestWithRaw: PresentationRequestWithRaw,
        requestedClaims: List<ClaimsPathPointer>,
    ): Result<Unit, ProcessVerificationAuthorizationTrustStatementError>
}

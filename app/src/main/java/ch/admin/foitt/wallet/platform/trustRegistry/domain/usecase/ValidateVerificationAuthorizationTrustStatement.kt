package ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ValidateVerificationAuthorizationTrustStatementError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VerificationAuthorizationTrustStatement
import com.github.michaelbull.result.Result

interface ValidateVerificationAuthorizationTrustStatement {
    suspend operator fun invoke(
        trustStatement: Jwt,
        actorDid: String,
    ): Result<VerificationAuthorizationTrustStatement, ValidateVerificationAuthorizationTrustStatementError>
}

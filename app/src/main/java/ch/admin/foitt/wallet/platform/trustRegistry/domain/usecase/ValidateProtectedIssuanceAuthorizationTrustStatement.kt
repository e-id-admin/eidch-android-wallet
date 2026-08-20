package ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ProtectedIssuanceAuthorizationTrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ValidateProtectedIssuanceAuthorizationTrustStatementError
import com.github.michaelbull.result.Result

interface ValidateProtectedIssuanceAuthorizationTrustStatement {
    suspend operator fun invoke(
        trustStatement: Jwt,
        actorDid: String,
    ): Result<ProtectedIssuanceAuthorizationTrustStatement, ValidateProtectedIssuanceAuthorizationTrustStatementError>
}

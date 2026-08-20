package ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IdentityV2TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ValidateIdentityTrustStatementError
import com.github.michaelbull.result.Result

interface ValidateIdentityTrustStatement {
    suspend operator fun invoke(
        trustStatement: Jwt,
        actorDid: String,
    ): Result<IdentityV2TrustStatement, ValidateIdentityTrustStatementError>
}

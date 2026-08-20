package ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IdentityV2TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ProcessIdentityTrustStatementError
import com.github.michaelbull.result.Result

interface ProcessIdentityTrustStatement {
    suspend operator fun invoke(
        identityTrustStatementJwt: Jwt?,
        actorDid: String,
    ): Result<IdentityV2TrustStatement?, ProcessIdentityTrustStatementError>
}

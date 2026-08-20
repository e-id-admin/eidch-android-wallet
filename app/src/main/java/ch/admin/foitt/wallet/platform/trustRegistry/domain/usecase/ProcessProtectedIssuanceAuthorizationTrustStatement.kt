package ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ProcessProtectedIssuanceAuthorizationTrustStatementError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VcSchemaTrustStatus
import com.github.michaelbull.result.Result

interface ProcessProtectedIssuanceAuthorizationTrustStatement {
    suspend operator fun invoke(
        protectedIssuanceAuthorizationTrustStatement: Jwt?,
        actorDid: String,
        vct: String,
    ): Result<VcSchemaTrustStatus, ProcessProtectedIssuanceAuthorizationTrustStatementError>
}

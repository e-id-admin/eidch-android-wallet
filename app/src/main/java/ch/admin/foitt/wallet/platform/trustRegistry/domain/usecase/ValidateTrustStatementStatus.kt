package ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatementStatusResult
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ValidateTrustStatementStatusError
import com.github.michaelbull.result.Result

interface ValidateTrustStatementStatus {
    suspend operator fun invoke(
        trustStatement: Jwt,
    ): Result<TrustStatementStatusResult, ValidateTrustStatementStatusError>
}

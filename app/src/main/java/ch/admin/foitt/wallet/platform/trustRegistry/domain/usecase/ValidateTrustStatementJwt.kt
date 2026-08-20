package ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ValidateTrustStatementJwtError
import com.github.michaelbull.result.Result

interface ValidateTrustStatementJwt {
    suspend operator fun invoke(
        trustStatement: Jwt,
    ): Result<Unit, ValidateTrustStatementJwtError>
}

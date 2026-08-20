package ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ProtectedIssuanceTrustListStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ValidateProtectedIssuanceTrustListStatementError
import com.github.michaelbull.result.Result

interface ValidateProtectedIssuanceTrustListStatement {
    suspend operator fun invoke(
        trustStatement: Jwt,
    ): Result<ProtectedIssuanceTrustListStatement, ValidateProtectedIssuanceTrustListStatementError>
}

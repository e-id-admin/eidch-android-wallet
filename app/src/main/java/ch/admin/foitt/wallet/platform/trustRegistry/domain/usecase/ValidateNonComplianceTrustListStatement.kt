package ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.NonComplianceTrustListStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ValidateNonComplianceTrustListStatementError
import com.github.michaelbull.result.Result

interface ValidateNonComplianceTrustListStatement {
    suspend operator fun invoke(
        trustStatement: Jwt,
        actorDid: String,
    ): Result<NonComplianceTrustListStatement, ValidateNonComplianceTrustListStatementError>
}

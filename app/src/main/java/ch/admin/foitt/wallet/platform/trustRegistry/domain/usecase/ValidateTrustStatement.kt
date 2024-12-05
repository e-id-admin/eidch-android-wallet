package ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase

import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ValidateTrustStatementError
import com.github.michaelbull.result.Result

fun interface ValidateTrustStatement {
    suspend operator fun invoke(trustStatementSdJwt: String): Result<TrustStatement, ValidateTrustStatementError>
}

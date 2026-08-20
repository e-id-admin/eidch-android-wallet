package ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase

import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ProcessProtectedIssuanceTrustListStatementError
import com.github.michaelbull.result.Result

interface ProcessProtectedIssuanceTrustListStatement {
    suspend operator fun invoke(
        issuerDid: String,
        vct: String,
    ): Result<Boolean, ProcessProtectedIssuanceTrustListStatementError>
}

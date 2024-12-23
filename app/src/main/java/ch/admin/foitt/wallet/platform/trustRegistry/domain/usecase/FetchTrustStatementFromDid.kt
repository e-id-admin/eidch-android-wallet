package ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase

import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.FetchTrustStatementFromDidError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatement
import com.github.michaelbull.result.Result

fun interface FetchTrustStatementFromDid {
    suspend operator fun invoke(
        did: String,
    ): Result<TrustStatement, FetchTrustStatementFromDidError>
}

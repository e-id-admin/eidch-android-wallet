package ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.FetchTrustStatementFromDidError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.GetTrustUrlFromDidError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatementRepositoryError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.toFetchTrustStatementFromDidError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.repository.TrustStatementRepository
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.FetchTrustStatementFromDid
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.GetTrustUrlFromDid
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateTrustStatement
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.mapError
import javax.inject.Inject

internal class FetchTrustStatementFromDidImpl @Inject constructor(
    private val getTrustUrlFromDid: GetTrustUrlFromDid,
    private val trustStatementRepository: TrustStatementRepository,
    private val validateTrustStatement: ValidateTrustStatement,
) : FetchTrustStatementFromDid {
    override suspend operator fun invoke(
        did: String,
    ): Result<TrustStatement, FetchTrustStatementFromDidError> = coroutineBinding {
        runSuspendCatching {
            val url = getTrustUrlFromDid(did)
                .mapError(GetTrustUrlFromDidError::toFetchTrustStatementFromDidError)
                .bind()

            val trustStatementsRaw = trustStatementRepository.fetchTrustStatements(
                url = url
            )
                .mapError(TrustStatementRepositoryError::toFetchTrustStatementFromDidError)
                .bind()

            val trustStatement = trustStatementsRaw.firstNotNullOf {
                val statement = validateTrustStatement(it)
                statement.getOrElse { null }
            }

            trustStatement
        }.mapError { throwable ->
            throwable.toFetchTrustStatementFromDidError("FetchTrustStatementFromDid error")
        }.bind()
    }
}

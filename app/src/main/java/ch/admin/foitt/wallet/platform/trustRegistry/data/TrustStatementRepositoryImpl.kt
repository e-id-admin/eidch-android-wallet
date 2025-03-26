package ch.admin.foitt.wallet.platform.trustRegistry.data

import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatementRepositoryError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.toTrustStatementRepositoryError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.repository.TrustStatementRepository
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import java.net.URL
import javax.inject.Inject

class TrustStatementRepositoryImpl @Inject constructor(
    private val httpClient: HttpClient,
) : TrustStatementRepository {
    override suspend fun fetchTrustStatements(url: URL): Result<List<String>, TrustStatementRepositoryError> =
        runSuspendCatching<List<String>> {
            httpClient.get(url).body()
        }.mapError { throwable ->
            throwable.toTrustStatementRepositoryError("TrustStatementRepository error")
        }
}

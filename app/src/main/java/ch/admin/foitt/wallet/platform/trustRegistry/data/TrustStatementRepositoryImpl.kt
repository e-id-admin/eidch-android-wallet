package ch.admin.foitt.wallet.platform.trustRegistry.data

import ch.admin.foitt.openid4vc.di.OpenId4VcModule.Companion.NAMED_DEFAULT_HTTP_CLIENT
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatementRepositoryError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.toUnexpected
import ch.admin.foitt.wallet.platform.trustRegistry.domain.repository.TrustStatementRepository
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import java.net.URL
import javax.inject.Inject
import javax.inject.Named

class TrustStatementRepositoryImpl @Inject constructor(
    @param:Named(NAMED_DEFAULT_HTTP_CLIENT) private val httpClient: HttpClient,
) : TrustStatementRepository {
    override suspend fun fetchTrustStatements(url: URL): Result<List<String>, TrustStatementRepositoryError> =
        runSuspendCatching<List<String>> {
            httpClient.get(url).body()
        }.mapError(Throwable::toUnexpected)

    override suspend fun fetchProtectedIssuanceTrustListStatement(
        trustRegistryDomain: String,
    ): Result<Jwt, TrustStatementRepositoryError> = runSuspendCatching {
        val url = URL("https://$trustRegistryDomain/api/v2/protected-issuance-trust-list")
        val response = httpClient.get(url)
        Jwt(response.body())
    }.mapError(Throwable::toUnexpected)

    override suspend fun fetchNonComplianceTrustListStatement(
        trustRegistryDomain: String,
    ): Result<Jwt, TrustStatementRepositoryError> = runSuspendCatching {
        val url = URL("https://$trustRegistryDomain/api/v2/non-compliance-trust-list")
        val response = httpClient.get(url)
        Jwt(response.body())
    }.mapError(Throwable::toUnexpected)
}

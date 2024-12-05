package ch.admin.foitt.wallet.platform.credentialStatus.data

import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialStatusError
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.FetchStatusFromTokenStatusListError
import ch.admin.foitt.wallet.platform.credentialStatus.domain.repository.CredentialStatusRepository
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import timber.log.Timber
import java.io.IOException
import javax.inject.Inject

class CredentialStatusRepositoryImpl @Inject constructor(
    private val httpClient: HttpClient,
) : CredentialStatusRepository {
    override suspend fun fetchTokenStatusListJwt(url: String): Result<String, FetchStatusFromTokenStatusListError> =
        runSuspendCatching<String> {
            httpClient.get(url) {
                header(HttpHeaders.Accept, "application/statuslist+jwt")
            }.body()
        }.mapError(Throwable::toFetchStatusFromStatusListError)
}

private fun Throwable.toFetchStatusFromStatusListError(): FetchStatusFromTokenStatusListError {
    Timber.e(this)
    return when (this) {
        is IOException -> CredentialStatusError.NetworkError
        else -> CredentialStatusError.Unexpected(this)
    }
}

package ch.admin.foitt.wallet.platform.oca.data

import ch.admin.foitt.wallet.platform.oca.domain.model.OcaRepositoryError
import ch.admin.foitt.wallet.platform.oca.domain.model.toOcaRepositoryError
import ch.admin.foitt.wallet.platform.oca.domain.repository.OcaRepository
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.http.ContentType
import java.net.URL
import javax.inject.Inject

class OcaRepositoryImpl @Inject constructor(
    private val httpClient: HttpClient,
) : OcaRepository {
    override suspend fun fetchVcSdJwtOcaBundle(url: URL): Result<String, OcaRepositoryError> = runSuspendCatching<String> {
        httpClient.get(url) {
            accept(ContentType.Application.Json)
        }.body()
    }.mapError { throwable ->
        throwable.toOcaRepositoryError("fetchOcaBundle error")
    }
}

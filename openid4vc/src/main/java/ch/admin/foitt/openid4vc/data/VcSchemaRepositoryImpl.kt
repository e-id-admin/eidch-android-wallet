package ch.admin.foitt.openid4vc.data

import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSchemaRepositoryError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.toVcSchemaRepositoryError
import ch.admin.foitt.openid4vc.domain.repository.VcSchemaRepository
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.contentType
import java.net.URL
import javax.inject.Inject

internal class VcSchemaRepositoryImpl @Inject constructor(
    private val httpClient: HttpClient,
) : VcSchemaRepository {

    override suspend fun fetchVcSchema(url: URL): Result<String, VcSchemaRepositoryError> = runSuspendCatching<String> {
        httpClient.get(url) {
            contentType(ContentType.Application.Json)
        }.body()
    }.mapError { throwable ->
        throwable.toVcSchemaRepositoryError(message = "Fetch vc schema error")
    }
}

package ch.admin.foitt.openid4vc.data

import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.TypeMetadataRepositoryError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.toTypeMetadataRepositoryError
import ch.admin.foitt.openid4vc.domain.repository.TypeMetadataRepository
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

internal class TypeMetadataRepositoryImpl @Inject constructor(
    private val httpClient: HttpClient,
) : TypeMetadataRepository {

    override suspend fun fetchTypeMetadata(url: URL): Result<String, TypeMetadataRepositoryError> = runSuspendCatching<String> {
        httpClient.get(url) {
            contentType(ContentType.Application.Json)
        }.body()
    }.mapError(Throwable::toTypeMetadataRepositoryError)
}

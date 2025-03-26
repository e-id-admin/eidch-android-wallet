package ch.admin.foitt.openid4vc.domain.repository

import androidx.annotation.CheckResult
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.TypeMetadataRepositoryError
import com.github.michaelbull.result.Result
import java.net.URL

interface TypeMetadataRepository {
    @CheckResult
    suspend fun fetchTypeMetadata(
        url: URL,
    ): Result<String, TypeMetadataRepositoryError>
}

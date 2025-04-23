package ch.admin.foitt.openid4vc.domain.repository

import androidx.annotation.CheckResult
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSchemaRepositoryError
import com.github.michaelbull.result.Result
import java.net.URL

interface VcSchemaRepository {
    @CheckResult
    suspend fun fetchVcSchema(
        url: URL,
    ): Result<String, VcSchemaRepositoryError>
}

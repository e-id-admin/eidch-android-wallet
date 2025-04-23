package ch.admin.foitt.wallet.platform.oca.domain.repository

import androidx.annotation.CheckResult
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaRepositoryError
import com.github.michaelbull.result.Result
import java.net.URL

interface OcaRepository {
    @CheckResult
    suspend fun fetchVcSdJwtOcaBundle(
        url: URL,
    ): Result<String, OcaRepositoryError>
}

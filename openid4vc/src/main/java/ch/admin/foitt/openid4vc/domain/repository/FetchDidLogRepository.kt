package ch.admin.foitt.openid4vc.domain.repository

import androidx.annotation.CheckResult
import ch.admin.foitt.openid4vc.domain.model.ResolveDidError
import com.github.michaelbull.result.Result
import java.net.URL

interface FetchDidLogRepository {
    @CheckResult
    suspend fun fetchDidLog(url: URL): Result<String, ResolveDidError.NetworkError>
}

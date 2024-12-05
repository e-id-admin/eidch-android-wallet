package ch.admin.foitt.wallet.platform.credentialStatus.domain.repository

import androidx.annotation.CheckResult
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.FetchStatusFromTokenStatusListError
import com.github.michaelbull.result.Result

interface CredentialStatusRepository {
    @CheckResult
    suspend fun fetchTokenStatusListJwt(url: String): Result<String, FetchStatusFromTokenStatusListError>
}

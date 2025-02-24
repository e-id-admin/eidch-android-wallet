package ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase

import androidx.annotation.CheckResult
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.TokenStatusListResponse
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.ValidateTokenStatusStatusListError
import com.github.michaelbull.result.Result

interface ValidateTokenStatusList {
    @CheckResult
    suspend operator fun invoke(
        credentialIssuer: String,
        statusListJwt: String,
        subject: String,
    ): Result<TokenStatusListResponse, ValidateTokenStatusStatusListError>
}

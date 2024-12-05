package ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase

import androidx.annotation.CheckResult
import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.TokenStatusListResponse
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.ValidateTokenStatusStatusListError
import com.github.michaelbull.result.Result

interface ValidateTokenStatusList {
    @CheckResult
    suspend operator fun invoke(
        anyCredential: AnyCredential,
        statusListJwt: String,
        subject: String,
    ): Result<TokenStatusListResponse, ValidateTokenStatusStatusListError>
}

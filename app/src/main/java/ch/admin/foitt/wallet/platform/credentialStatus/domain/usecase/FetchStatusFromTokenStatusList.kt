package ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase

import androidx.annotation.CheckResult
import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.FetchStatusFromTokenStatusListError
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.TokenStatusListProperties
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import com.github.michaelbull.result.Result

interface FetchStatusFromTokenStatusList {
    @CheckResult
    suspend operator fun invoke(
        anyCredential: AnyCredential,
        statusProperties: TokenStatusListProperties,
    ): Result<CredentialStatus, FetchStatusFromTokenStatusListError>
}

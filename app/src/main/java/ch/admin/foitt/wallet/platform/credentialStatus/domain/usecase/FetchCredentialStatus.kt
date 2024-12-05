package ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialStatusProperties
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.FetchCredentialStatusError
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import com.github.michaelbull.result.Result

fun interface FetchCredentialStatus {
    suspend operator fun invoke(
        anyCredential: AnyCredential,
        properties: CredentialStatusProperties,
    ): Result<CredentialStatus, FetchCredentialStatusError>
}

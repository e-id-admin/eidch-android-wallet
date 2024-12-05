package ch.admin.foitt.wallet.platform.credential.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.AnyCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerCredentialInformation
import ch.admin.foitt.wallet.platform.credential.domain.model.SaveCredentialError
import com.github.michaelbull.result.Result

fun interface SaveCredential {
    suspend operator fun invoke(
        issuerInfo: IssuerCredentialInformation,
        anyCredential: AnyCredential,
        credentialConfiguration: AnyCredentialConfiguration,
    ): Result<Long, SaveCredentialError>
}

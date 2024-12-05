package ch.admin.foitt.openid4vc.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOffer
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchCredentialByConfigError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.AnyCredentialConfiguration
import com.github.michaelbull.result.Result

fun interface FetchCredentialByConfig {
    suspend operator fun invoke(
        credentialConfig: AnyCredentialConfiguration,
        credentialOffer: CredentialOffer,
    ): Result<AnyCredential, FetchCredentialByConfigError>
}

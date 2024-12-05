package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOffer
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchCredentialByConfigError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchCredentialError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.AnyCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.UnknownCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.VcSdJwtCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.toFetchCredentialByConfigError
import ch.admin.foitt.openid4vc.domain.usecase.FetchCredentialByConfig
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.FetchVcSdJwtCredential
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.mapError
import javax.inject.Inject

internal class FetchCredentialByConfigImpl @Inject constructor(
    private val fetchVcSdJwtCredential: FetchVcSdJwtCredential,
) : FetchCredentialByConfig {
    override suspend fun invoke(
        credentialConfig: AnyCredentialConfiguration,
        credentialOffer: CredentialOffer,
    ): Result<AnyCredential, FetchCredentialByConfigError> =
        when (credentialConfig) {
            is VcSdJwtCredentialConfiguration -> {
                fetchVcSdJwtCredential(
                    credentialConfig = credentialConfig,
                    credentialOffer = credentialOffer,
                ).mapError(FetchCredentialError::toFetchCredentialByConfigError)
            }
            is UnknownCredentialConfiguration -> Err(CredentialOfferError.UnsupportedCredentialFormat)
        }
}

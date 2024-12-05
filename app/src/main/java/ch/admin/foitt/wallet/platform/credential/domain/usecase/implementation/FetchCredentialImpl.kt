package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOffer
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchCredentialByConfigError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchIssuerCredentialInformationError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.AnyCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.usecase.FetchCredentialByConfig
import ch.admin.foitt.openid4vc.domain.usecase.FetchIssuerCredentialInformation
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.SaveCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.toFetchCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.FetchCredential
import ch.admin.foitt.wallet.platform.credential.domain.usecase.SaveCredential
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class FetchCredentialImpl @Inject constructor(
    private val fetchIssuerCredentialInformation: FetchIssuerCredentialInformation,
    private val fetchCredentialByConfig: FetchCredentialByConfig,
    private val saveCredential: SaveCredential,
) : FetchCredential {
    override suspend fun invoke(credentialOffer: CredentialOffer): Result<Long, FetchCredentialError> = coroutineBinding {
        val issuerInfo = fetchIssuerCredentialInformation(credentialOffer.credentialIssuer, refresh = true)
            .mapError(FetchIssuerCredentialInformationError::toFetchCredentialError)
            .bind()

        val config = getCredentialConfig(
            credentials = credentialOffer.credentialConfigurationIds,
            credentialConfigurations = issuerInfo.credentialConfigurations
        ).bind()
        val credential = fetchCredentialByConfig(
            credentialConfig = config,
            credentialOffer = credentialOffer,
        ).mapError(FetchCredentialByConfigError::toFetchCredentialError)
            .bind()

        saveCredential(
            issuerInfo = issuerInfo,
            anyCredential = credential,
            credentialConfiguration = config
        ).mapError(SaveCredentialError::toFetchCredentialError)
            .bind()
    }

    private fun getCredentialConfig(
        credentials: List<String>,
        credentialConfigurations: List<AnyCredentialConfiguration>
    ): Result<AnyCredentialConfiguration, FetchCredentialError> {
        val matchingCredentials = credentialConfigurations
            .filter { it.identifier in credentials }
        return if (matchingCredentials.isEmpty()) {
            Err(CredentialError.UnsupportedCredentialIdentifier)
        } else {
            Ok(matchingCredentials.first())
        }
    }
}

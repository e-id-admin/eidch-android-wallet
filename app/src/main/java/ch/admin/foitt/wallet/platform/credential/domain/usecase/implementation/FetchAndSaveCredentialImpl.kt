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
import ch.admin.foitt.wallet.platform.credential.domain.usecase.FetchAndSaveCredential
import ch.admin.foitt.wallet.platform.credential.domain.usecase.SaveCredential
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.oca.domain.model.FetchOcaBundleByFormatError
import ch.admin.foitt.wallet.platform.oca.domain.usecase.FetchOcaBundleByFormat
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import timber.log.Timber
import javax.inject.Inject

class FetchAndSaveCredentialImpl @Inject constructor(
    private val fetchIssuerCredentialInformation: FetchIssuerCredentialInformation,
    private val fetchCredentialByConfig: FetchCredentialByConfig,
    private val environmentSetupRepository: EnvironmentSetupRepository,
    private val fetchOcaBundleByFormat: FetchOcaBundleByFormat,
    private val saveCredential: SaveCredential,
) : FetchAndSaveCredential {
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

        // temporary feature flag
        if (environmentSetupRepository.fetchOca) {
            val rawOcaBundle = fetchOcaBundleByFormat(credential)
                .mapError(FetchOcaBundleByFormatError::toFetchCredentialError)
                .bind()

            Timber.d("rawOcaBundle: $rawOcaBundle")
        }

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

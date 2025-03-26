package ch.admin.foitt.wallet.openid4vc

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialRequestProof
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialResponse
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchIssuerConfigurationError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchIssuerCredentialInformationError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchVerifiableCredentialError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.TokenResponse
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.AnyCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerConfiguration
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerCredentialInformation
import ch.admin.foitt.openid4vc.domain.repository.CredentialOfferRepository
import ch.admin.foitt.wallet.feature.credentialOffer.mock.CredentialOfferMocks.MOCK_ACCESS_TOKEN
import ch.admin.foitt.wallet.feature.credentialOffer.mock.CredentialOfferMocks.MOCK_CREDENTIAL_RESPONSE
import ch.admin.foitt.wallet.feature.credentialOffer.mock.CredentialOfferMocks.MOCK_OPEN_ID_CONFIG
import ch.admin.foitt.wallet.feature.credentialOffer.mock.CredentialOfferMocks.MOCK_UETLIBERG_CREDENTIAL_METADATA
import ch.admin.foitt.wallet.platform.utils.SafeJson
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import timber.log.Timber
import javax.inject.Inject

internal class FakeCredentialOfferRepositoryImpl @Inject constructor(
    private val safeJson: SafeJson,
) : CredentialOfferRepository {
    override suspend fun fetchIssuerCredentialInformation(
        issuerEndpoint: String, refresh: Boolean
    ): Result<IssuerCredentialInformation, FetchIssuerCredentialInformationError> {
        Timber.d("issuer credential information fake was used")
        return Ok(safeJson.safeDecodeStringTo<IssuerCredentialInformation>(MOCK_UETLIBERG_CREDENTIAL_METADATA).value)
    }

    override suspend fun fetchIssuerConfiguration(
        issuerEndpoint: String, refresh: Boolean
    ): Result<IssuerConfiguration, FetchIssuerConfigurationError> =
        Ok(safeJson.safeDecodeStringTo<IssuerConfiguration>(MOCK_OPEN_ID_CONFIG).value)

    override suspend fun fetchAccessToken(
        tokenEndpoint: String,
        preAuthorizedCode: String
    ): Result<TokenResponse, FetchVerifiableCredentialError> =
        Ok(safeJson.safeDecodeStringTo<TokenResponse>(MOCK_ACCESS_TOKEN).value)

    override suspend fun fetchCredential(
        issuerEndpoint: String,
        credentialConfiguration: AnyCredentialConfiguration,
        proof: CredentialRequestProof?,
        accessToken: String
    ): Result<CredentialResponse, FetchVerifiableCredentialError> =
        Ok(safeJson.safeDecodeStringTo<CredentialResponse>(MOCK_CREDENTIAL_RESPONSE).value)
}

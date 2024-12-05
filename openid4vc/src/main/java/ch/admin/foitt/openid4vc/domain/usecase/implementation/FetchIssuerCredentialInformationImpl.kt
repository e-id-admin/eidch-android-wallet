package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.repository.CredentialOfferRepository
import ch.admin.foitt.openid4vc.domain.usecase.FetchIssuerCredentialInformation
import javax.inject.Inject

internal class FetchIssuerCredentialInformationImpl @Inject constructor(
    private val credentialOfferRepository: CredentialOfferRepository
) : FetchIssuerCredentialInformation {
    override suspend fun invoke(issuerEndpoint: String, refresh: Boolean) =
        credentialOfferRepository.fetchIssuerCredentialInformation(issuerEndpoint, refresh)
}

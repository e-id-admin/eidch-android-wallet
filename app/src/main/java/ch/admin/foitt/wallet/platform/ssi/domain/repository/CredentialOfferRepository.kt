package ch.admin.foitt.wallet.platform.ssi.domain.repository

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.OidClaimDisplay
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.OidCredentialDisplay
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.OidIssuerDisplay
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.SigningAlgorithm
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialOfferRepositoryError
import com.github.michaelbull.result.Result

interface CredentialOfferRepository {
    suspend fun saveCredentialOffer(
        keyBindingIdentifier: String?,
        keyBindingAlgorithm: SigningAlgorithm?,
        payload: String,
        format: CredentialFormat,
        issuer: String?,
        issuerDisplays: List<OidIssuerDisplay>,
        credentialDisplays: List<OidCredentialDisplay>,
        claims: Map<CredentialClaim, List<OidClaimDisplay>>,
    ): Result<Long, CredentialOfferRepositoryError>
}

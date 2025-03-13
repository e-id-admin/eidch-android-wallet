package ch.admin.foitt.wallet.platform.ssi.domain.model

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.OidClaimDisplay
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.OidCredentialDisplay
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.OidIssuerDisplay
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.SigningAlgorithm
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim

data class LocalizedCredentialOffer(
    val keyBindingIdentifier: String?,
    val keyBindingAlgorithm: SigningAlgorithm?,
    val payload: String,
    val format: CredentialFormat,
    val issuer: String?,
    val issuerDisplays: List<OidIssuerDisplay>,
    val credentialDisplays: List<OidCredentialDisplay>,
    val claims: Map<CredentialClaim, List<OidClaimDisplay>>
)

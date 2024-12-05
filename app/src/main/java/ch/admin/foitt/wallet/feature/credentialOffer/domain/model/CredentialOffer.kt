package ch.admin.foitt.wallet.feature.credentialOffer.domain.model

import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialPreview
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimData

data class CredentialOffer(
    val actorName: String,
    val actorLogo: String?,
    val credential: CredentialPreview,
    val claims: List<CredentialClaimData>,
)

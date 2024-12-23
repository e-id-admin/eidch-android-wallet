package ch.admin.foitt.wallet.feature.credentialOffer.domain.model

import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorDisplayData
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialPreview
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimData

data class CredentialOffer(
    val issuerDisplayData: ActorDisplayData,
    val credential: CredentialPreview,
    val claims: List<CredentialClaimData>,
)

package ch.admin.foitt.wallet.feature.presentationRequest.domain.model

import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimData

data class PresentationMetadata(
    val claims: List<CredentialClaimData>,
)

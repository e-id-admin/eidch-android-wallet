package ch.admin.foitt.wallet.feature.presentationRequest.domain.model

import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialPreview
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimData

data class PresentationRequestUi(
    val credential: CredentialPreview,
    val requestedClaims: List<CredentialClaimData>
)

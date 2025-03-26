package ch.admin.foitt.wallet.feature.presentationRequest.domain.model

import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialDisplayData
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimData

data class PresentationRequestDisplayData(
    val credential: CredentialDisplayData,
    val requestedClaims: List<CredentialClaimData>
)

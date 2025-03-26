package ch.admin.foitt.wallet.feature.presentationRequest.domain.model

import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialDisplayData

data class PresentationCredentialDisplayData(
    val credentials: List<CredentialDisplayData>,
)

package ch.admin.foitt.wallet.feature.presentationRequest.domain.model

import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialPreview

data class PresentationCredentialDisplayData(
    val credentials: List<CredentialPreview>,
)

package ch.admin.foitt.wallet.feature.presentationRequest.presentation.model

import ch.admin.foitt.wallet.platform.credential.presentation.model.CredentialCardState

data class PresentationCredentialListUiState(
    val credentials: List<CredentialCardState>,
) {
    companion object {
        val EMPTY = PresentationCredentialListUiState(
            credentials = emptyList(),
        )
    }
}

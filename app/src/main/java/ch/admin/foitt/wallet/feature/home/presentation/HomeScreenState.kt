package ch.admin.foitt.wallet.feature.home.presentation

import ch.admin.foitt.wallet.platform.credential.presentation.model.CredentialCardState

sealed interface HomeScreenState {
    data object Initial : HomeScreenState
    data class CredentialList(
        val credentials: List<CredentialCardState>,
        val onCredentialClick: (Long) -> Unit,
    ) : HomeScreenState
    data object NoCredential : HomeScreenState
}

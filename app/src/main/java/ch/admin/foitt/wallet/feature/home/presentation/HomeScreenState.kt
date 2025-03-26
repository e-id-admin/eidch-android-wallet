package ch.admin.foitt.wallet.feature.home.presentation

import ch.admin.foitt.wallet.feature.home.domain.model.EIdRequest
import ch.admin.foitt.wallet.platform.credential.presentation.model.CredentialCardState

sealed interface HomeScreenState {
    data object Initial : HomeScreenState
    data class CredentialList(
        val eIdRequests: List<EIdRequest>,
        val credentials: List<CredentialCardState>,
        val onCredentialClick: (Long) -> Unit,
    ) : HomeScreenState
    data class NoCredential(
        val eIdRequests: List<EIdRequest>,
        val showBetaIdRequestButton: Boolean,
        val showEIdRequestButton: Boolean,
    ) : HomeScreenState
}

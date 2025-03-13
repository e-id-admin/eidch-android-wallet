package ch.admin.foitt.wallet.feature.home.presentation

import ch.admin.foitt.wallet.platform.credential.presentation.model.CredentialCardState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequest

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

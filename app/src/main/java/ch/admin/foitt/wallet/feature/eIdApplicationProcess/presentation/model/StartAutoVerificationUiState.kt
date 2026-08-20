package ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.model

internal sealed interface StartAutoVerificationUiState {
    data class Info(
        val onStart: () -> Unit,
    ) : StartAutoVerificationUiState
    data object Loading : StartAutoVerificationUiState
    data class Started(
        val onContinue: () -> Unit,
    ) : StartAutoVerificationUiState
    data class Unexpected(
        val onClose: () -> Unit,
        val onRetry: () -> Unit,
    ) : StartAutoVerificationUiState
    data class NetworkError(
        val onClose: () -> Unit,
        val onRetry: () -> Unit,
    ) : StartAutoVerificationUiState
    data class UnauthorizedPairing(
        val onClose: () -> Unit,
    ) : StartAutoVerificationUiState
}

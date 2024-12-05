package ch.admin.foitt.wallet.platform.biometricPrompt.domain.model

sealed interface BiometricAuthenticationError {
    data object PromptCancelled : BiometricAuthenticationError
    data object PromptLocked : BiometricAuthenticationError
    data class PromptFailure(
        val throwable: Throwable,
    ) : BiometricAuthenticationError
    data class Unexpected(
        val throwable: Throwable,
    ) : BiometricAuthenticationError
}

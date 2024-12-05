package ch.admin.foitt.wallet.platform.biometricPrompt.domain.model

sealed interface BiometricPromptType {
    object Setup : BiometricPromptType
    object Login : BiometricPromptType
}

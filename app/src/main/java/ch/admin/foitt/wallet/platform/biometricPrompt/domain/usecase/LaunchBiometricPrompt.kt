package ch.admin.foitt.wallet.platform.biometricPrompt.domain.usecase

import ch.admin.foitt.wallet.platform.biometricPrompt.domain.model.BiometricAuthenticationError
import ch.admin.foitt.wallet.platform.biometricPrompt.domain.model.BiometricPromptWrapper
import com.github.michaelbull.result.Result
import javax.crypto.Cipher

fun interface LaunchBiometricPrompt {
    suspend operator fun invoke(
        promptWrapper: BiometricPromptWrapper,
        cipher: Cipher,
    ): Result<Cipher, BiometricAuthenticationError>
}

package ch.admin.foitt.wallet.platform.biometricPrompt.domain.model

import androidx.biometric.BiometricManager
import com.github.michaelbull.result.Result
import javax.crypto.Cipher

fun interface BiometricPromptWrapper {
    suspend fun launchPrompt(
        cipher: Cipher,
    ): Result<Cipher, BiometricAuthenticationError>

    companion object {
        const val ALLOWED_AUTHENTICATORS = BiometricManager.Authenticators.BIOMETRIC_STRONG
    }
}

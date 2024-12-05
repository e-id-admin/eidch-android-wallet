package ch.admin.foitt.wallet.platform.login.domain.usecase

import androidx.annotation.CheckResult
import ch.admin.foitt.wallet.platform.biometricPrompt.domain.model.BiometricPromptWrapper
import ch.admin.foitt.wallet.platform.login.domain.model.LoginWithBiometricsError
import com.github.michaelbull.result.Result

fun interface LoginWithBiometrics {
    @CheckResult
    suspend operator fun invoke(promptWrapper: BiometricPromptWrapper): Result<Unit, LoginWithBiometricsError>
}

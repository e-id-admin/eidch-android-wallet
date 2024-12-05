package ch.admin.foitt.wallet.platform.biometricPrompt.domain.usecase.implementation

import androidx.annotation.CheckResult
import androidx.biometric.BiometricManager
import ch.admin.foitt.wallet.platform.biometricPrompt.domain.usecase.GetAuthenticators
import javax.inject.Inject

class GetAuthenticatorsImpl @Inject constructor() : GetAuthenticators {
    @CheckResult
    override fun invoke(): Int = if (
        android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R
    ) {
        ALLOWED_AUTHENTICATORS
    } else {
        ALLOWED_AUTHENTICATORS_LEGACY
    }

    companion object {
        private const val ALLOWED_AUTHENTICATORS =
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        private const val ALLOWED_AUTHENTICATORS_LEGACY =
            BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.BIOMETRIC_WEAK
    }
}

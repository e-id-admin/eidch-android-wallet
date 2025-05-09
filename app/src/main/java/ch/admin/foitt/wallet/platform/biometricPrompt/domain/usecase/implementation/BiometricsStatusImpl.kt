package ch.admin.foitt.wallet.platform.biometricPrompt.domain.usecase.implementation

import android.content.Context
import androidx.biometric.BiometricManager
import ch.admin.foitt.wallet.platform.biometricPrompt.domain.model.BiometricManagerResult
import ch.admin.foitt.wallet.platform.biometricPrompt.domain.model.BiometricPromptWrapper
import ch.admin.foitt.wallet.platform.biometricPrompt.domain.usecase.BiometricsStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

class BiometricsStatusImpl @Inject constructor(
    @ApplicationContext private val appContext: Context,
) : BiometricsStatus {

    override fun invoke(): BiometricManagerResult {
        val biometricManager = BiometricManager.from(appContext)
        return when (val canAuthenticate = biometricManager.canAuthenticate(BiometricPromptWrapper.ALLOWED_AUTHENTICATORS)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricManagerResult.Available
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricManagerResult.CanEnroll
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricManagerResult.Disabled
            else -> {
                Timber.e(message = "Biometric prompt cannot be used: $canAuthenticate")
                BiometricManagerResult.Unsupported
            }
        }
    }
}

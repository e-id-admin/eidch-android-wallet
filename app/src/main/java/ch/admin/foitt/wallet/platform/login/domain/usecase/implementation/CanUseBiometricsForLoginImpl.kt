package ch.admin.foitt.wallet.platform.login.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.biometricPrompt.domain.model.BiometricManagerResult
import ch.admin.foitt.wallet.platform.biometricPrompt.domain.usecase.BiometricsStatus
import ch.admin.foitt.wallet.platform.biometrics.domain.usecase.GetBiometricsCipher
import ch.admin.foitt.wallet.platform.di.IoDispatcher
import ch.admin.foitt.wallet.platform.login.domain.model.CanUseBiometricsForLoginResult
import ch.admin.foitt.wallet.platform.login.domain.usecase.BiometricLoginEnabled
import ch.admin.foitt.wallet.platform.login.domain.usecase.CanUseBiometricsForLogin
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CanUseBiometricsForLoginImpl @Inject constructor(
    @param:IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val biometricsStatus: BiometricsStatus,
    private val biometricLoginEnabled: BiometricLoginEnabled,
    private val getBiometricsCipher: GetBiometricsCipher,
) : CanUseBiometricsForLogin {
    override suspend fun invoke(): CanUseBiometricsForLoginResult = withContext(ioDispatcher) {
        val status by lazy { biometricsStatus() }
        val biometricsNotSetupInApp = !biometricLoginEnabled()

        when {
            biometricsNotSetupInApp -> CanUseBiometricsForLoginResult.NotSetUpInApp
            isKeyNotUsable() -> CanUseBiometricsForLoginResult.Changed
            status == BiometricManagerResult.Available -> CanUseBiometricsForLoginResult.Usable
            status == BiometricManagerResult.CanEnroll -> CanUseBiometricsForLoginResult.RemovedInDeviceSettings
            status == BiometricManagerResult.Disabled -> CanUseBiometricsForLoginResult.DeactivatedInDeviceSettings
            status == BiometricManagerResult.Unsupported -> CanUseBiometricsForLoginResult.NoHardwareAvailable
            else -> CanUseBiometricsForLoginResult.NoHardwareAvailable
        }
    }

    private suspend fun isKeyNotUsable() = getBiometricsCipher().isErr
}

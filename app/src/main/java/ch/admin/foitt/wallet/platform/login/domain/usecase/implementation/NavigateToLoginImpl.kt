package ch.admin.foitt.wallet.platform.login.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.biometrics.domain.usecase.ResetBiometrics
import ch.admin.foitt.wallet.platform.login.domain.model.CanUseBiometricsForLoginResult
import ch.admin.foitt.wallet.platform.login.domain.usecase.CanUseBiometricsForLogin
import ch.admin.foitt.wallet.platform.login.domain.usecase.NavigateToLogin
import ch.admin.foitt.wallet.platform.navArgs.domain.model.PassphraseLoginNavArg
import ch.admin.foitt.walletcomposedestinations.destinations.BiometricLoginScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.PassphraseLoginScreenDestination
import com.ramcosta.composedestinations.spec.Direction
import javax.inject.Inject

class NavigateToLoginImpl @Inject constructor(
    private val canUseBiometricsForLogin: CanUseBiometricsForLogin,
    private val resetBiometrics: ResetBiometrics,
) : NavigateToLogin {
    override suspend fun invoke(): Direction {
        return when (canUseBiometricsForLogin()) {
            CanUseBiometricsForLoginResult.Usable -> BiometricLoginScreenDestination
            CanUseBiometricsForLoginResult.NotSetUpInApp ->
                PassphraseLoginScreenDestination(PassphraseLoginNavArg(biometricsLocked = false))
            else -> {
                resetBiometrics()
                PassphraseLoginScreenDestination(PassphraseLoginNavArg(biometricsLocked = false))
            }
        }
    }
}

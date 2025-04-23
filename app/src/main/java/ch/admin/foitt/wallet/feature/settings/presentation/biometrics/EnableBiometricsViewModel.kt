package ch.admin.foitt.wallet.feature.settings.presentation.biometrics

import android.content.Context
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.biometricPrompt.domain.model.BiometricManagerResult
import ch.admin.foitt.wallet.platform.biometricPrompt.domain.model.BiometricPromptType
import ch.admin.foitt.wallet.platform.biometricPrompt.domain.usecase.BiometricsStatus
import ch.admin.foitt.wallet.platform.biometricPrompt.presentation.AndroidBiometricPrompt
import ch.admin.foitt.wallet.platform.biometrics.domain.model.BiometricsError
import ch.admin.foitt.wallet.platform.biometrics.domain.model.toEnableBiometricsError
import ch.admin.foitt.wallet.platform.biometrics.domain.usecase.InitializeCipherWithBiometrics
import ch.admin.foitt.wallet.platform.biometrics.domain.usecase.SaveUseBiometricLogin
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.passphrase.domain.model.StorePassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.StorePassphrase
import ch.admin.foitt.wallet.platform.scaffold.domain.model.FullscreenState
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetFullscreenState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.map
import ch.admin.foitt.wallet.platform.utils.openSecuritySettings
import ch.admin.foitt.wallet.platform.utils.trackCompletion
import ch.admin.foitt.walletcomposedestinations.destinations.EnableBiometricsErrorScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.EnableBiometricsLockoutScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.EnableBiometricsScreenDestination
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class EnableBiometricsViewModel @Inject constructor(
    private val biometricsStatus: BiometricsStatus,
    private val initializeCipherWithBiometrics: InitializeCipherWithBiometrics,
    private val storePassphrase: StorePassphrase,
    private val saveUseBiometricLogin: SaveUseBiometricLogin,
    private val navManager: NavigationManager,
    setTopBarState: SetTopBarState,
    setFullscreenState: SetFullscreenState,
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val appContext: Context,
) : ScreenViewModel(setTopBarState, setFullscreenState) {
    override val topBarState = TopBarState.Details(::close, R.string.change_biometrics_title)
    override val fullscreenState = FullscreenState.Insets

    private val navArgs = EnableBiometricsScreenDestination.argsFrom(savedStateHandle)
    private val pin = navArgs.pin

    private val _initializationInProgress = MutableStateFlow(false)
    val initializationInProgress = _initializationInProgress.asStateFlow()

    private val _biometricsStatus = MutableStateFlow(biometricsStatus())
    val areBiometricsEnabled = _biometricsStatus.asStateFlow().map(viewModelScope) { biometricManagerResult ->
        when (biometricManagerResult) {
            BiometricManagerResult.Available -> true
            BiometricManagerResult.CanEnroll,
            BiometricManagerResult.Disabled -> false
            BiometricManagerResult.Unsupported -> {
                Timber.w(message = "Biometrics unsupported on the enabling screen")
                false
            }
        }
    }

    fun refreshBiometricStatus(activity: FragmentActivity) {
        _biometricsStatus.value = biometricsStatus()
        if (areBiometricsEnabled.value) {
            enableBiometricsLogin(activity)
        }
    }

    fun openSettings() = appContext.openSecuritySettings()

    fun enableBiometricsLogin(activity: FragmentActivity) {
        viewModelScope.launch {
            enableBiometricsLogin(activity, pin)
        }.trackCompletion(_initializationInProgress)
    }

    private suspend fun enableBiometricsLogin(activity: FragmentActivity, pin: String) {
        Timber.d("Passphrase: Showing biometric dialog")
        val biometricPromptWrapper = AndroidBiometricPrompt(
            activity = activity,
            promptType = BiometricPromptType.Setup
        )

        initializeCipherWithBiometrics(biometricPromptWrapper)
            .andThen { initializedEncryptionCipher ->
                storePassphrase(pin, initializedEncryptionCipher)
                    .mapError(StorePassphraseError::toEnableBiometricsError)
            }.andThen {
                saveUseBiometricLogin(true)
                Ok(Unit)
            }
            .onSuccess {
                close()
            }
            .onFailure { enableBiometricsError ->
                when (enableBiometricsError) {
                    BiometricsError.Locked -> {
                        Timber.w("Enable biometric error: Lockout")
                        navManager.navigateToAndClearCurrent(EnableBiometricsLockoutScreenDestination)
                    }
                    is BiometricsError.Unexpected -> {
                        Timber.e(enableBiometricsError.cause, "Enable biometric error")
                        navManager.navigateToAndClearCurrent(EnableBiometricsErrorScreenDestination)
                    }
                    BiometricsError.Cancelled -> { }
                }
            }
    }

    private fun close() = navManager.popBackStack()
}

package ch.admin.foitt.wallet.feature.login.presentation

import androidx.compose.ui.text.input.TextFieldValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.feature.login.domain.usecase.GetLockoutDuration
import ch.admin.foitt.wallet.feature.login.domain.usecase.GetRemainingLoginAttempts
import ch.admin.foitt.wallet.feature.login.domain.usecase.IncreaseFailedLoginAttemptsCounter
import ch.admin.foitt.wallet.feature.login.domain.usecase.ResetLockout
import ch.admin.foitt.wallet.platform.deeplink.domain.usecase.HandleDeeplink
import ch.admin.foitt.wallet.platform.login.domain.model.CanUseBiometricsForLoginResult
import ch.admin.foitt.wallet.platform.login.domain.model.LoginError
import ch.admin.foitt.wallet.platform.login.domain.usecase.CanUseBiometricsForLogin
import ch.admin.foitt.wallet.platform.login.domain.usecase.LoginWithPassphrase
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.passphraseInput.domain.model.PassphraseInputFieldState
import ch.admin.foitt.wallet.platform.scaffold.domain.model.FullscreenState
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetFullscreenState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.trackCompletion
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.model.AppVersionInfo
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.FetchAppVersionInfo
import ch.admin.foitt.walletcomposedestinations.destinations.AppVersionBlockedScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.BiometricLoginScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.LockoutScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.PassphraseLoginScreenDestination
import com.github.michaelbull.result.mapBoth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import java.time.Duration
import javax.inject.Inject

@HiltViewModel
class PassphraseLoginViewModel @Inject constructor(
    private val navigationManager: NavigationManager,
    private val fetchAppVersionInfo: FetchAppVersionInfo,
    private val loginWithPassphrase: LoginWithPassphrase,
    private val handleDeeplink: HandleDeeplink,
    private val resetLockout: ResetLockout,
    private val getRemainingLoginAttempts: GetRemainingLoginAttempts,
    private val increaseFailedLoginAttemptsCounter: IncreaseFailedLoginAttemptsCounter,
    private val getLockoutDuration: GetLockoutDuration,
    private val canUseBiometricsForLogin: CanUseBiometricsForLogin,
    setTopBarState: SetTopBarState,
    setFullscreenState: SetFullscreenState,
    savedStateHandle: SavedStateHandle,
) : ScreenViewModel(setTopBarState, setFullscreenState, systemBarsFixedLightColor = true) {
    override val topBarState = TopBarState.None
    override val fullscreenState = FullscreenState.Fullscreen

    private val biometricsLocked = PassphraseLoginScreenDestination.argsFrom(savedStateHandle).biometricsLocked

    private var appVersionInfo = flow {
        emit(fetchAppVersionInfo())
    }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        AppVersionInfo.Unknown,
    )

    private val _textFieldValue = MutableStateFlow(TextFieldValue(""))
    val textFieldValue = _textFieldValue.asStateFlow()

    private var _passphraseInputFieldState: MutableStateFlow<PassphraseInputFieldState> =
        MutableStateFlow(PassphraseInputFieldState.Typing)
    val passphraseInputFieldState = _passphraseInputFieldState.asStateFlow()

    private val _loginAttemptsLeft = MutableStateFlow(5)
    val loginAttemptsLeft = _loginAttemptsLeft.asStateFlow()

    private val _showPassphraseErrorToast = MutableStateFlow(false)
    val showPassphraseErrorToast = _showPassphraseErrorToast.asStateFlow()

    private var _showBiometricLoginButton = MutableStateFlow(false)
    val showBiometricLoginButton = _showBiometricLoginButton.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    init {
        canUseBiometrics()
        checkForLockout()
        _loginAttemptsLeft.value = getRemainingLoginAttempts()
    }

    fun canUseBiometrics() = viewModelScope.launch {
        _showBiometricLoginButton.value = canUseBiometricsForLogin() == CanUseBiometricsForLoginResult.Usable && !biometricsLocked
    }

    fun onTextFieldValueChange(textFieldValue: TextFieldValue) {
        _passphraseInputFieldState.value = PassphraseInputFieldState.Typing
        _textFieldValue.value = textFieldValue
    }

    fun onLoginWithPassphrase() {
        viewModelScope.launch {
            _passphraseInputFieldState.value = loginWithPassphrase(passphrase = textFieldValue.value.text).mapBoth(
                success = {
                    resetLockout()
                    PassphraseInputFieldState.Success
                },
                failure = { error ->
                    when (error) {
                        is LoginError.InvalidPassphrase -> {
                            _showPassphraseErrorToast.value = true
                            increaseFailedLoginAttemptsCounter()
                            _loginAttemptsLeft.value = getRemainingLoginAttempts()
                            checkForLockout()
                        }

                        else -> Timber.e(error.toString(), "Login with PIN failed")
                    }
                    PassphraseInputFieldState.Error
                }
            )
            continueWithLogin()
        }.trackCompletion(_isLoading)
    }

    private fun checkForLockout() {
        val lockoutDuration = getLockoutDuration()
        if (lockoutDuration > Duration.ZERO) {
            navigationManager.navigateToAndClearCurrent(LockoutScreenDestination)
        }
    }

    private fun continueWithLogin() {
        if (passphraseInputFieldState.value is PassphraseInputFieldState.Success) {
            val info = appVersionInfo.value // if it is available now, perfect, otherwise it acts like a time-out
            if (info is AppVersionInfo.Blocked) {
                navigateToAppVersionBlocked(info.title, info.text)
            } else {
                handleDeeplink()
            }
        }
    }

    private fun navigateToAppVersionBlocked(title: String?, text: String?) {
        navigationManager.navigateToAndPopUpTo(
            direction = AppVersionBlockedScreenDestination(title = title, text = text),
            route = PassphraseLoginScreenDestination.route,
            inclusivePop = true,
        )
    }

    private fun handleDeeplink() = viewModelScope.launch {
        handleDeeplink(fromOnboarding = false).navigate()
    }.trackCompletion(_isLoading)

    fun onLoginWithBiometrics() = navigationManager.navigateToAndClearCurrent(BiometricLoginScreenDestination)

    fun navigateBack(activity: FragmentActivity) = if (showBiometricLoginButton.value) {
        onLoginWithBiometrics()
    } else {
        activity.finish()
    }

    fun onClosePassphraseError() {
        _showPassphraseErrorToast.value = false
    }
}

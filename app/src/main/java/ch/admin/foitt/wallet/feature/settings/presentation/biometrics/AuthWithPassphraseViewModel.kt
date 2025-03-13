package ch.admin.foitt.wallet.feature.settings.presentation.biometrics

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.authenticateWithPassphrase.domain.model.AuthenticateWithPassphraseError
import ch.admin.foitt.wallet.platform.authenticateWithPassphrase.domain.usecase.AuthenticateWithPassphrase
import ch.admin.foitt.wallet.platform.biometrics.domain.usecase.ResetBiometrics
import ch.admin.foitt.wallet.platform.login.domain.usecase.GetRemainingLoginAttempts
import ch.admin.foitt.wallet.platform.login.domain.usecase.IncreaseFailedLoginAttemptsCounter
import ch.admin.foitt.wallet.platform.login.domain.usecase.ResetLockout
import ch.admin.foitt.wallet.platform.navArgs.domain.model.EnableBiometricsNavArg
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.passphraseInput.domain.model.PassphraseInputFieldState
import ch.admin.foitt.wallet.platform.passphraseInput.domain.model.PassphraseValidationState
import ch.admin.foitt.wallet.platform.passphraseInput.domain.usecase.ValidatePassphrase
import ch.admin.foitt.wallet.platform.scaffold.domain.model.FullscreenState
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetFullscreenState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.trackCompletion
import ch.admin.foitt.walletcomposedestinations.destinations.AuthWithPassphraseScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.EnableBiometricsScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.LockoutScreenDestination
import com.github.michaelbull.result.mapBoth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AuthWithPassphraseViewModel @Inject constructor(
    private val navManager: NavigationManager,
    private val validatePassphrase: ValidatePassphrase,
    private val authenticateWithPassphrase: AuthenticateWithPassphrase,
    private val getRemainingLoginAttempts: GetRemainingLoginAttempts,
    private val resetLockout: ResetLockout,
    private val increaseFailedLoginAttemptsCounter: IncreaseFailedLoginAttemptsCounter,
    private val resetBiometrics: ResetBiometrics,
    setTopBarState: SetTopBarState,
    setFullscreenState: SetFullscreenState,
    savedStateHandle: SavedStateHandle,
) : ScreenViewModel(setTopBarState, setFullscreenState) {

    override val topBarState = TopBarState.Details(navManager::navigateUp, R.string.change_biometrics_title)
    override val fullscreenState = FullscreenState.Insets

    private val navArgs = AuthWithPassphraseScreenDestination.argsFrom(savedStateHandle)
    val enableBiometrics = navArgs.enableBiometrics

    private val _textFieldValue = MutableStateFlow(TextFieldValue(""))
    val textFieldValue = _textFieldValue.asStateFlow()

    private var _passphraseInputFieldState: MutableStateFlow<PassphraseInputFieldState> =
        MutableStateFlow(PassphraseInputFieldState.Typing)
    val passphraseInputFieldState = _passphraseInputFieldState.asStateFlow()

    private var _isPassphraseValid =
        MutableStateFlow(validatePassphrase(textFieldValue.value.text) == PassphraseValidationState.VALID)
    val isPassphraseValid = _isPassphraseValid.asStateFlow()

    private val _remainingAuthAttempts = MutableStateFlow(getRemainingLoginAttempts())
    val remainingAuthAttempts = _remainingAuthAttempts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _initialSupportText = MutableStateFlow(true)
    val initialSupportText = _initialSupportText.asStateFlow()

    fun onTextFieldValueChange(textFieldValue: TextFieldValue) {
        _passphraseInputFieldState.value = PassphraseInputFieldState.Typing
        _textFieldValue.value = textFieldValue
        _isPassphraseValid.value = validatePassphrase(textFieldValue.text) == PassphraseValidationState.VALID
    }

    fun onCheckPassphrase() {
        _passphraseInputFieldState.value = PassphraseInputFieldState.Typing
        if (isPassphraseValid.value) {
            viewModelScope.launch {
                authenticateWithPassphrase(passphrase = textFieldValue.value.text).mapBoth(
                    success = {
                        handlePassphraseSuccess()
                    },
                    failure = { error ->
                        handlePassphraseFailure(error)
                    }
                )
            }.trackCompletion(_isLoading)
        }
    }

    private suspend fun handlePassphraseSuccess() {
        resetLockout()
        _passphraseInputFieldState.value = PassphraseInputFieldState.Success

        if (enableBiometrics) {
            navManager.navigateToAndClearCurrent(
                EnableBiometricsScreenDestination(navArgs = EnableBiometricsNavArg(pin = textFieldValue.value.text))
            )
        } else {
            resetBiometrics()
            navManager.popBackStack()
        }
    }

    private fun handlePassphraseFailure(error: AuthenticateWithPassphraseError) {
        increaseFailedLoginAttemptsCounter()
        if (error is AuthenticateWithPassphraseError.Unexpected) {
            Timber.e(error.cause, "Authentication with pin for biometrics failed")
        }
        _passphraseInputFieldState.value = PassphraseInputFieldState.Error
        _initialSupportText.value = false
        checkRemainingAttempts()
    }

    fun checkRemainingAttempts() {
        _remainingAuthAttempts.value = getRemainingLoginAttempts()
        if (remainingAuthAttempts.value <= 0) {
            navigateToLockoutScreen()
        }
    }

    private fun navigateToLockoutScreen() = viewModelScope.launch {
        navManager.navigateToAndClearCurrent(LockoutScreenDestination)
    }
}

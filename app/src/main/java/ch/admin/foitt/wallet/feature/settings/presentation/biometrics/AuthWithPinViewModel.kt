package ch.admin.foitt.wallet.feature.settings.presentation.biometrics

import androidx.annotation.StringRes
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.authenticateWithPassphrase.domain.model.AuthenticateWithPassphraseError
import ch.admin.foitt.wallet.platform.authenticateWithPassphrase.domain.usecase.AuthenticateWithPassphrase
import ch.admin.foitt.wallet.platform.biometrics.domain.usecase.ResetBiometrics
import ch.admin.foitt.wallet.platform.navArgs.domain.model.EnableBiometricsNavArg
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.passphraseInput.domain.model.PassphraseInputFieldState
import ch.admin.foitt.wallet.platform.scaffold.domain.model.FullscreenState
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetFullscreenState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.trackCompletion
import ch.admin.foitt.walletcomposedestinations.destinations.AuthWithPinScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.EnableBiometricsScreenDestination
import com.github.michaelbull.result.mapBoth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AuthWithPinViewModel @Inject constructor(
    private val navManager: NavigationManager,
    private val authenticateWithPassphrase: AuthenticateWithPassphrase,
    private val resetBiometrics: ResetBiometrics,
    setTopBarState: SetTopBarState,
    setFullscreenState: SetFullscreenState,
    savedStateHandle: SavedStateHandle,
) : ScreenViewModel(setTopBarState, setFullscreenState) {

    override val topBarState = TopBarState.Details(navManager::navigateUp, R.string.change_biometrics_title)
    override val fullscreenState = FullscreenState.Insets

    private val navArgs = AuthWithPinScreenDestination.argsFrom(savedStateHandle)
    val enableBiometrics = navArgs.enable

    private val _textFieldValue = MutableStateFlow(TextFieldValue(""))
    val textFieldValue = _textFieldValue.asStateFlow()

    private var _passphraseInputFieldState: MutableStateFlow<PassphraseInputFieldState> =
        MutableStateFlow(PassphraseInputFieldState.Typing)
    val passphraseInputFieldState = _passphraseInputFieldState.asStateFlow()

    private var _errorMessage: MutableStateFlow<Int?> = MutableStateFlow(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun onTextFieldValueChange(textFieldValue: TextFieldValue) {
        _passphraseInputFieldState.value = PassphraseInputFieldState.Typing
        _textFieldValue.value = textFieldValue
    }

    fun onCheckPassphrase() {
        viewModelScope.launch {
            authenticateWithPassphrase(passphrase = textFieldValue.value.text).mapBoth(
                success = {
                    if (!enableBiometrics) {
                        resetBiometrics()
                    }
                    _passphraseInputFieldState.value = PassphraseInputFieldState.Success
                    handlePassphraseSuccess()
                },
                failure = { error ->
                    if (error is AuthenticateWithPassphraseError.Unexpected) {
                        Timber.e(error.cause, "Authentication with pin for biometrics failed")
                    }
                    _passphraseInputFieldState.value = PassphraseInputFieldState.Error
                    showPinValidationError(R.string.tk_onboarding_nopasswordmismatch_notification)
                }
            )
        }.trackCompletion(_isLoading)
    }

    private fun handlePassphraseSuccess() {
        if (enableBiometrics) {
            navManager.navigateToAndClearCurrent(
                EnableBiometricsScreenDestination(navArgs = EnableBiometricsNavArg(pin = textFieldValue.value.text))
            )
        } else {
            navManager.popBackStack()
        }
    }

    private fun showPinValidationError(@StringRes text: Int) {
        _errorMessage.value = text
    }

    fun onClosePassphraseError() {
        _errorMessage.value = null
    }
}

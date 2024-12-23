package ch.admin.foitt.wallet.feature.changeLogin.presentation

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.changeLogin.domain.Constants.MAX_NEW_PASSPHRASE_CONFIRMATION_ATTEMPTS
import ch.admin.foitt.wallet.feature.changeLogin.domain.usecase.ChangePassphrase
import ch.admin.foitt.wallet.feature.changeLogin.domain.usecase.DeleteNewPassphraseConfirmationAttempts
import ch.admin.foitt.wallet.feature.changeLogin.domain.usecase.GetNewPassphraseConfirmationAttempts
import ch.admin.foitt.wallet.feature.changeLogin.domain.usecase.IncreaseFailedNewPassphraseConfirmationAttemptsCounter
import ch.admin.foitt.wallet.platform.eventToast.domain.repository.PassphraseChangeSuccessToastRepository
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
import ch.admin.foitt.walletcomposedestinations.destinations.ConfirmNewPassphraseScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.SecuritySettingsScreenDestination
import com.github.michaelbull.result.mapBoth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ConfirmNewPassphraseViewModel @Inject constructor(
    private val navManager: NavigationManager,
    private val validatePassphrase: ValidatePassphrase,
    private val getNewPassphraseConfirmationAttempts: GetNewPassphraseConfirmationAttempts,
    private val increaseFailedNewPassphraseConfirmationAttemptsCounter: IncreaseFailedNewPassphraseConfirmationAttemptsCounter,
    private val deleteNewPassphraseConfirmationAttempts: DeleteNewPassphraseConfirmationAttempts,
    private val changePassphrase: ChangePassphrase,
    private val passphraseChangeSuccessToastRepository: PassphraseChangeSuccessToastRepository,
    setTopBarState: SetTopBarState,
    setFullscreenState: SetFullscreenState,
    savedStateHandle: SavedStateHandle,
) : ScreenViewModel(setTopBarState, setFullscreenState) {
    override val topBarState = TopBarState.Details(navManager::navigateUp, R.string.pin_change_title)
    override val fullscreenState = FullscreenState.Insets

    private val originalPassphrase = ConfirmNewPassphraseScreenDestination.argsFrom(savedStateHandle).passphrase

    private val _textFieldValue = MutableStateFlow(TextFieldValue(text = ""))
    val textFieldValue = _textFieldValue.asStateFlow()

    private var _passphraseInputFieldState: MutableStateFlow<PassphraseInputFieldState> =
        MutableStateFlow(PassphraseInputFieldState.Typing)
    val passphraseInputFieldState = _passphraseInputFieldState.asStateFlow()

    private var _isNextButtonEnabled =
        MutableStateFlow(validatePassphrase(textFieldValue.value.text) == PassphraseValidationState.VALID)
    val isNextButtonEnabled = _isNextButtonEnabled.asStateFlow()

    private var _remainingConfirmationAttempts = MutableStateFlow(getNewPassphraseConfirmationAttempts())
    val remainingConfirmationAttempts = _remainingConfirmationAttempts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    val hideSupportText = combine(
        passphraseInputFieldState,
        remainingConfirmationAttempts,
        isLoading
    ) { passphraseInputFieldState, remainingConfirmationAttempts, isLoading ->
        passphraseInputFieldState == PassphraseInputFieldState.Typing ||
            remainingConfirmationAttempts >= MAX_NEW_PASSPHRASE_CONFIRMATION_ATTEMPTS ||
            isLoading
    }.toStateFlow(true)

    fun onTextFieldValueChange(textFieldValue: TextFieldValue) {
        _passphraseInputFieldState.value = PassphraseInputFieldState.Typing
        _textFieldValue.value = textFieldValue
        _isNextButtonEnabled.value = validatePassphrase(textFieldValue.text) == PassphraseValidationState.VALID
    }

    fun onCheckPassphrase() {
        _passphraseInputFieldState.value = PassphraseInputFieldState.Typing
        if (isNextButtonEnabled.value) {
            viewModelScope.launch {
                when (textFieldValue.value.text == originalPassphrase) {
                    true -> onValidPassphrase()
                    false -> onInvalidPassphrase()
                }
            }.trackCompletion(_isLoading)
        }
    }

    private fun onInvalidPassphrase() {
        increaseFailedNewPassphraseConfirmationAttemptsCounter()
        _passphraseInputFieldState.value = PassphraseInputFieldState.Error
        checkRemainingConfirmationAttempts()
    }

    private suspend fun onValidPassphrase() = changePassphrase(textFieldValue.value.text).mapBoth(
        success = {
            _passphraseInputFieldState.value = PassphraseInputFieldState.Success
            deleteNewPassphraseConfirmationAttempts()
            passphraseChangeSuccessToastRepository.showPassphraseChangeSuccess()
            navManager.popBackStackTo(SecuritySettingsScreenDestination, false)
        },
        failure = { error ->
            Timber.e(error.throwable, "Could not change password")
            _passphraseInputFieldState.value = PassphraseInputFieldState.Error
        }
    )

    fun checkRemainingConfirmationAttempts() {
        _remainingConfirmationAttempts.value = getNewPassphraseConfirmationAttempts()
        if (remainingConfirmationAttempts.value <= 0) {
            deleteNewPassphraseConfirmationAttempts()
            navManager.popBackStack()
        }
    }
}

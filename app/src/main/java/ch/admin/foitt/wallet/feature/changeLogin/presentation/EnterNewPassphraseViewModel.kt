package ch.admin.foitt.wallet.feature.changeLogin.presentation

import androidx.compose.ui.text.input.TextFieldValue
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.navArgs.domain.model.ConfirmNewPassphraseNavArg
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.passphraseInput.domain.model.PassphraseInputFieldState
import ch.admin.foitt.wallet.platform.passphraseInput.domain.model.PassphraseValidationState
import ch.admin.foitt.wallet.platform.passphraseInput.domain.usecase.ValidatePassphrase
import ch.admin.foitt.wallet.platform.scaffold.domain.model.FullscreenState
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetFullscreenState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.walletcomposedestinations.destinations.ConfirmNewPassphraseScreenDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class EnterNewPassphraseViewModel @Inject constructor(
    private val navManager: NavigationManager,
    private val validatePassphrase: ValidatePassphrase,
    setTopBarState: SetTopBarState,
    setFullscreenState: SetFullscreenState,
) : ScreenViewModel(setTopBarState, setFullscreenState) {
    override val topBarState = TopBarState.Details(navManager::navigateUp, R.string.tk_global_newpassword)
    override val fullscreenState = FullscreenState.Insets

    private val _textFieldValue = MutableStateFlow(TextFieldValue(""))
    val textFieldValue = _textFieldValue.asStateFlow()

    private var _passphraseInputFieldState: MutableStateFlow<PassphraseInputFieldState> =
        MutableStateFlow(PassphraseInputFieldState.Typing)
    val passphraseInputFieldState = _passphraseInputFieldState.asStateFlow()

    val isPassphraseValid: StateFlow<Boolean> = textFieldValue.map { textField ->
        validatePassphrase(textField.text) == PassphraseValidationState.VALID
    }.toStateFlow(false, 0)

    fun onTextFieldValueChange(textFieldValue: TextFieldValue) {
        _passphraseInputFieldState.value = PassphraseInputFieldState.Typing
        _textFieldValue.value = textFieldValue
    }

    fun onCheckPassphrase() {
        if (isPassphraseValid.value) {
            navigateToConfirmNewPassphraseScreen()
            _textFieldValue.value = TextFieldValue("")
        }
    }

    private fun navigateToConfirmNewPassphraseScreen() =
        navManager.navigateTo(ConfirmNewPassphraseScreenDestination(navArgs = ConfirmNewPassphraseNavArg(textFieldValue.value.text)))
}

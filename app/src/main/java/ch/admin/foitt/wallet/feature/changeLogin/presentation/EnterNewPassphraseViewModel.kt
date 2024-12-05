package ch.admin.foitt.wallet.feature.changeLogin.presentation

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
import kotlinx.coroutines.flow.asStateFlow
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

    private val _passphrase = MutableStateFlow("")
    val passphrase = _passphrase.asStateFlow()

    private var _passphraseInputFieldState: MutableStateFlow<PassphraseInputFieldState> =
        MutableStateFlow(PassphraseInputFieldState.Typing)
    val passphraseInputFieldState = _passphraseInputFieldState.asStateFlow()

    private var _isNextButtonEnabled =
        MutableStateFlow(validatePassphrase(passphrase.value) == PassphraseValidationState.VALID)
    val isNextButtonEnabled = _isNextButtonEnabled.asStateFlow()

    fun onUpdatePassphrase(passphrase: String) {
        _passphraseInputFieldState.value = PassphraseInputFieldState.Typing
        _passphrase.value = passphrase
        _isNextButtonEnabled.value = validatePassphrase(passphrase) == PassphraseValidationState.VALID
    }

    fun onCheckPassphrase() {
        if (isNextButtonEnabled.value) {
            navigateToConfirmNewPassphraseScreen()
            _passphrase.value = ""
        }
    }

    private fun navigateToConfirmNewPassphraseScreen() =
        navManager.navigateTo(ConfirmNewPassphraseScreenDestination(navArgs = ConfirmNewPassphraseNavArg(passphrase.value)))
}

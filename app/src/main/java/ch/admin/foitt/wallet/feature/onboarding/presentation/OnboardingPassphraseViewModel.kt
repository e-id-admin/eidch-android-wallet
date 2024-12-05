package ch.admin.foitt.wallet.feature.onboarding.presentation

import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.navArgs.domain.model.ConfirmPassphraseNavArg
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
import ch.admin.foitt.walletcomposedestinations.destinations.OnboardingConfirmPassphraseScreenDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingPassphraseViewModel @Inject constructor(
    private val navManager: NavigationManager,
    private val validatePassphrase: ValidatePassphrase,
    setTopBarState: SetTopBarState,
    setFullscreenState: SetFullscreenState,
) : ScreenViewModel(setTopBarState, setFullscreenState, systemBarsFixedLightColor = true) {
    override val topBarState = TopBarState.Transparent(titleId = R.string.tk_global_enterpassword, onUp = navManager::popBackStack)
    override val fullscreenState = FullscreenState.Fullscreen

    private val _passphrase = MutableStateFlow("")
    val passphrase = _passphrase.asStateFlow()

    private var _passphraseInputFieldState: MutableStateFlow<PassphraseInputFieldState> =
        MutableStateFlow(PassphraseInputFieldState.Typing)
    val passphraseInputFieldState = _passphraseInputFieldState.asStateFlow()

    private var _showPassphraseErrorToast = MutableStateFlow(false)
    val showPassphraseErrorToast = _showPassphraseErrorToast.asStateFlow()

    private val _isValidating = MutableStateFlow(false)
    val isValidating = _isValidating.asStateFlow()

    fun onUpdatePassphrase(passphrase: String) {
        _passphraseInputFieldState.value = PassphraseInputFieldState.Typing
        _passphrase.value = passphrase
    }

    fun onCheckPassphrase() {
        viewModelScope.launch {
            when (validatePassphrase(passphrase.value)) {
                PassphraseValidationState.VALID -> {
                    _passphraseInputFieldState.value = PassphraseInputFieldState.Success
                    navigateToConfirmPassphraseScreen()
                    _passphrase.value = ""
                }

                PassphraseValidationState.INVALID_MIN_LENGTH -> {
                    _passphraseInputFieldState.value = PassphraseInputFieldState.Error
                    _showPassphraseErrorToast.value = true
                }
            }
        }.trackCompletion(_isValidating)
    }

    private fun navigateToConfirmPassphraseScreen() =
        navManager.navigateTo(OnboardingConfirmPassphraseScreenDestination(navArgs = ConfirmPassphraseNavArg(passphrase.value)))

    fun onClosePassphraseError() {
        _showPassphraseErrorToast.value = false
    }
}

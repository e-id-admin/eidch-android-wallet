package ch.admin.foitt.wallet.feature.changeLogin.presentation

import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.changeLogin.domain.Constants.MAX_CURRENT_PASSPHRASE_ATTEMPTS
import ch.admin.foitt.wallet.feature.changeLogin.domain.usecase.DeleteCurrentPassphraseAttempts
import ch.admin.foitt.wallet.feature.changeLogin.domain.usecase.GetCurrentPassphraseAttempts
import ch.admin.foitt.wallet.feature.changeLogin.domain.usecase.IncreaseFailedCurrentPassphraseAttemptsCounter
import ch.admin.foitt.wallet.platform.authenticateWithPassphrase.domain.model.AuthenticateWithPassphraseError
import ch.admin.foitt.wallet.platform.authenticateWithPassphrase.domain.usecase.AuthenticateWithPassphrase
import ch.admin.foitt.wallet.platform.login.domain.usecase.NavigateToLogin
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
import ch.admin.foitt.walletcomposedestinations.destinations.EnterNewPassphraseScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.HomeScreenDestination
import com.github.michaelbull.result.mapBoth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class EnterCurrentPassphraseViewModel @Inject constructor(
    private val navManager: NavigationManager,
    private val getCurrentPassphraseAttempts: GetCurrentPassphraseAttempts,
    private val validatePassphrase: ValidatePassphrase,
    private val authenticateWithPassphrase: AuthenticateWithPassphrase,
    private val deleteCurrentPassphraseAttempts: DeleteCurrentPassphraseAttempts,
    private val increaseFailedCurrentPassphraseAttemptsCounter: IncreaseFailedCurrentPassphraseAttemptsCounter,
    private val navigateToLogin: NavigateToLogin,
    setTopBarState: SetTopBarState,
    setFullscreenState: SetFullscreenState,
) : ScreenViewModel(setTopBarState, setFullscreenState) {
    override val topBarState = TopBarState.Details(navManager::navigateUp, R.string.tk_global_changepassword)
    override val fullscreenState = FullscreenState.Insets

    private val _passphrase = MutableStateFlow("")
    val passphrase = _passphrase.asStateFlow()

    private var _passphraseInputFieldState: MutableStateFlow<PassphraseInputFieldState> =
        MutableStateFlow(PassphraseInputFieldState.Typing)
    val passphraseInputFieldState = _passphraseInputFieldState.asStateFlow()

    private var _isNextButtonEnabled =
        MutableStateFlow(validatePassphrase(passphrase.value) == PassphraseValidationState.VALID)
    val isNextButtonEnabled = _isNextButtonEnabled.asStateFlow()

    private val _remainingAuthAttempts = MutableStateFlow(getCurrentPassphraseAttempts())
    val remainingAuthAttempts = _remainingAuthAttempts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    val hideSupportText = combine(
        passphraseInputFieldState,
        remainingAuthAttempts,
        isLoading
    ) { passphraseInputFieldState, remainingAuthAttempts, isLoading ->
        passphraseInputFieldState == PassphraseInputFieldState.Typing ||
            remainingAuthAttempts >= MAX_CURRENT_PASSPHRASE_ATTEMPTS ||
            isLoading
    }.toStateFlow(true)

    fun onUpdatePassphrase(passphrase: String) {
        _passphraseInputFieldState.value = PassphraseInputFieldState.Typing
        _passphrase.value = passphrase
        _isNextButtonEnabled.value = validatePassphrase(passphrase) == PassphraseValidationState.VALID
    }

    fun onCheckPassphrase() {
        _passphraseInputFieldState.value = PassphraseInputFieldState.Typing
        if (isNextButtonEnabled.value) {
            viewModelScope.launch {
                authenticateWithPassphrase(passphrase = passphrase.value).mapBoth(
                    success = {
                        _passphraseInputFieldState.value = PassphraseInputFieldState.Success
                        deleteCurrentPassphraseAttempts()
                        _passphrase.value = ""
                        navigateToEnterNewPassphraseScreen()
                    },
                    failure = { error ->
                        increaseFailedCurrentPassphraseAttemptsCounter()
                        if (error is AuthenticateWithPassphraseError.Unexpected) {
                            Timber.e(error.cause, "Authentication with current passphrase failed")
                        }
                        _passphraseInputFieldState.value = PassphraseInputFieldState.Error
                        checkRemainingAttempts()
                    }
                )
            }.trackCompletion(_isLoading)
        }
    }

    fun checkRemainingAttempts() {
        _remainingAuthAttempts.value = getCurrentPassphraseAttempts()
        if (remainingAuthAttempts.value <= 0) {
            deleteCurrentPassphraseAttempts()
            navigateToLoginScreen()
        }
    }

    private fun navigateToEnterNewPassphraseScreen() = navManager.navigateTo(EnterNewPassphraseScreenDestination)

    private fun navigateToLoginScreen() = viewModelScope.launch {
        navManager.navigateToAndPopUpTo(
            direction = navigateToLogin(),
            route = HomeScreenDestination.route,
        )
    }
}

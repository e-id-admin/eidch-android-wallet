package ch.admin.foitt.wallet.feature.settings.presentation.security

import android.content.Context
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.eventTracking.domain.usecase.ApplyUserPrivacyPolicy
import ch.admin.foitt.wallet.platform.eventTracking.domain.usecase.IsUserPrivacyPolicyAcceptedFlow
import ch.admin.foitt.wallet.platform.login.domain.model.CanUseBiometricsForLoginResult
import ch.admin.foitt.wallet.platform.login.domain.usecase.CanUseBiometricsForLogin
import ch.admin.foitt.wallet.platform.navArgs.domain.model.AuthWithPinNavArg
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.GetPassphraseWasDeleted
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.SavePassphraseWasDeleted
import ch.admin.foitt.wallet.platform.scaffold.domain.model.FullscreenState
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetFullscreenState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.openLink
import ch.admin.foitt.walletcomposedestinations.destinations.AuthWithPinScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.DataAnalysisScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.EnterCurrentPassphraseScreenDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SecuritySettingsViewModel @Inject constructor(
    private val navManager: NavigationManager,
    @ApplicationContext private val appContext: Context,
    private val canUseBiometricsForLogin: CanUseBiometricsForLogin,
    private val applyUserPrivacyPolicy: ApplyUserPrivacyPolicy,
    isUserPrivacyPolicyAcceptedFlow: IsUserPrivacyPolicyAcceptedFlow,
    private val getPassphraseWasDeleted: GetPassphraseWasDeleted,
    private val savePassphraseWasDeleted: SavePassphraseWasDeleted,
    setTopBarState: SetTopBarState,
    setFullscreenState: SetFullscreenState,
) : ScreenViewModel(setTopBarState, setFullscreenState) {

    override val topBarState = TopBarState.Details(navManager::navigateUp, R.string.securitySettings_title)
    override val fullscreenState = FullscreenState.Insets

    val biometricsHardwareIsAvailable: Flow<Boolean> = flow {
        emit(
            canUseBiometricsForLogin() != CanUseBiometricsForLoginResult.NoHardwareAvailable
        )
    }

    val isBiometricsToggleEnabled: Flow<Boolean> = flow {
        emit(
            canUseBiometricsForLogin() == CanUseBiometricsForLoginResult.Usable
        )
    }

    val showPassphraseDeletionMessage: Flow<Boolean> = flow {
        emit(getPassphraseWasDeleted())
        savePassphraseWasDeleted(false)
    }

    val shareAnalysisEnabled = isUserPrivacyPolicyAcceptedFlow()

    fun onChangeBiometrics() {
        viewModelScope.launch {
            isBiometricsToggleEnabled.collect { isToggleCurrentStateOn ->
                if (isToggleCurrentStateOn) {
                    toggleBiometricsOff()
                } else {
                    toggleBiometricsOn()
                }
            }
        }
    }

    private fun toggleBiometricsOn() {
        navManager.navigateTo(AuthWithPinScreenDestination(navArgs = AuthWithPinNavArg(enable = true)))
    }

    private fun toggleBiometricsOff() {
        navManager.navigateTo(AuthWithPinScreenDestination(navArgs = AuthWithPinNavArg(enable = false)))
    }

    fun onDataProtection() = appContext.openLink(R.string.securitySettings_dataProtectionLink)

    fun onChangePassphrase() = navManager.navigateTo(EnterCurrentPassphraseScreenDestination)

    fun onShareAnalysisChange(isEnabled: Boolean) {
        applyUserPrivacyPolicy(isEnabled)
    }

    fun onDataAnalysis() = navManager.navigateTo(DataAnalysisScreenDestination)
}

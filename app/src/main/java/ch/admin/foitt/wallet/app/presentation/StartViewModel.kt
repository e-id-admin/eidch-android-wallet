package ch.admin.foitt.wallet.app.presentation

import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.platform.appSetupState.domain.repository.OnboardingStateRepository
import ch.admin.foitt.wallet.platform.login.domain.usecase.NavigateToLogin
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.scaffold.domain.model.FullscreenState
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetFullscreenState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.walletcomposedestinations.destinations.OnboardingIntroScreenDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StartViewModel @Inject constructor(
    private val onboardingStateRepository: OnboardingStateRepository,
    private val navigateToLogin: NavigateToLogin,
    private val navManager: NavigationManager,
    setTopBarState: SetTopBarState,
    setFullscreenState: SetFullscreenState,
) : ScreenViewModel(setTopBarState, setFullscreenState) {

    override val topBarState = TopBarState.SystemBarPadding
    override val fullscreenState = FullscreenState.Insets

    fun navigateToFirstScreen() {
        viewModelScope.launch {
            when (onboardingStateRepository.getOnboardingState()) {
                true -> navManager.navigateToAndClearCurrent(navigateToLogin())
                false -> navManager.navigateToAndClearCurrent(OnboardingIntroScreenDestination)
            }
        }
    }
}

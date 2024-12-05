package ch.admin.foitt.wallet.feature.onboarding.presentation

import android.content.Context
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.eventTracking.domain.usecase.ApplyUserPrivacyPolicy
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.scaffold.domain.model.FullscreenState
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetFullscreenState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.openLink
import ch.admin.foitt.walletcomposedestinations.destinations.OnboardingPassphraseExplanationScreenDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class UserPrivacyPolicyViewModel @Inject constructor(
    private val navManager: NavigationManager,
    @ApplicationContext private val appContext: Context,
    setTopBarState: SetTopBarState,
    setFullscreenState: SetFullscreenState,
    private val applyUserPrivacyPolicy: ApplyUserPrivacyPolicy,
) : ScreenViewModel(setTopBarState, setFullscreenState) {

    override val topBarState = TopBarState.Details(onUp = navManager::popBackStack, null)
    override val fullscreenState = FullscreenState.Insets

    fun acceptTracking() {
        applyUserPrivacyPolicy(true)
        onNext()
    }

    fun declineTracking() {
        applyUserPrivacyPolicy(false)
        onNext()
    }

    fun onOpenUserPrivacyPolicy() = appContext.openLink(R.string.onboarding_privacy_link_value)

    private fun onNext() = navManager.navigateTo(OnboardingPassphraseExplanationScreenDestination)
    fun onBack() = navManager.popBackStack()
}

package ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.SetHasLegalGuardian
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.walletcomposedestinations.destinations.EIdInfoScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.EIdIntroScreenDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class EIdGuardianshipViewModel @Inject constructor(
    private val navManager: NavigationManager,
    private val setHasLegalGuardian: SetHasLegalGuardian,
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {
    override val topBarState = TopBarState.DetailsWithCloseButton(
        titleId = null,
        onUp = navManager::popBackStack,
        onClose = { navManager.navigateBackToHome(EIdIntroScreenDestination) },
    )

    fun onDeclareGuardianship(hasGuardianship: Boolean) {
        setHasLegalGuardian(hasGuardianship)
        navManager.navigateTo(EIdInfoScreenDestination)
    }
}

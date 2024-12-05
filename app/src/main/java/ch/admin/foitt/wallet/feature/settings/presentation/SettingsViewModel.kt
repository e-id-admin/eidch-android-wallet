package ch.admin.foitt.wallet.feature.settings.presentation

import android.content.Context
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.scaffold.domain.model.FullscreenState
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetFullscreenState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.openLink
import ch.admin.foitt.walletcomposedestinations.destinations.ImpressumScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.LanguageScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.LicencesScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.SecuritySettingsScreenDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val navManager: NavigationManager,
    @ApplicationContext private val appContext: Context,
    setTopBarState: SetTopBarState,
    setFullscreenState: SetFullscreenState,
) : ScreenViewModel(setTopBarState, setFullscreenState) {

    override val topBarState = TopBarState.Details(navManager::navigateUp, R.string.settings_title)
    override val fullscreenState = FullscreenState.Insets

    fun onSecurityScreen() = navManager.navigateTo(SecuritySettingsScreenDestination)

    fun onLanguageScreen() = navManager.navigateTo(LanguageScreenDestination)

    fun onHelp() = appContext.openLink(R.string.settings_helpLink)

    fun onContact() = appContext.openLink(R.string.settings_contactLink)

    fun onImpressumScreen() = navManager.navigateTo(ImpressumScreenDestination)

    fun onLicencesScreen() = navManager.navigateTo(LicencesScreenDestination)
}

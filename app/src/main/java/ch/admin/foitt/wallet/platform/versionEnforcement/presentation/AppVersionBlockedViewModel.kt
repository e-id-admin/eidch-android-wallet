package ch.admin.foitt.wallet.platform.versionEnforcement.presentation

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.scaffold.domain.model.FullscreenState
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetFullscreenState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.openLink
import ch.admin.foitt.walletcomposedestinations.destinations.AppVersionBlockedScreenDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class AppVersionBlockedViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    setTopBarState: SetTopBarState,
    setFullscreenState: SetFullscreenState,
    savedStateHandle: SavedStateHandle
) : ScreenViewModel(setTopBarState, setFullscreenState) {
    override val topBarState = TopBarState.None
    override val fullscreenState = FullscreenState.Fullscreen

    private val navArgs = AppVersionBlockedScreenDestination.argsFrom(savedStateHandle)
    val title = navArgs.title
    val text = navArgs.text

    fun goToPlayStore() = appContext.openLink(R.string.version_enforcement_store_link)
}

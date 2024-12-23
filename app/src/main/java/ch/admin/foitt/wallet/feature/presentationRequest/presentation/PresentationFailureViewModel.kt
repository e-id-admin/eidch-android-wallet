package ch.admin.foitt.wallet.feature.presentationRequest.presentation

import androidx.lifecycle.SavedStateHandle
import ch.admin.foitt.wallet.platform.composables.presentation.adapter.GetLocalizedDateTime
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.scaffold.domain.model.FullscreenState
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetFullscreenState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.extension.navigateUpOrToRoot
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.walletcomposedestinations.destinations.PresentationRequestScreenDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
class PresentationFailureViewModel @Inject constructor(
    private val navManager: NavigationManager,
    private val getLocalizedDateTime: GetLocalizedDateTime,
    savedStateHandle: SavedStateHandle,
    setTopBarState: SetTopBarState,
    setFullscreenState: SetFullscreenState,
) : ScreenViewModel(setTopBarState, setFullscreenState) {
    override val topBarState = TopBarState.None
    override val fullscreenState = FullscreenState.Insets

    private val navArgs = PresentationRequestScreenDestination.argsFrom(savedStateHandle)
    private val compatibleCredential = navArgs.compatibleCredential
    private val presentationRequest = navArgs.presentationRequest

    private val eventTime = ZonedDateTime.now()
    val dateTime: String get() = getLocalizedDateTime(eventTime)

    fun onRetry() = navManager.navigateToAndClearCurrent(
        direction = PresentationRequestScreenDestination(
            compatibleCredential = compatibleCredential,
            presentationRequest = presentationRequest,
        )
    )

    fun onClose() = navManager.navigateUpOrToRoot()
}

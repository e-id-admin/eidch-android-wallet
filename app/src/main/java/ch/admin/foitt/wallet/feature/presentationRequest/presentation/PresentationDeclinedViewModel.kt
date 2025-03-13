package ch.admin.foitt.wallet.feature.presentationRequest.presentation

import androidx.lifecycle.SavedStateHandle
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.adapter.GetActorUiState
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.model.ActorUiState
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.scaffold.domain.model.FullscreenState
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetFullscreenState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.extension.navigateUpOrToRoot
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.walletcomposedestinations.destinations.PresentationDeclinedScreenDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class PresentationDeclinedViewModel @Inject constructor(
    private val navManager: NavigationManager,
    private val getActorUiState: GetActorUiState,
    savedStateHandle: SavedStateHandle,
    setTopBarState: SetTopBarState,
    setFullscreenState: SetFullscreenState,
) : ScreenViewModel(setTopBarState, setFullscreenState) {
    override val topBarState = TopBarState.None
    override val fullscreenState = FullscreenState.Fullscreen
    private val navArgs = PresentationDeclinedScreenDestination.argsFrom(savedStateHandle)
    private val issuerDisplayData = navArgs.issuerDisplayData

    private val _verifierDisplayData: MutableStateFlow<ActorDisplayData> = MutableStateFlow(issuerDisplayData)
    val verifierUiState = _verifierDisplayData.map { verifierDisplayData ->
        getActorUiState(
            actorDisplayData = verifierDisplayData,
        )
    }.toStateFlow(ActorUiState.EMPTY, 0)

    fun onBack() = navManager.navigateUpOrToRoot()
}

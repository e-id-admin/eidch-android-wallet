package ch.admin.foitt.wallet.feature.presentationRequest.presentation

import androidx.lifecycle.SavedStateHandle
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.GetActorForScope
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.adapter.GetActorUiState
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.model.ActorUiState
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.ComponentScope
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.extension.navigateUpOrToRoot
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.walletcomposedestinations.destinations.PresentationSuccessScreenDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class PresentationSuccessViewModel @Inject constructor(
    private val navManager: NavigationManager,
    private val getActorUiState: GetActorUiState,
    getActorForScope: GetActorForScope,
    savedStateHandle: SavedStateHandle,
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {
    override val topBarState = TopBarState.None

    private val navArg = PresentationSuccessScreenDestination.argsFrom(savedStateHandle)
    val fields = navArg.sentFields.toList()

    private val verifierDisplayData = getActorForScope(ComponentScope.Verifier)
    val verifierUiState = verifierDisplayData.map {
        getActorUiState(actorDisplayData = it)
    }.toStateFlow(ActorUiState.EMPTY, 0)

    fun onClose() = navManager.navigateUpOrToRoot()
}

package ch.admin.foitt.wallet.feature.presentationRequest.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase.GetPresentationRequestCredentialListFlow
import ch.admin.foitt.wallet.feature.presentationRequest.presentation.model.PresentationCredentialListUiState
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.FetchVerifierDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.adapter.GetActorUiState
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.model.ActorUiState
import ch.admin.foitt.wallet.platform.credential.presentation.adapter.GetCredentialCardState
import ch.admin.foitt.wallet.platform.navArgs.domain.model.PresentationRequestNavArg
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.scaffold.domain.model.FullscreenState
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetFullscreenState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.extension.navigateUpOrToRoot
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.walletcomposedestinations.destinations.ErrorScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.PresentationCredentialListScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.PresentationRequestScreenDestination
import com.github.michaelbull.result.mapBoth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PresentationCredentialListViewModel @Inject constructor(
    private val navManager: NavigationManager,
    getPresentationRequestCredentialListFlow: GetPresentationRequestCredentialListFlow,
    private val fetchVerifierDisplayData: FetchVerifierDisplayData,
    private val getCredentialCardState: GetCredentialCardState,
    private val getActorUiState: GetActorUiState,
    savedStateHandle: SavedStateHandle,
    setTopBarState: SetTopBarState,
    setFullscreenState: SetFullscreenState,
) : ScreenViewModel(setTopBarState, setFullscreenState) {
    override val topBarState = TopBarState.None
    override val fullscreenState = FullscreenState.Fullscreen

    private val navArgs = PresentationCredentialListScreenDestination.argsFrom(savedStateHandle)

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _verifierDisplayData: MutableStateFlow<ActorDisplayData> = MutableStateFlow(ActorDisplayData.EMPTY)
    val verifierUiState = _verifierDisplayData.map { verifierDisplayData ->
        getActorUiState(
            actorDisplayData = verifierDisplayData,
            defaultName = R.string.presentation_verifier_name_unknown,
        )
    }.toStateFlow(ActorUiState.EMPTY, 0)

    val presentationCredentialListUiState: StateFlow<PresentationCredentialListUiState> = getPresentationRequestCredentialListFlow(
        compatibleCredentials = navArgs.compatibleCredentials,
    ).map { result ->
        result.mapBoth(
            success = { presentationCredentialListUi ->
                _isLoading.value = false
                PresentationCredentialListUiState(
                    credentials = presentationCredentialListUi.credentials.map { getCredentialCardState(it) },
                )
            },
            failure = {
                navigateToErrorScreen()
                null
            },
        )
    }
        .filterNotNull()
        .toStateFlow(PresentationCredentialListUiState.EMPTY)

    init {
        viewModelScope.launch {
            updateVerifierDisplayData()
        }
    }

    fun onCredentialSelected(index: Int) {
        navManager.navigateToAndClearCurrent(
            direction = PresentationRequestScreenDestination(
                navArgs = PresentationRequestNavArg(
                    navArgs.compatibleCredentials[index],
                    navArgs.presentationRequest,
                    navArgs.shouldFetchTrustStatement,
                )
            )
        )
    }

    fun onBack() = navManager.navigateUpOrToRoot()

    private suspend fun updateVerifierDisplayData() {
        val verifierDisplayData: ActorDisplayData = fetchVerifierDisplayData(
            presentationRequest = navArgs.presentationRequest,
            shouldFetchTrustStatement = navArgs.shouldFetchTrustStatement,
        )
        _verifierDisplayData.value = verifierDisplayData
    }

    private fun navigateToErrorScreen() {
        navManager.navigateToAndClearCurrent(ErrorScreenDestination)
    }
}

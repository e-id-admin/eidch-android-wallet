package ch.admin.foitt.wallet.feature.presentationRequest.presentation

import androidx.lifecycle.SavedStateHandle
import ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase.GetPresentationRequestCredentialListFlow
import ch.admin.foitt.wallet.platform.composables.presentation.adapter.GetDrawableFromUri
import ch.admin.foitt.wallet.platform.credential.presentation.adapter.GetCredentialCardState
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.ClientNameDisplay
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.LogoUriDisplay
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedDisplay
import ch.admin.foitt.wallet.platform.navArgs.domain.model.PresentationRequestNavArg
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.scaffold.domain.model.FullscreenState
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetFullscreenState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.extension.navigateUpOrToRoot
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.toPainter
import ch.admin.foitt.walletcomposedestinations.destinations.ErrorScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.PresentationCredentialListScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.PresentationRequestScreenDestination
import com.github.michaelbull.result.mapBoth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class PresentationCredentialListViewModel @Inject constructor(
    private val navManager: NavigationManager,
    getPresentationRequestCredentialListFlow: GetPresentationRequestCredentialListFlow,
    private val getCredentialCardState: GetCredentialCardState,
    private val getDrawableFromUri: GetDrawableFromUri,
    private val getLocalizedDisplay: GetLocalizedDisplay,
    savedStateHandle: SavedStateHandle,
    setTopBarState: SetTopBarState,
    setFullscreenState: SetFullscreenState,
) : ScreenViewModel(setTopBarState, setFullscreenState) {
    override val topBarState = TopBarState.None
    override val fullscreenState = FullscreenState.Insets

    private val navArgs = PresentationCredentialListScreenDestination.argsFrom(savedStateHandle)

    val localizedDisplayClients = ClientNameDisplay.fromClientName(
        navArgs.presentationRequest.clientMetaData?.clientNameList ?: emptyList()
    )
    val localizedDisplayUris = LogoUriDisplay.fromLogoUri(navArgs.presentationRequest.clientMetaData?.logoUriList ?: emptyList())

    // timeout = 0, overriding the default 5s, to change the text immediately in case a language change happens
    val verifierName = flow {
        emit(getLocalizedDisplay(localizedDisplayClients)?.clientName)
    }.toStateFlow(null, timeout = 0)

    val verifierLogo = flow {
        emit(getDrawableFromUri(getLocalizedDisplay(localizedDisplayUris)?.logoUri)?.toPainter())
    }.toStateFlow(null, timeout = 0)

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    val presentationCredentialListUiState = getPresentationRequestCredentialListFlow(
        compatibleCredentials = navArgs.compatibleCredentials,
        presentationRequest = navArgs.presentationRequest,
    ).map { result ->
        result.mapBoth(
            success = { credentialPreviews ->
                _isLoading.value = false
                credentialPreviews.map { getCredentialCardState(it) }
            },
            failure = {
                navigateToErrorScreen()
                null
            },
        )
    }.filterNotNull()
        .toStateFlow(emptyList())

    fun onCredentialSelected(index: Int) {
        navManager.navigateToAndClearCurrent(
            direction = PresentationRequestScreenDestination(
                navArgs = PresentationRequestNavArg(
                    navArgs.compatibleCredentials[index],
                    navArgs.presentationRequest,
                    navArgs.clientDisplay,
                    navArgs.uriDisplay
                )
            )
        )
    }

    fun onBack() = navManager.navigateUpOrToRoot()

    private fun navigateToErrorScreen() {
        navManager.navigateToAndClearCurrent(ErrorScreenDestination)
    }
}

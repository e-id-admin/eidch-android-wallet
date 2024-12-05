package ch.admin.foitt.wallet.feature.credentialOffer.presentation

import androidx.lifecycle.SavedStateHandle
import ch.admin.foitt.wallet.feature.credentialOffer.presentation.model.DeclineCredentialOfferUiState
import ch.admin.foitt.wallet.platform.composables.presentation.adapter.GetDrawableFromUri
import ch.admin.foitt.wallet.platform.credential.presentation.model.IssuerUiState
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialIssuerDisplay
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.scaffold.domain.model.FullscreenState
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetFullscreenState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.GetCredentialIssuerDisplayFlow
import ch.admin.foitt.wallet.platform.utils.toPainter
import ch.admin.foitt.walletcomposedestinations.destinations.CredentialOfferDeclinedScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.DeclineCredentialOfferScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.ErrorScreenDestination
import com.github.michaelbull.result.mapBoth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DeclineCredentialOfferViewModel @Inject constructor(
    getCredentialIssuerDisplayFlow: GetCredentialIssuerDisplayFlow,
    private val getDrawableFromUri: GetDrawableFromUri,
    private val navManager: NavigationManager,
    setTopBarState: SetTopBarState,
    setFullscreenState: SetFullscreenState,
    savedStateHandle: SavedStateHandle,
) : ScreenViewModel(setTopBarState, setFullscreenState) {
    override val topBarState = TopBarState.None
    override val fullscreenState = FullscreenState.Fullscreen

    private val navArgs = DeclineCredentialOfferScreenDestination.argsFrom(savedStateHandle)
    private val credentialId = navArgs.credentialId

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    val uiState: StateFlow<DeclineCredentialOfferUiState> = getCredentialIssuerDisplayFlow(credentialId).map { result ->
        result.mapBoth(
            success = { credentialIssuerDisplay ->
                _isLoading.value = false
                credentialIssuerDisplay?.toDeclineCredentialOfferUiState()
            },
            failure = {
                navigateToErrorScreen()
                DeclineCredentialOfferUiState.EMPTY
            },
        )
    }.filterNotNull()
        .toStateFlow(DeclineCredentialOfferUiState.EMPTY)

    private suspend fun CredentialIssuerDisplay.toDeclineCredentialOfferUiState() = DeclineCredentialOfferUiState(
        issuer = IssuerUiState(
            name = this.name,
            painter = getDrawableFromUri(this.image)?.toPainter()
        ),
    )

    fun onCancel() = navManager.popBackStack()

    fun onDecline() = navManager.navigateTo(CredentialOfferDeclinedScreenDestination(credentialId))

    private fun navigateToErrorScreen() = navManager.navigateToAndClearCurrent(ErrorScreenDestination)
}

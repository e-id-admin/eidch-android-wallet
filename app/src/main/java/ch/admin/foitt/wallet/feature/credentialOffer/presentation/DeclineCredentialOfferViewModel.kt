package ch.admin.foitt.wallet.feature.credentialOffer.presentation

import androidx.lifecycle.SavedStateHandle
import ch.admin.foitt.wallet.feature.credentialOffer.presentation.model.DeclineCredentialOfferUiState
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.GetActorForScope
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.adapter.GetActorUiState
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.ComponentScope
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.walletcomposedestinations.destinations.CredentialOfferDeclinedScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.DeclineCredentialOfferScreenDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class DeclineCredentialOfferViewModel @Inject constructor(
    private val getActorUiState: GetActorUiState,
    private val navManager: NavigationManager,
    getActorForScope: GetActorForScope,
    setTopBarState: SetTopBarState,
    savedStateHandle: SavedStateHandle,
) : ScreenViewModel(setTopBarState) {
    override val topBarState = TopBarState.None

    private val navArgs = DeclineCredentialOfferScreenDestination.argsFrom(savedStateHandle)
    private val credentialId = navArgs.credentialId
    private val issuerDisplayData = getActorForScope(ComponentScope.CredentialIssuer)

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    val uiState: StateFlow<DeclineCredentialOfferUiState> = issuerDisplayData.map { displayData ->
        val uiState = DeclineCredentialOfferUiState(
            issuer = getActorUiState(
                actorDisplayData = displayData,
            ),
        )
        _isLoading.value = false
        uiState
    }.toStateFlow(DeclineCredentialOfferUiState.EMPTY, 0)

    fun onCancel() = navManager.popBackStack()

    fun onDecline() = navManager.navigateTo(
        CredentialOfferDeclinedScreenDestination(
            credentialId = credentialId,
        )
    )
}

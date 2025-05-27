package ch.admin.foitt.wallet.feature.credentialOffer.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.feature.credentialOffer.domain.model.CredentialOffer
import ch.admin.foitt.wallet.feature.credentialOffer.domain.usecase.GetCredentialOfferFlow
import ch.admin.foitt.wallet.feature.credentialOffer.presentation.model.CredentialOfferUiState
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.GetActorForScope
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.adapter.GetActorUiState
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.model.ActorUiState
import ch.admin.foitt.wallet.platform.appSetupState.domain.usecase.SaveFirstCredentialWasAdded
import ch.admin.foitt.wallet.platform.credential.presentation.adapter.GetCredentialCardState
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.UpdateCredentialStatus
import ch.admin.foitt.wallet.platform.messageEvents.domain.model.CredentialOfferEvent
import ch.admin.foitt.wallet.platform.messageEvents.domain.repository.CredentialOfferEventRepository
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.ComponentScope
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.extension.navigateUpOrToRoot
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.walletcomposedestinations.destinations.CredentialOfferScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.CredentialOfferWrongDataScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.DeclineCredentialOfferScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.ErrorScreenDestination
import com.github.michaelbull.result.mapBoth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CredentialOfferViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getCredentialOfferFlow: GetCredentialOfferFlow,
    private val navManager: NavigationManager,
    private val updateCredentialStatus: UpdateCredentialStatus,
    private val getCredentialCardState: GetCredentialCardState,
    private val saveFirstCredentialWasAdded: SaveFirstCredentialWasAdded,
    private val getActorUiState: GetActorUiState,
    getActorForScope: GetActorForScope,
    private val credentialOfferEventRepository: CredentialOfferEventRepository,
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {
    override val topBarState = TopBarState.None

    private val navArgs = CredentialOfferScreenDestination.argsFrom(savedStateHandle)
    private val credentialId = navArgs.credentialId

    private val issuerUiState = getActorForScope(ComponentScope.CredentialIssuer).map { displayData ->
        getActorUiState(
            actorDisplayData = displayData,
        )
    }.toStateFlow(ActorUiState.EMPTY, 0)

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val credentialOffer: StateFlow<CredentialOffer?> = getCredentialOfferFlow(credentialId)
        .map { result ->
            result.mapBoth(
                success = { credentialOffer ->
                    _isLoading.value = false
                    credentialOffer
                },
                failure = {
                    navigateToErrorScreen()
                    null
                }
            )
        }.toStateFlow(null)

    val credentialOfferUiState: StateFlow<CredentialOfferUiState> = combine(
        credentialOffer,
        issuerUiState,
    ) { credentialOffer, issuerUiState ->
        credentialOffer?.let {
            CredentialOfferUiState(
                issuer = issuerUiState,
                credential = getCredentialCardState(credentialOffer.credential),
                claims = credentialOffer.claims,
            )
        }
    }
        .filterNotNull()
        .toStateFlow(CredentialOfferUiState.EMPTY)

    init {
        viewModelScope.launch {
            updateCredentialStatus(credentialId)
        }
    }

    fun onAcceptClicked() {
        viewModelScope.launch {
            saveFirstCredentialWasAdded()
            credentialOfferEventRepository.setEvent(CredentialOfferEvent.ACCEPTED)
            navManager.navigateUpOrToRoot()
        }
    }

    fun onDeclineClicked() {
        credentialOffer.value?.let { credentialOffer ->
            navManager.navigateTo(
                DeclineCredentialOfferScreenDestination(
                    credentialId = credentialId,
                )
            )
        } ?: navigateToErrorScreen()
    }

    private fun navigateToErrorScreen() {
        navManager.navigateToAndClearCurrent(ErrorScreenDestination)
    }

    fun onWrongDataClicked() {
        navManager.navigateTo(CredentialOfferWrongDataScreenDestination)
    }
}

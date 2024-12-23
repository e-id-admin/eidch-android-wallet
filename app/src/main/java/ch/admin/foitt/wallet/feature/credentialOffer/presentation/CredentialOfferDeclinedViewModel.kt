package ch.admin.foitt.wallet.feature.credentialOffer.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.credentialOffer.presentation.model.DeclineCredentialOfferUiState
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.adapter.GetActorUiState
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.scaffold.domain.model.FullscreenState
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetFullscreenState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.DeleteCredential
import ch.admin.foitt.walletcomposedestinations.destinations.CredentialOfferDeclinedScreenDestination
import com.github.michaelbull.result.onFailure
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CredentialOfferDeclinedViewModel @Inject constructor(
    private val getActorUiState: GetActorUiState,
    private val deleteCredential: DeleteCredential,
    private val navManager: NavigationManager,
    setTopBarState: SetTopBarState,
    setFullscreenState: SetFullscreenState,
    savedStateHandle: SavedStateHandle,
) : ScreenViewModel(setTopBarState, setFullscreenState) {
    override val topBarState = TopBarState.None
    override val fullscreenState = FullscreenState.Fullscreen

    private val navArgs = CredentialOfferDeclinedScreenDestination.argsFrom(savedStateHandle)
    private val credentialId = navArgs.credentialId
    private val issuerDisplayData = navArgs.issuerDisplayData

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    val uiState: StateFlow<DeclineCredentialOfferUiState> = flow {
        deleteCredential()
        val uiState = DeclineCredentialOfferUiState(
            issuer = getActorUiState(
                actorDisplayData = issuerDisplayData,
                defaultName = R.string.tk_credential_offer_issuer_name_unknown,
            ),
        )
        emit(uiState)
        _isLoading.value = false
    }
        .toStateFlow(DeclineCredentialOfferUiState.EMPTY, 0)

    private fun deleteCredential() = viewModelScope.launch {
        deleteCredential(credentialId).onFailure { error ->
            when (error) {
                is SsiError.Unexpected -> Timber.e(error.cause)
            }
        }
        delay(NAV_DELAY)
        navigateToHome()
    }

    fun navigateToHome() = navManager.navigateBackToHome(from = CredentialOfferDeclinedScreenDestination)

    companion object {
        private const val NAV_DELAY = 2500L
    }
}

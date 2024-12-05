package ch.admin.foitt.wallet.feature.credentialOffer.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
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
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.DeleteCredential
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.GetCredentialIssuerDisplayFlow
import ch.admin.foitt.wallet.platform.utils.toPainter
import ch.admin.foitt.walletcomposedestinations.destinations.CredentialOfferDeclinedScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.ErrorScreenDestination
import com.github.michaelbull.result.mapBoth
import com.github.michaelbull.result.onFailure
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CredentialOfferDeclinedViewModel @Inject constructor(
    getCredentialIssuerDisplayFlow: GetCredentialIssuerDisplayFlow,
    private val deleteCredential: DeleteCredential,
    private val getDrawableFromUri: GetDrawableFromUri,
    private val navManager: NavigationManager,
    setTopBarState: SetTopBarState,
    setFullscreenState: SetFullscreenState,
    savedStateHandle: SavedStateHandle,
) : ScreenViewModel(setTopBarState, setFullscreenState) {
    override val topBarState = TopBarState.None
    override val fullscreenState = FullscreenState.Fullscreen

    private val navArgs = CredentialOfferDeclinedScreenDestination.argsFrom(savedStateHandle)
    private val credentialId = navArgs.credentialId

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    val uiState: StateFlow<DeclineCredentialOfferUiState> = getCredentialIssuerDisplayFlow(credentialId).map { result ->
        result.mapBoth(
            success = { credentialIssuerDisplay ->
                _isLoading.value = false
                credentialIssuerDisplay?.let {
                    deleteCredential()
                    it.toDeclineCredentialOfferUiState()
                }
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
        )
    )

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

    private fun navigateToErrorScreen() = navManager.navigateToAndClearCurrent(ErrorScreenDestination)

    companion object {
        private const val NAV_DELAY = 2500L
    }
}

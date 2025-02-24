package ch.admin.foitt.wallet.feature.presentationRequest.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestErrorBody
import ch.admin.foitt.openid4vc.domain.usecase.DeclinePresentation
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.PresentationRequestDisplayData
import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.PresentationRequestError
import ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase.GetPresentationRequestFlow
import ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase.SubmitPresentation
import ch.admin.foitt.wallet.feature.presentationRequest.presentation.model.PresentationRequestUiState
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.FetchVerifierDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.adapter.GetActorUiState
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.model.ActorUiState
import ch.admin.foitt.wallet.platform.credential.presentation.adapter.GetCredentialCardState
import ch.admin.foitt.wallet.platform.di.IoDispatcherScope
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.scaffold.domain.model.FullscreenState
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetFullscreenState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.launchWithDelayedLoading
import ch.admin.foitt.wallet.platform.utils.trackCompletion
import ch.admin.foitt.walletcomposedestinations.destinations.ErrorScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.PresentationDeclinedScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.PresentationFailureScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.PresentationRequestScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.PresentationSuccessScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.PresentationValidationErrorScreenDestination
import com.github.michaelbull.result.mapBoth
import com.github.michaelbull.result.onFailure
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PresentationRequestViewModel @Inject constructor(
    private val navManager: NavigationManager,
    getPresentationRequestFlow: GetPresentationRequestFlow,
    private val fetchVerifierDisplayData: FetchVerifierDisplayData,
    private val submitPresentation: SubmitPresentation,
    private val declinePresentation: DeclinePresentation,
    @IoDispatcherScope private val ioDispatcherScope: CoroutineScope,
    private val getCredentialCardState: GetCredentialCardState,
    private val getActorUiState: GetActorUiState,
    savedStateHandle: SavedStateHandle,
    setTopBarState: SetTopBarState,
    setFullscreenState: SetFullscreenState,
) : ScreenViewModel(setTopBarState, setFullscreenState) {
    override val topBarState: TopBarState = TopBarState.None
    override val fullscreenState = FullscreenState.Fullscreen

    private val navArgs = PresentationRequestScreenDestination.argsFrom(savedStateHandle)
    private val compatibleCredential = navArgs.compatibleCredential
    private val presentationRequest = navArgs.presentationRequest

    private val _verifierDisplayData: MutableStateFlow<ActorDisplayData> = MutableStateFlow(ActorDisplayData.EMPTY)
    val verifierUiState = _verifierDisplayData.map { verifierDisplayData ->
        getActorUiState(
            actorDisplayData = verifierDisplayData,
            defaultName = R.string.presentation_verifier_name_unknown,
        )
    }.toStateFlow(ActorUiState.EMPTY, 0)

    val presentationRequestUiState: StateFlow<PresentationRequestUiState> =
        getPresentationRequestFlow(
            id = compatibleCredential.credentialId,
            requestedFields = compatibleCredential.requestedFields,
            presentationRequest = presentationRequest,
        ).map { result ->
            result.mapBoth(
                success = { presentationRequestUi ->
                    _isLoading.value = false
                    presentationRequestUi.toUiState()
                },
                failure = {
                    navigateToErrorScreen()
                    null
                },
            )
        }.filterNotNull()
            .toStateFlow(PresentationRequestUiState.EMPTY)

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting = _isSubmitting.asStateFlow()

    private val _showDelayReason = MutableStateFlow(false)
    val showDelayReason = _showDelayReason.asStateFlow()

    init {
        viewModelScope.launch {
            updateVerifierDisplayData()
        }
    }

    fun submit() {
        viewModelScope.launchWithDelayedLoading(
            isLoadingFlow = _showDelayReason,
            delay = DELAY_REASON_DURATION
        ) {
            viewModelScope.launch {
                submitPresentation(
                    presentationRequest = presentationRequest,
                    compatibleCredential = compatibleCredential,
                ).mapBoth(
                    success = { navigateToSuccess() },
                    failure = { error ->
                        when (error) {
                            PresentationRequestError.InvalidUrl,
                            PresentationRequestError.RawSdJwtParsingError,
                            PresentationRequestError.NetworkError,
                            is PresentationRequestError.Unexpected -> navigateToFailure()
                            PresentationRequestError.ValidationError -> navigateToValidationError()
                        }
                    },
                )
            }.trackCompletion(_isSubmitting)
        }
    }

    fun onDecline() {
        ioDispatcherScope.launch {
            declinePresentation(
                url = presentationRequest.responseUri,
                reason = PresentationRequestErrorBody.ErrorType.CLIENT_REJECTED,
            ).onFailure { error ->
                Timber.w("Decline presentation error: $error")
            }
        }
        navManager.navigateToAndClearCurrent(
            direction = PresentationDeclinedScreenDestination(
                issuerDisplayData = _verifierDisplayData.value,
            )
        )
    }

    private suspend fun updateVerifierDisplayData() {
        val verifierDisplayData: ActorDisplayData = fetchVerifierDisplayData(
            navArgs.presentationRequest,
            navArgs.shouldFetchTrustStatement,
        )
        _verifierDisplayData.value = verifierDisplayData
    }

    private fun navigateToSuccess() {
        navManager.navigateToAndClearCurrent(
            direction = PresentationSuccessScreenDestination(
                sentFields = getSentFields(),
                issuerDisplayData = _verifierDisplayData.value,
            )
        )
    }

    private fun navigateToValidationError() {
        navManager.navigateToAndClearCurrent(
            direction = PresentationValidationErrorScreenDestination(
                issuerDisplayData = _verifierDisplayData.value
            )
        )
    }

    private fun getSentFields() =
        presentationRequestUiState.value.requestedClaims.map { claimData ->
            claimData.localizedKey
        }.toTypedArray()

    private fun navigateToFailure() = navManager.navigateToAndClearCurrent(
        PresentationFailureScreenDestination(
            compatibleCredential = compatibleCredential,
            presentationRequest = presentationRequest,
            issuerDisplayData = _verifierDisplayData.value,
            shouldFetchTrustStatement = navArgs.shouldFetchTrustStatement,
        )
    )

    private fun navigateToErrorScreen() {
        navManager.navigateToAndClearCurrent(ErrorScreenDestination)
    }

    private suspend fun PresentationRequestDisplayData.toUiState(): PresentationRequestUiState {
        return PresentationRequestUiState(
            credential = getCredentialCardState(credential),
            requestedClaims = requestedClaims,
        )
    }

    companion object {
        private const val DELAY_REASON_DURATION = 5000L
    }
}

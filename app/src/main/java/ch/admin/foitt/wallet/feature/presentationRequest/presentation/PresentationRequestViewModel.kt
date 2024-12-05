package ch.admin.foitt.wallet.feature.presentationRequest.presentation

import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.painter.Painter
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestErrorBody
import ch.admin.foitt.openid4vc.domain.usecase.DeclinePresentation
import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.PresentationRequestError
import ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase.GetPresentationRequestFlow
import ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase.SubmitPresentation
import ch.admin.foitt.wallet.feature.presentationRequest.presentation.model.PresentationRequestUiState
import ch.admin.foitt.wallet.platform.composables.presentation.adapter.GetDrawableFromUri
import ch.admin.foitt.wallet.platform.credential.presentation.adapter.GetCredentialCardState
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.ClientNameDisplay
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.LogoUriDisplay
import ch.admin.foitt.wallet.platform.di.IoDispatcherScope
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedDisplay
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.scaffold.domain.model.FullscreenState
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetFullscreenState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.toPainter
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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class PresentationRequestViewModel @Inject constructor(
    private val navManager: NavigationManager,
    getPresentationRequestFlow: GetPresentationRequestFlow,
    private val submitPresentation: SubmitPresentation,
    private val declinePresentation: DeclinePresentation,
    @IoDispatcherScope private val ioDispatcherScope: CoroutineScope,
    private val getCredentialCardState: GetCredentialCardState,
    private val getDrawableFromUri: GetDrawableFromUri,
    getLocalizedDisplay: GetLocalizedDisplay,
    savedStateHandle: SavedStateHandle,
    setTopBarState: SetTopBarState,
    setFullscreenState: SetFullscreenState,
) : ScreenViewModel(setTopBarState, setFullscreenState) {
    override val topBarState: TopBarState = TopBarState.None
    override val fullscreenState = FullscreenState.Insets

    private val navArgs = PresentationRequestScreenDestination.argsFrom(savedStateHandle)
    private val compatibleCredential = navArgs.compatibleCredential
    private val presentationRequest = navArgs.presentationRequest

    val localizedDisplayClients = ClientNameDisplay.fromClientName(
        navArgs.presentationRequest.clientMetaData?.clientNameList ?: emptyList()
    )
    val localizedDisplayUris = LogoUriDisplay.fromLogoUri(navArgs.presentationRequest.clientMetaData?.logoUriList ?: emptyList())

    // timeout = 0, overriding the default 5s, to change the text immediately in case a language change happens
    val verifierName = flow {
        emit(getLocalizedDisplay(localizedDisplayClients)?.clientName)
    }.toStateFlow(null, timeout = 0)

    private val verifierLogoDrawable: StateFlow<Drawable?> = flow {
        emit(getDrawableFromUri(getLocalizedDisplay(localizedDisplayUris)?.logoUri))
    }.toStateFlow(null, timeout = 0)

    val verifierLogoPainter: StateFlow<Painter?> = verifierLogoDrawable.map { drawable ->
        drawable?.toPainter()
    }.toStateFlow(null)

    val presentationRequestUiState: StateFlow<PresentationRequestUiState> =
        getPresentationRequestFlow(
            id = compatibleCredential.credentialId,
            requestedFields = compatibleCredential.requestedFields,
            presentationRequest = presentationRequest,
        ).map { result ->
            result.mapBoth(
                success = {
                    _isLoading.value = false
                    PresentationRequestUiState(
                        credential = getCredentialCardState(it.credential),
                        requestedClaims = it.requestedClaims
                    )
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

    fun submit() {
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

    fun onDecline() {
        ioDispatcherScope.launch {
            declinePresentation(
                url = presentationRequest.responseUri,
                reason = PresentationRequestErrorBody.ErrorType.CLIENT_REJECTED,
            ).onFailure { error ->
                Timber.w("Decline presentation error: $error")
            }
        }
        navManager.navigateToAndClearCurrent(PresentationDeclinedScreenDestination)
    }

    private fun navigateToSuccess() {
        navManager.navigateToAndClearCurrent(
            direction = PresentationSuccessScreenDestination(
                sentFields = getSentFields(),
            )
        )
    }

    private fun navigateToValidationError() {
        navManager.navigateToAndClearCurrent(
            direction = PresentationValidationErrorScreenDestination(
                sentFields = getSentFields(),
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
        )
    )

    private fun navigateToErrorScreen() {
        navManager.navigateToAndClearCurrent(ErrorScreenDestination)
    }
}

package ch.admin.foitt.wallet.feature.qrscan.presentation

import android.content.Context
import androidx.camera.view.PreviewView
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestErrorBody
import ch.admin.foitt.openid4vc.domain.usecase.DeclinePresentation
import ch.admin.foitt.wallet.feature.qrscan.infra.QrScanner
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError
import ch.admin.foitt.wallet.platform.invitation.domain.model.ProcessInvitationError
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.HandleInvitationProcessingSuccess
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.ProcessInvitation
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.extension.hasCameraPermission
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.walletcomposedestinations.destinations.QrScanPermissionScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.QrScannerScreenDestination
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class QrScannerViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val processInvitation: ProcessInvitation,
    private val handleInvitationProcessingSuccess: HandleInvitationProcessingSuccess,
    private val declinePresentation: DeclinePresentation,
    private val qrScanner: QrScanner,
    private val navManager: NavigationManager,
    savedStateHandle: SavedStateHandle,
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState, systemBarsFixedLightColor = true) {

    override val topBarState = TopBarState.None

    private val firstCredentialWasAdded = QrScannerScreenDestination.argsFrom(savedStateHandle).firstCredentialWasAdded

    val flashLightState = qrScanner.flashLightState

    private val _infoState = MutableStateFlow(initQrInfoHint())
    val infoState = _infoState.asStateFlow()

    private var job: Job? = null

    val scanIsRunning = qrScanner.isRunning

    fun onInitScan(previewView: PreviewView) {
        qrScanner.registerScanner(previewView).onFailure { throwable ->
            _infoState.update { QrInfoState.UnexpectedError }
            Timber.e(t = throwable, message = "ValidationFailure while registering scanner")
        }
    }

    private fun onQrScanSuccess(barcodesContent: List<String>) {
        if (job?.isActive == true) {
            return
        }
        qrScanner.pauseScanner()
        _infoState.update { QrInfoState.Loading }
        job = viewModelScope.launch {
            val invitationUri = barcodesContent.firstOrNull() ?: ""
            processInvitation(invitationUri = invitationUri)
                .onSuccess { invitation ->
                    handleInvitationProcessingSuccess(invitation).navigate()
                }
                .onFailure { invitationError ->
                    handleProcessingFailure(invitationError)
                }
        }.apply {
            invokeOnCompletion {
                this@QrScannerViewModel.job = null
                if (infoState.value is QrInfoState.Loading) {
                    _infoState.update { QrInfoState.Empty }
                }
            }
        }
    }

    private suspend fun handleProcessingFailure(failureResult: ProcessInvitationError) {
        _infoState.update { failureResult.toQrInfoState() }

        when (failureResult) {
            is InvitationError.InvalidPresentation -> declinePresentationRequest(failureResult.responseUri)
            else -> {}
        }

        delay(DELAY_SCANNING_IN_MS)
        if (infoState.value !is QrInfoState.Loading) {
            qrScanner.resumeScanner()
        }
    }

    private fun tryInitAnalyser() {
        return if (hasCameraPermission(appContext)) {
            qrScanner.initAnalyser(onBarcodesScanned = ::onQrScanSuccess)
        } else {
            navManager.navigateToAndClearCurrent(QrScanPermissionScreenDestination)
        }
    }

    fun onFlashLight() {
        viewModelScope.launch {
            qrScanner.toggleFlashLight()
        }
    }

    fun onUp() = navManager.navigateUp()

    fun onCloseToast() {
        _infoState.update { QrInfoState.Empty }
        job?.cancel()
        qrScanner.resumeScanner()
    }

    private fun initQrInfoHint(): QrInfoState {
        return if (firstCredentialWasAdded) {
            QrInfoState.Empty
        } else {
            QrInfoState.Hint
        }
    }

    private fun ProcessInvitationError.toQrInfoState() = when (this) {
        InvitationError.InvalidInput -> QrInfoState.InvalidQr
        InvitationError.InvalidCredentialOffer -> QrInfoState.InvalidCredentialOffer
        InvitationError.CredentialOfferExpired -> QrInfoState.ExpiredCredentialOffer
        InvitationError.NoCompatibleCredential -> QrInfoState.NoCompatibleCredential
        InvitationError.EmptyWallet -> QrInfoState.EmptyWallet
        InvitationError.NetworkError -> QrInfoState.NetworkError
        InvitationError.InvalidPresentationRequest,
        is InvitationError.InvalidPresentation -> QrInfoState.InvalidPresentation
        InvitationError.Unexpected -> QrInfoState.UnexpectedError
        InvitationError.UnknownIssuer -> QrInfoState.UnknownIssuer
        InvitationError.UnknownVerifier -> QrInfoState.UnknownVerifier
    }

    private fun declinePresentationRequest(uri: String) = viewModelScope.launch {
        declinePresentation(url = uri, reason = PresentationRequestErrorBody.ErrorType.INVALID_REQUEST)
    }

    init {
        tryInitAnalyser()
    }

    override fun onCleared() {
        qrScanner.unRegisterScanner()
        super.onCleared()
    }

    companion object {
        private const val DELAY_SCANNING_IN_MS = 1500L
    }
}

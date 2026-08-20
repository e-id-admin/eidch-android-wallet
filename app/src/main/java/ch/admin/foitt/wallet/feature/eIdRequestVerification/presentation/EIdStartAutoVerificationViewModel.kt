package ch.admin.foitt.wallet.feature.eIdRequestVerification.presentation

import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.model.StartAutoVerificationUiState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.AutoVerificationResponse
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.StartAutoVerificationError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.AbortSIdProcess
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.FetchSIdStatus
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.SetStartAutoVerificationResult
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.StartAutoVerification
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.VerifyWalletPairing
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.trackCompletion
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.annotation.UnsafeResultValueAccess
import com.github.michaelbull.result.getError
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import com.github.michaelbull.result.unwrapError
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = EIdStartAutoVerificationViewModel.Factory::class)
internal class EIdStartAutoVerificationViewModel @AssistedInject constructor(
    private val startAutoVerification: StartAutoVerification,
    private val fetchSIdStatus: FetchSIdStatus,
    private val verifyWalletPairing: VerifyWalletPairing,
    private val abortSIdProcess: AbortSIdProcess,
    private val navManager: NavigationManager,
    private val setStartAutoVerificationResult: SetStartAutoVerificationResult,
    @Assisted private val caseId: String,
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {

    @AssistedFactory
    interface Factory {
        fun create(caseId: String): EIdStartAutoVerificationViewModel
    }

    override val topBarState = TopBarState.Empty

    private val isLoading = MutableStateFlow(false)
    private val startAutoVerificationResult =
        MutableStateFlow<Result<AutoVerificationResponse, StartAutoVerificationError>?>(null)

    @OptIn(UnsafeResultValueAccess::class)
    val state: StateFlow<StartAutoVerificationUiState> = combine(
        isLoading,
        startAutoVerificationResult,
    ) { isLoading, startAutoVerificationResult ->
        when {
            isLoading -> StartAutoVerificationUiState.Loading
            startAutoVerificationResult == null -> StartAutoVerificationUiState.Info(
                onStart = ::onStart
            )

            startAutoVerificationResult.isOk -> StartAutoVerificationUiState.Started(
                onContinue = { onContinue(startAutoVerificationResult.value) }
            )

            startAutoVerificationResult.unwrapError() is EIdRequestError.NetworkError ->
                StartAutoVerificationUiState.NetworkError(
                    onClose = ::onClose,
                    onRetry = ::onRetry
                )

            startAutoVerificationResult.isErr && startAutoVerificationResult.getError() == EIdRequestError.UnauthorizedPairing ->
                StartAutoVerificationUiState.UnauthorizedPairing(
                    onClose = ::onSecurityErrorClose
                )

            else -> StartAutoVerificationUiState.Unexpected(
                onClose = ::onClose,
                onRetry = ::onRetry,
            )
        }
    }.toStateFlow(StartAutoVerificationUiState.Loading)

    private suspend fun onStartAv() {
        startAutoVerification(caseId = caseId)
            .onOk { response ->
                fetchSIdStatus(caseId).onOk { stateResponse ->
                    verifyWalletPairing(caseId, stateResponse).onOk {
                        setStartAutoVerificationResult(startAutoVerificationResult = response)
                        startAutoVerificationResult.value = Ok(response)
                        onContinue(autoVerificationResponse = response)
                    }.onErr { error ->
                        val mappedError: StartAutoVerificationError = when (error) {
                            is EIdRequestError.UnauthorizedPairing -> error
                            else -> EIdRequestError.Unexpected(null)
                        }
                        startAutoVerificationResult.value = Err(mappedError)
                    }
                }.onErr {
                    startAutoVerificationResult.value = Err(EIdRequestError.NetworkError)
                }
            }
            .onErr { error ->
                startAutoVerificationResult.value = Err(error)
            }
    }

    private fun onSecurityErrorClose() {
        viewModelScope.launch {
            abortSIdProcess(caseId)
            navManager.navigateBackToHomeScreen(popUntil = Destination.EIdIntroScreen::class)
        }
    }

    private fun onStart() {
        if (isLoading.value) {
            return
        }
        viewModelScope.launch {
            onStartAv()
        }.trackCompletion(isLoading)
    }

    private fun onClose() = navManager.popBackStackTo(Destination.HomeScreen::class, false)

    private fun onRetry() = onStart()

    private fun onContinue(
        autoVerificationResponse: AutoVerificationResponse,
    ) = handleAutoVerificationResponse(
        useNfc = autoVerificationResponse.useNfc,
        recordDocumentVideo = autoVerificationResponse.recordDocumentVideo,
        scanDocument = autoVerificationResponse.scanDocument,
    ).let { navManager.navigateTo(it) }

    private fun handleAutoVerificationResponse(
        useNfc: Boolean,
        recordDocumentVideo: Boolean,
        scanDocument: Boolean,
    ): Destination = when {
        useNfc -> Destination.EIdNfcScannerScreen(caseId = caseId)
        scanDocument -> Destination.EIdDocumentScannerInfoScreen(caseId = caseId)
        recordDocumentVideo -> Destination.EIdDocumentRecordingInfoScreen(caseId = caseId)
        // At this point, default is to do a face scan
        else -> Destination.EIdStartSelfieVideoScreen(caseId = caseId)
    }
}

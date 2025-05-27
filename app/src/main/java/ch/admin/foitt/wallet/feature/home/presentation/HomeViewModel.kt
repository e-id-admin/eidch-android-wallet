package ch.admin.foitt.wallet.feature.home.presentation

import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.home.domain.usecase.DeleteEIdRequestCase
import ch.admin.foitt.wallet.feature.home.domain.usecase.GetEIdRequestsFlow
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialDisplayData
import ch.admin.foitt.wallet.platform.credential.presentation.adapter.GetCredentialCardState
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.UpdateAllCredentialStatuses
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.SIdRequestDisplayData
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.SIdRequestDisplayStatus
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.UpdateAllSIdStatuses
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.messageEvents.domain.model.CredentialOfferEvent
import ch.admin.foitt.wallet.platform.messageEvents.domain.repository.CredentialOfferEventRepository
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.GetCredentialsWithDisplaysFlow
import ch.admin.foitt.wallet.platform.utils.trackCompletion
import ch.admin.foitt.walletcomposedestinations.destinations.BetaIdScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.CredentialDetailScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.EIdGuardianSelectionScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.EIdIntroScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.EIdWalletPairingScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.ErrorScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.QrScanPermissionScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.SettingsScreenDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    getCredentialsWithDisplaysFlow: GetCredentialsWithDisplaysFlow,
    getEIdRequestsFlow: GetEIdRequestsFlow,
    private val getCredentialCardState: GetCredentialCardState,
    private val updateAllCredentialStatuses: UpdateAllCredentialStatuses,
    private val updateAllSIdStatuses: UpdateAllSIdStatuses,
    private val deleteEIdRequestCase: DeleteEIdRequestCase,
    private val environmentSetupRepository: EnvironmentSetupRepository,
    private val navManager: NavigationManager,
    private val credentialOfferEventRepository: CredentialOfferEventRepository,
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {
    override val topBarState = TopBarState.None

    private val _eventMessage = MutableStateFlow<Int?>(null)
    val eventMessage = _eventMessage.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    val screenState: StateFlow<HomeScreenState> = combine(
        getCredentialsWithDisplaysFlow(),
        getEIdRequestsFlow()
    ) { homeDataFlow, eIdRequestsFlow ->
        when {
            homeDataFlow.isOk && eIdRequestsFlow.isOk -> mapToUiState(homeDataFlow.value, eIdRequestsFlow.value)
            else -> {
                navigateToErrorScreen()
                null
            }
        }
    }.filterNotNull()
        .toStateFlow(HomeScreenState.Initial)

    init {
        viewModelScope.launch {
            credentialOfferEventRepository.event.collect { event ->
                _eventMessage.value = when (event) {
                    CredentialOfferEvent.ACCEPTED -> R.string.tk_home_notification_credential_accepted
                    CredentialOfferEvent.DECLINED -> R.string.tk_home_notification_credential_declined
                    CredentialOfferEvent.NONE -> null
                }
                if (_eventMessage.value != null) {
                    delay(4000L)
                    credentialOfferEventRepository.resetEvent()
                }
            }
        }
    }

    fun onCloseToast() {
        _eventMessage.value = null
        credentialOfferEventRepository.resetEvent()
    }

    private suspend fun mapToUiState(
        credentials: List<CredentialDisplayData>,
        eIdRequestCases: List<SIdRequestDisplayData>
    ): HomeScreenState = when {
        credentials.isNotEmpty() -> {
            HomeScreenState.CredentialList(
                eIdRequests = filterEIdRequests(eIdRequestCases),
                credentials = getCredentialStateList(credentials),
                onCredentialClick = ::onCredentialPreviewClick,
            )
        }

        else -> HomeScreenState.NoCredential(
            eIdRequests = filterEIdRequests(eIdRequestCases),
            showBetaIdRequestButton = environmentSetupRepository.betaIdRequestEnabled,
            showEIdRequestButton = environmentSetupRepository.eIdRequestEnabled,
        )
    }

    private fun filterEIdRequests(eIdRequestCases: List<SIdRequestDisplayData>): List<SIdRequestDisplayData> {
        return eIdRequestCases.filter { requestCase ->
            requestCase.status != SIdRequestDisplayStatus.OTHER
        }
    }

    private suspend fun getCredentialStateList(credentialDisplayData: List<CredentialDisplayData>) = credentialDisplayData
        .map { credentialPreview -> getCredentialCardState(credentialPreview) }

    fun onStartOnlineIdentification() = navManager.navigateTo(EIdWalletPairingScreenDestination)

    fun onCloseEId(caseId: String) {
        viewModelScope.launch {
            deleteEIdRequestCase(caseId)
        }
    }

    fun onQrScan() = navManager.navigateTo(QrScanPermissionScreenDestination)

    fun onMenu() = navManager.navigateTo(SettingsScreenDestination)

    fun onRefresh() {
        viewModelScope.launch {
            updateAllCredentialStatuses()
            updateAllSIdStatuses()
        }.trackCompletion(_isRefreshing)
    }

    fun onRefreshSIdStatuses() {
        if (!isRefreshing.value) {
            viewModelScope.launch {
                updateAllSIdStatuses()
            }.trackCompletion(_isRefreshing)
        }
    }

    fun onObtainConsent(caseId: String) {
        navManager.navigateTo(EIdGuardianSelectionScreenDestination(sIdCaseId = caseId))
    }

    private fun onCredentialPreviewClick(credentialId: Long) {
        navManager.navigateTo(CredentialDetailScreenDestination(credentialId = credentialId))
    }

    private fun navigateToErrorScreen() {
        navManager.navigateToAndClearCurrent(ErrorScreenDestination)
    }

    fun onGetEId() {
        navManager.navigateTo(EIdIntroScreenDestination)
    }

    fun onGetBetaId() {
        navManager.navigateTo(BetaIdScreenDestination)
    }
}

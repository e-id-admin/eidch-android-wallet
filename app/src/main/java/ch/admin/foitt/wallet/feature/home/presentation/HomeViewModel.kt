package ch.admin.foitt.wallet.feature.home.presentation

import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.feature.home.domain.usecase.GetHomeDataFlow
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialPreview
import ch.admin.foitt.wallet.platform.credential.presentation.adapter.GetCredentialCardState
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.UpdateAllCredentialStatuses
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestCaseWithState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.toEIdRequest
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.GetEIdRequestsFlow
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.UpdateAllSIdStatuses
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetCurrentAppLocale
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.scaffold.domain.model.FullscreenState
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetFullscreenState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.trackCompletion
import ch.admin.foitt.walletcomposedestinations.destinations.BetaIdScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.CredentialDetailScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.EIdIntroScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.EIdWalletPairingScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.ErrorScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.QrScanPermissionScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.SettingsScreenDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    getHomeDataFlow: GetHomeDataFlow,
    getEIdRequestsFlow: GetEIdRequestsFlow,
    private val getCredentialCardState: GetCredentialCardState,
    private val updateAllCredentialStatuses: UpdateAllCredentialStatuses,
    private val updateAllSIdStatuses: UpdateAllSIdStatuses,
    private val environmentSetupRepository: EnvironmentSetupRepository,
    private val getCurrentAppLocale: GetCurrentAppLocale,
    private val navManager: NavigationManager,
    setTopBarState: SetTopBarState,
    setFullscreenState: SetFullscreenState,
) : ScreenViewModel(setTopBarState, setFullscreenState) {
    override val topBarState = TopBarState.None
    override val fullscreenState = FullscreenState.Fullscreen

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    val screenState: StateFlow<HomeScreenState> = combine(
        getHomeDataFlow(),
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

    private suspend fun mapToUiState(
        credentials: List<CredentialPreview>,
        eIdRequestCasesWithStates: List<EIdRequestCaseWithState>
    ): HomeScreenState = when {
        credentials.isNotEmpty() -> {
            HomeScreenState.CredentialList(
                eIdRequests = eIdRequestCasesWithStates.map { it.toEIdRequest(getCurrentAppLocale()) },
                credentials = getCredentialStateList(credentials),
                onCredentialClick = ::onCredentialPreviewClick,
            )
        }
        else -> HomeScreenState.NoCredential(
            eIdRequests = eIdRequestCasesWithStates.map { it.toEIdRequest(getCurrentAppLocale()) },
            showBetaIdRequestButton = environmentSetupRepository.betaIdRequestEnabled,
            showEIdRequestButton = environmentSetupRepository.eIdRequestEnabled,
        )
    }

    private suspend fun getCredentialStateList(credentialPreviews: List<CredentialPreview>) = credentialPreviews
        .map { credentialPreview -> getCredentialCardState(credentialPreview) }

    fun onStartOnlineIdentification() = navManager.navigateTo(EIdWalletPairingScreenDestination)

    fun onQrScan() = navManager.navigateTo(QrScanPermissionScreenDestination)

    fun onMenu() = navManager.navigateTo(SettingsScreenDestination)

    fun onRefresh() {
        viewModelScope.launch {
            updateAllCredentialStatuses()
            updateAllSIdStatuses()
        }.trackCompletion(_isRefreshing)
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

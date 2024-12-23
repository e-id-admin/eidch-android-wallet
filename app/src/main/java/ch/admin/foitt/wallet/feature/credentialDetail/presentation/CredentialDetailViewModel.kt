package ch.admin.foitt.wallet.feature.credentialDetail.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.feature.credentialDetail.domain.model.CredentialDetail
import ch.admin.foitt.wallet.feature.credentialDetail.domain.usecase.GetCredentialDetailFlow
import ch.admin.foitt.wallet.feature.credentialDetail.presentation.composables.VisibleBottomSheet
import ch.admin.foitt.wallet.feature.credentialDetail.presentation.model.CredentialDetailUiState
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.model.ActorUiState
import ch.admin.foitt.wallet.platform.composables.presentation.adapter.GetDrawableFromUri
import ch.admin.foitt.wallet.platform.credential.presentation.adapter.GetCredentialCardState
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.UpdateCredentialStatus
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialIssuerDisplay
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.scaffold.domain.model.FullscreenState
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetFullscreenState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.DeleteCredential
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.platform.utils.toPainter
import ch.admin.foitt.walletcomposedestinations.destinations.CredentialDetailScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.CredentialWrongDataScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.ErrorScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.HomeScreenDestination
import com.github.michaelbull.result.mapBoth
import com.github.michaelbull.result.onFailure
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CredentialDetailViewModel @Inject constructor(
    getCredentialDetailFlow: GetCredentialDetailFlow,
    private val getCredentialCardState: GetCredentialCardState,
    private val updateCredentialStatus: UpdateCredentialStatus,
    private val getDrawableFromUri: GetDrawableFromUri,
    private val navManager: NavigationManager,
    private val deleteCredential: DeleteCredential,
    setTopBarState: SetTopBarState,
    setFullscreenState: SetFullscreenState,
    savedStateHandle: SavedStateHandle
) : ScreenViewModel(setTopBarState, setFullscreenState) {
    override val topBarState = TopBarState.None
    override val fullscreenState = FullscreenState.Fullscreen

    private val navArgs = CredentialDetailScreenDestination.argsFrom(savedStateHandle)

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _visibleBottomSheet = MutableStateFlow(VisibleBottomSheet.NONE)
    val visibleBottomSheet = _visibleBottomSheet.asStateFlow()

    val credentialDetailUiState: StateFlow<CredentialDetailUiState> = getCredentialDetailFlow(navArgs.credentialId).map { result ->
        result.mapBoth(
            success = {
                _isLoading.value = false
                mapToUiState(result.value)
            },
            failure = {
                navigateToErrorScreen()
                null
            },
        )
    }.filterNotNull()
        .toStateFlow(CredentialDetailUiState.EMPTY)

    private suspend fun mapToUiState(credentialDetail: CredentialDetail?) = when (credentialDetail) {
        null -> CredentialDetailUiState.EMPTY
        else -> CredentialDetailUiState(
            credential = getCredentialCardState(credentialDetail.credential),
            claims = credentialDetail.claims,
            issuer = credentialDetail.issuer.toIssuerUiState(),
        )
    }

    private fun toggleMenuBottomSheet() = when (_visibleBottomSheet.value) {
        VisibleBottomSheet.NONE -> _visibleBottomSheet.value = VisibleBottomSheet.MENU
        VisibleBottomSheet.MENU -> _visibleBottomSheet.value = VisibleBottomSheet.NONE
        else -> {}
    }

    init {
        viewModelScope.launch {
            updateCredentialStatus(navArgs.credentialId)
        }
    }

    fun onBack() {
        navManager.navigateUp()
    }

    fun onMenu() {
        toggleMenuBottomSheet()
    }

    fun onDelete() {
        _visibleBottomSheet.value = VisibleBottomSheet.DELETE
    }

    fun onDeleteCredential() {
        _visibleBottomSheet.value = VisibleBottomSheet.NONE
        viewModelScope.launch {
            deleteCredential(credentialId = navArgs.credentialId).onFailure { error ->
                when (error) {
                    is SsiError.Unexpected -> Timber.e(error.cause)
                }
            }
            navManager.popBackStackTo(HomeScreenDestination, false)
        }
    }

    fun onBottomSheetDismiss() {
        _visibleBottomSheet.value = VisibleBottomSheet.NONE
    }

    private fun navigateToErrorScreen() {
        navManager.navigateToAndClearCurrent(ErrorScreenDestination)
    }

    fun onWrongData() {
        navManager.navigateTo(CredentialWrongDataScreenDestination)
    }

    private suspend fun CredentialIssuerDisplay.toIssuerUiState() = ActorUiState(
        name = name,
        painter = getDrawableFromUri(image)?.toPainter(),
        trustStatus = TrustStatus.UNKNOWN,
    )
}

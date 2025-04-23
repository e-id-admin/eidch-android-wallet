package ch.admin.foitt.wallet.feature.credentialDetail.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.feature.credentialDetail.domain.model.IssuerDisplay
import ch.admin.foitt.wallet.feature.credentialDetail.domain.usecase.GetCredentialIssuerDisplaysFlow
import ch.admin.foitt.wallet.feature.credentialDetail.presentation.composables.VisibleBottomSheet
import ch.admin.foitt.wallet.feature.credentialDetail.presentation.model.CredentialDetailUiState
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorType
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.model.ActorUiState
import ch.admin.foitt.wallet.platform.composables.presentation.adapter.GetDrawableFromUri
import ch.admin.foitt.wallet.platform.credential.presentation.adapter.GetCredentialCardState
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.UpdateCredentialStatus
import ch.admin.foitt.wallet.platform.database.domain.model.DisplayConst
import ch.admin.foitt.wallet.platform.database.domain.model.DisplayLanguage
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.scaffold.domain.model.FullscreenState
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetFullscreenState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialDetail
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.DeleteCredential
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.GetCredentialDetailFlow
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.platform.utils.toPainter
import ch.admin.foitt.walletcomposedestinations.destinations.CredentialDetailScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.CredentialDetailWrongDataScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.ErrorScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.HomeScreenDestination
import com.github.michaelbull.result.get
import com.github.michaelbull.result.onFailure
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CredentialDetailViewModel @Inject constructor(
    getCredentialDetailFlow: GetCredentialDetailFlow,
    getCredentialIssuerDisplaysFlow: GetCredentialIssuerDisplaysFlow,
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

    val credentialDetailUiState: StateFlow<CredentialDetailUiState> = combine(
        getCredentialDetailFlow(navArgs.credentialId),
        getCredentialIssuerDisplaysFlow(navArgs.credentialId)
    ) { detailsResult, issuerDisplayResult ->
        when {
            detailsResult.isOk -> {
                _isLoading.value = false
                mapToUiState(detailsResult.value, issuerDisplayResult.get())
            }
            else -> {
                navigateToErrorScreen()
                null
            }
        }
    }.filterNotNull()
        .toStateFlow(CredentialDetailUiState.EMPTY)

    private suspend fun mapToUiState(
        credentialDetail: CredentialDetail?,
        issuerDisplay: IssuerDisplay?,
    ) = when (credentialDetail) {
        null -> CredentialDetailUiState.EMPTY
        else -> CredentialDetailUiState(
            credential = getCredentialCardState(credentialDetail.credential),
            claims = credentialDetail.claims,
            issuer = issuerDisplay.toActorUiState(),
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
        navManager.navigateTo(CredentialDetailWrongDataScreenDestination)
        onBottomSheetDismiss()
    }

    private suspend fun IssuerDisplay?.toActorUiState() = this?.let {
        ActorUiState(
            name = if (locale == DisplayLanguage.FALLBACK) {
                null
            } else if (name == DisplayConst.ISSUER_FALLBACK_NAME) {
                null
            } else {
                name
            },
            painter = getDrawableFromUri(image)?.toPainter(),
            trustStatus = TrustStatus.UNKNOWN,
            actorType = ActorType.ISSUER,
        )
    } ?: ActorUiState.EMPTY.copy(actorType = ActorType.ISSUER)
}

package ch.admin.foitt.wallet.platform.invitation.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.HandleInvitationProcessingError
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.HandleInvitationProcessingSuccess
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.ProcessInvitation
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.scaffold.domain.model.FullscreenState
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetFullscreenState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.extension.navigateUpOrToRoot
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.trackCompletion
import ch.admin.foitt.walletcomposedestinations.destinations.NoInternetConnectionScreenDestination
import com.github.michaelbull.result.mapEither
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoInternetConnectionViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val navManager: NavigationManager,
    private val processInvitation: ProcessInvitation,
    private val handleInvitationProcessingSuccess: HandleInvitationProcessingSuccess,
    private val handleInvitationProcessingError: HandleInvitationProcessingError,
    setTopBarState: SetTopBarState,
    setFullscreenState: SetFullscreenState,
) : ScreenViewModel(setTopBarState, setFullscreenState) {

    override val topBarState = TopBarState.SystemBarPadding
    override val fullscreenState = FullscreenState.Insets

    private val navArgs = NoInternetConnectionScreenDestination.argsFrom(savedStateHandle)
    private val invitationUri = navArgs.invitationUri

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    fun retry() {
        viewModelScope.launch {
            processInvitation(invitationUri).mapEither(
                success = { invitation ->
                    handleInvitationProcessingSuccess(invitation).navigate()
                },
                failure = { invitationError ->
                    handleInvitationProcessingError(invitationError, invitationUri).navigate()
                },
            )
        }.trackCompletion(_isLoading)
    }

    fun close() = navManager.navigateUpOrToRoot()
}

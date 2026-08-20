package ch.admin.foitt.wallet.platform.invitation.presentation

import android.content.Context
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationResponseErrorBody
import ch.admin.foitt.openid4vc.domain.usecase.DeclinePresentation
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.ValidateRedirectUri
import ch.admin.foitt.wallet.platform.genericScreens.domain.model.GenericErrorScreenState
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationErrorScreenState
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.proximity.domain.usecase.GetProximityRepositoryForScope
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.openLink
import com.github.michaelbull.result.onErr
import com.github.michaelbull.result.onOk
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = InvitationFailureViewModel.Factory::class)
class InvitationFailureViewModel @AssistedInject constructor(
    @param:ApplicationContext private val appContext: Context,
    private val navManager: NavigationManager,
    private val declinePresentation: DeclinePresentation,
    private val validateRedirectUri: ValidateRedirectUri,
    private val getProximityRepositoryForScope: GetProximityRepositoryForScope,
    setTopBarState: SetTopBarState,
    @Assisted val invitationErrorScreenState: InvitationErrorScreenState,
    @Assisted("responseUri") private val responseUri: String?,
    @Assisted("state") private val state: String?,
) : ScreenViewModel(setTopBarState) {

    @AssistedFactory
    interface Factory {
        fun create(
            invitationErrorScreenState: InvitationErrorScreenState,
            @Assisted("responseUri") responseUri: String?,
            @Assisted("state") state: String?
        ): InvitationFailureViewModel
    }

    override val topBarState = TopBarState.None

    fun close() = handleErrorType(responseUri, state)

    private fun handleErrorType(responseUri: String?, state: String?) = viewModelScope.launch {
        when (invitationErrorScreenState) {
            InvitationErrorScreenState.EMPTY_WALLET,
            InvitationErrorScreenState.NO_COMPATIBLE_CREDENTIAL -> {
                if (responseUri != null) {
                    rejectPresentation(responseUri, state)
                } else {
                    getProximityRepositoryForScope().decline()
                }
            }
            else -> {}
        }
        navManager.popBackStackOrToRoot()
    }

    private fun rejectPresentation(
        responseUri: String,
        state: String?,
    ) = viewModelScope.launch {
        declinePresentation(
            url = responseUri,
            reason = AuthorizationResponseErrorBody.ErrorType.ACCESS_DENIED,
            state = state,
        ).onOk { authorizationResponseResponse ->
            authorizationResponseResponse.redirectUri?.let {
                handleRedirectUri(it)
            } ?: backToHome()
        }.onErr {
            backToHome()
        }
    }

    private fun handleRedirectUri(redirectUri: String) = validateRedirectUri(redirectUri)
        .onOk {
            appContext.openLink(redirectUri)
            backToHome()
        }.onErr {
            navManager.replaceCurrentWith(Destination.GenericErrorScreen(GenericErrorScreenState.General.invalidRedirectUri()))
        }

    private fun backToHome() = navManager.navigateBackToHomeScreen(Destination.GenericErrorScreen::class)
}

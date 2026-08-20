package ch.admin.foitt.wallet.platform.genericScreens.presentation

import android.content.Context
import androidx.lifecycle.viewModelScope
import ch.admin.foitt.openid4vc.domain.usecase.DeclinePresentation
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.ValidateRedirectUri
import ch.admin.foitt.wallet.platform.genericScreens.domain.model.DeclineData
import ch.admin.foitt.wallet.platform.genericScreens.domain.model.GenericErrorScreenState
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
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

@HiltViewModel(assistedFactory = GenericErrorViewModel.Factory::class)
class GenericErrorViewModel @AssistedInject constructor(
    private val declinePresentation: DeclinePresentation,
    private val validateRedirectUri: ValidateRedirectUri,
    private val navManager: NavigationManager,
    @param:ApplicationContext private val appContext: Context,
    @Assisted private val errorScreenState: GenericErrorScreenState,
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {
    override val topBarState = TopBarState.Empty

    @AssistedFactory
    interface Factory {
        fun create(error: GenericErrorScreenState): GenericErrorViewModel
    }

    val image = when (errorScreenState) {
        is GenericErrorScreenState.Error -> errorScreenState.image
        is GenericErrorScreenState.PresentationError -> errorScreenState.image
    }

    val title = when (errorScreenState) {
        is GenericErrorScreenState.Error -> errorScreenState.title
        is GenericErrorScreenState.PresentationError -> errorScreenState.title
    }

    val subtitle = when (errorScreenState) {
        is GenericErrorScreenState.Error -> errorScreenState.subtitle
        is GenericErrorScreenState.PresentationError -> errorScreenState.subtitle
    }

    val errorText = when (errorScreenState) {
        is GenericErrorScreenState.Error -> errorScreenState.errorText
        is GenericErrorScreenState.PresentationError -> errorScreenState.errorText
    }

    val errorDescription = when (errorScreenState) {
        is GenericErrorScreenState.Error -> errorScreenState.errorDescription
        is GenericErrorScreenState.PresentationError -> errorScreenState.errorDescription
    }

    fun onClick() = when (errorScreenState) {
        is GenericErrorScreenState.Error -> {
            if (errorScreenState.declineData != null) {
                rejectPresentation(errorScreenState.declineData)
            } else {
                backToHome()
            }
        }

        is GenericErrorScreenState.PresentationError -> backToHome()
    }

    private fun rejectPresentation(declineData: DeclineData) = viewModelScope.launch {
        declinePresentation(
            url = declineData.responseUri,
            reason = declineData.reason,
            state = declineData.state,
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

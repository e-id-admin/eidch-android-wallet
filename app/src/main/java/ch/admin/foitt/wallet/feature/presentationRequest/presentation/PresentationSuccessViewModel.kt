package ch.admin.foitt.wallet.feature.presentationRequest.presentation

import android.content.Context
import androidx.annotation.StringRes
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.GetActorForScope
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.adapter.GetActorUiState
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.model.ActorUiState
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.model.toBadgeBottomSheetUiState
import ch.admin.foitt.wallet.platform.badges.presentation.model.BadgeBottomSheetUiState
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.ValidateRedirectUri
import ch.admin.foitt.wallet.platform.genericScreens.domain.model.GenericErrorScreenState
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.ComponentScope
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

@HiltViewModel(assistedFactory = PresentationSuccessViewModel.Factory::class)
class PresentationSuccessViewModel @AssistedInject constructor(
    @param:ApplicationContext private val appContext: Context,
    private val navManager: NavigationManager,
    private val getActorUiState: GetActorUiState,
    getActorForScope: GetActorForScope,
    private val validateRedirectUri: ValidateRedirectUri,
    setTopBarState: SetTopBarState,
    @Assisted val redirectUri: String?,
) : ScreenViewModel(setTopBarState) {
    @AssistedFactory
    interface Factory {
        fun create(redirectUri: String?): PresentationSuccessViewModel
    }

    override val topBarState = TopBarState.None

    private val _badgeBottomSheetUiState: MutableStateFlow<BadgeBottomSheetUiState?> = MutableStateFlow(null)
    val badgeBottomSheet = _badgeBottomSheetUiState.asStateFlow()

    private val verifierDisplayData = getActorForScope(ComponentScope.Verifier)
    val verifierUiState = verifierDisplayData.map {
        getActorUiState(actorDisplayData = it)
    }.toStateFlow(ActorUiState.EMPTY, 0)

    fun onClose() = if (redirectUri != null) {
        validateRedirectUri(redirectUri)
            .onOk {
                appContext.openLink(redirectUri)
                navManager.popBackStackOrToRoot()
            }.onErr {
                navManager.replaceCurrentWith(Destination.GenericErrorScreen(GenericErrorScreenState.General.invalidRedirectUri()))
            }
    } else {
        navManager.popBackStackOrToRoot()
    }

    fun onActorNameTap() {
        _badgeBottomSheetUiState.value = verifierUiState.value.toBadgeBottomSheetUiState {
            onMoreInformation(R.string.tk_badgeInformation_furtherInformation_link_value)
        }
    }

    fun onReportedActorInfo() {
        _badgeBottomSheetUiState.value = BadgeBottomSheetUiState.NonCompliantActor(
            actorName = verifierUiState.value.name ?: "",
            actorPainter = verifierUiState.value.painter,
            reason = verifierUiState.value.nonComplianceReason,
            onMoreInformation = { onMoreInformation(R.string.tk_badgeInformation_furtherInformation_link_value) },
        )
    }

    fun onDismissBottomSheet() {
        _badgeBottomSheetUiState.value = null
    }

    private fun onMoreInformation(@StringRes uriResource: Int) = appContext.openLink(uriResource)
}

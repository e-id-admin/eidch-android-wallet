package ch.admin.foitt.wallet.feature.presentationRequest.presentation

import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CompatibleCredential
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.PresentationRequestWithRaw
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel

@HiltViewModel(assistedFactory = PresentationRequestReviewViewModel.Factory::class)
class PresentationRequestReviewViewModel @AssistedInject constructor(
    private val navManager: NavigationManager,
    setTopBarState: SetTopBarState,
    @Assisted private val compatibleCredentials: Set<CompatibleCredential>,
    @Assisted private val presentationRequestWithRaw: PresentationRequestWithRaw,
) : ScreenViewModel(setTopBarState) {

    @AssistedFactory
    interface Factory {
        fun create(
            compatibleCredentials: Set<CompatibleCredential>,
            presentationRequestWithRaw: PresentationRequestWithRaw,
        ): PresentationRequestReviewViewModel
    }

    override val topBarState: TopBarState = TopBarState.None

    fun onProceed() {
        if (compatibleCredentials.size == 1) {
            navManager.replaceCurrentWith(
                Destination.PresentationRequestScreen(
                    compatibleCredential = compatibleCredentials.first(),
                    presentationRequestWithRaw = presentationRequestWithRaw,
                )
            )
        } else {
            navManager.replaceCurrentWith(
                Destination.PresentationCredentialListScreen(
                    compatibleCredentials = compatibleCredentials,
                    presentationRequestWithRaw = presentationRequestWithRaw,
                )
            )
        }
    }

    fun onCancel() {
        navManager.popBackStackOrToRoot()
    }
}

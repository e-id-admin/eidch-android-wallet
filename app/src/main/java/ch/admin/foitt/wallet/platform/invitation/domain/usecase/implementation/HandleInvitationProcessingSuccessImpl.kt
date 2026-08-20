package ch.admin.foitt.wallet.platform.invitation.domain.usecase.implementation

import androidx.annotation.CheckResult
import ch.admin.foitt.wallet.platform.invitation.domain.model.ProcessInvitationResult
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.HandleInvitationProcessingSuccess
import ch.admin.foitt.wallet.platform.messageEvents.domain.model.CredentialEvent
import ch.admin.foitt.wallet.platform.messageEvents.domain.repository.CredentialEventRepository
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.navigation.domain.model.NavigationAction
import javax.inject.Inject

class HandleInvitationProcessingSuccessImpl @Inject constructor(
    private val navManager: NavigationManager,
    private val credentialEventRepository: CredentialEventRepository,
) : HandleInvitationProcessingSuccess {
    @CheckResult
    override suspend operator fun invoke(successResult: ProcessInvitationResult): NavigationAction =
        when (successResult) {
            is ProcessInvitationResult.CredentialOffer -> NavigationAction { navigateToCredentialOffer(successResult) }
            is ProcessInvitationResult.PresentationRequest -> NavigationAction { navigateToPresentationRequest(successResult) }
            is ProcessInvitationResult.PresentationRequestReview -> NavigationAction {
                navigateToPresentationRequestReview(successResult)
            }
            is ProcessInvitationResult.PresentationRequestCredentialList -> NavigationAction {
                navigateToPresentationCredentialList(successResult)
            }
            is ProcessInvitationResult.DeferredCredential -> {
                NavigationAction { navigateToHome() }
            }
        }

    private fun navigateToCredentialOffer(
        credentialOffer: ProcessInvitationResult.CredentialOffer,
    ) = navManager.replaceCurrentWith(
        Destination.CredentialOfferScreen(credentialId = credentialOffer.credentialId)
    )

    private fun navigateToPresentationRequest(
        presentationRequest: ProcessInvitationResult.PresentationRequest
    ) = navManager.replaceCurrentWith(
        Destination.PresentationRequestScreen(
            compatibleCredential = presentationRequest.credential,
            presentationRequestWithRaw = presentationRequest.request,
        )
    )

    private fun navigateToPresentationRequestReview(
        presentationRequest: ProcessInvitationResult.PresentationRequestReview
    ) = navManager.replaceCurrentWith(
        Destination.PresentationRequestReviewScreen(
            compatibleCredentials = presentationRequest.credentials,
            presentationRequestWithRaw = presentationRequest.request,
        )
    )

    private fun navigateToPresentationCredentialList(
        presentationRequest: ProcessInvitationResult.PresentationRequestCredentialList,
    ) = navManager.replaceCurrentWith(
        Destination.PresentationCredentialListScreen(
            compatibleCredentials = presentationRequest.credentials,
            presentationRequestWithRaw = presentationRequest.request,
        )
    )

    private fun navigateToHome() {
        credentialEventRepository.setEvent(CredentialEvent.ACCEPTED)
        navManager.navigateBackToHomeScreen(popUntil = Destination.ShowOrScanQrCodeScreen::class)
    }
}

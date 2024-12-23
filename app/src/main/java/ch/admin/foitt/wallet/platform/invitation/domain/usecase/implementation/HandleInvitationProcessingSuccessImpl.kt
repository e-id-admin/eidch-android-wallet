package ch.admin.foitt.wallet.platform.invitation.domain.usecase.implementation

import androidx.annotation.CheckResult
import ch.admin.foitt.wallet.platform.invitation.domain.model.ProcessInvitationResult
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.HandleInvitationProcessingSuccess
import ch.admin.foitt.wallet.platform.navArgs.domain.model.CredentialOfferNavArg
import ch.admin.foitt.wallet.platform.navArgs.domain.model.PresentationCredentialListNavArg
import ch.admin.foitt.wallet.platform.navArgs.domain.model.PresentationRequestNavArg
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.NavigationAction
import ch.admin.foitt.walletcomposedestinations.destinations.CredentialOfferScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.PresentationCredentialListScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.PresentationRequestScreenDestination
import javax.inject.Inject

class HandleInvitationProcessingSuccessImpl @Inject constructor(
    private val navManager: NavigationManager,
) : HandleInvitationProcessingSuccess {
    @CheckResult
    override suspend operator fun invoke(successResult: ProcessInvitationResult) = NavigationAction {
        when (successResult) {
            is ProcessInvitationResult.CredentialOffer -> navigateToCredentialOffer(successResult)
            is ProcessInvitationResult.PresentationRequest -> navigateToPresentationRequest(successResult)
            is ProcessInvitationResult.PresentationRequestCredentialList -> navigateToPresentationCredentialList(successResult)
        }
    }

    private fun navigateToCredentialOffer(
        credentialOffer: ProcessInvitationResult.CredentialOffer,
    ) = navManager.navigateToAndClearCurrent(
        CredentialOfferScreenDestination(
            CredentialOfferNavArg(
                credentialOffer.credentialId
            )
        )
    )

    private fun navigateToPresentationRequest(
        presentationRequest: ProcessInvitationResult.PresentationRequest
    ) = navManager.navigateToAndClearCurrent(
        PresentationRequestScreenDestination(
            PresentationRequestNavArg(
                presentationRequest.credential,
                presentationRequest.request,
            )
        )
    )

    private fun navigateToPresentationCredentialList(
        presentationRequest: ProcessInvitationResult.PresentationRequestCredentialList,
    ) = navManager.navigateToAndClearCurrent(
        PresentationCredentialListScreenDestination(
            PresentationCredentialListNavArg(
                presentationRequest.credentials.toTypedArray(),
                presentationRequest.request,
            )
        )
    )
}

package ch.admin.foitt.wallet.platform.invitation.domain.usecase.implementation

import androidx.annotation.CheckResult
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestErrorBody
import ch.admin.foitt.openid4vc.domain.usecase.DeclinePresentation
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError
import ch.admin.foitt.wallet.platform.invitation.domain.model.ProcessInvitationError
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.HandleInvitationProcessingError
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.NavigationAction
import ch.admin.foitt.walletcomposedestinations.destinations.InvalidCredentialErrorScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.InvitationFailureScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.NoInternetConnectionScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.PresentationEmptyWalletScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.PresentationNoMatchScreenDestination
import javax.inject.Inject

class HandleInvitationProcessingErrorImpl @Inject constructor(
    private val navManager: NavigationManager,
    private val declinePresentation: DeclinePresentation,
) : HandleInvitationProcessingError {
    @CheckResult
    override suspend operator fun invoke(
        failureResult: ProcessInvitationError,
        invitationUri: String,
    ): NavigationAction {
        handleErrorActions(failureResult)
        return mapToNavigationAction(failureResult, invitationUri)
    }

    private suspend fun handleErrorActions(failureResult: ProcessInvitationError) {
        when (failureResult) {
            is InvitationError.InvalidPresentation -> declinePresentationRequest(failureResult.responseUri)
            else -> {}
        }
    }

    private suspend fun declinePresentationRequest(url: String) = declinePresentation(
        url = url,
        reason = PresentationRequestErrorBody.ErrorType.INVALID_REQUEST
    )

    private fun mapToNavigationAction(failureResult: ProcessInvitationError, invitationUri: String) = NavigationAction {
        when (failureResult) {
            InvitationError.EmptyWallet -> navigateToEmptyWallet()
            InvitationError.InvalidCredentialInvitation -> navigateToInvalidCredential()
            InvitationError.NetworkError -> navigateToNoInternetConnection(invitationUri)
            InvitationError.NoCompatibleCredential -> navigateToNoMatchingCredential()
            InvitationError.Unexpected,
            InvitationError.InvalidInput,
            InvitationError.InvalidPresentationRequest,
            is InvitationError.InvalidPresentation -> navigateToFailureScreen()
        }
    }

    private fun navigateToNoInternetConnection(
        invitationUri: String,
    ) = navManager.navigateToAndClearCurrent(
        NoInternetConnectionScreenDestination(invitationUri)
    )

    private fun navigateToInvalidCredential() = navManager.navigateToAndClearCurrent(
        InvalidCredentialErrorScreenDestination
    )

    private fun navigateToEmptyWallet() = navManager.navigateToAndClearCurrent(
        PresentationEmptyWalletScreenDestination
    )

    private fun navigateToNoMatchingCredential() = navManager.navigateToAndClearCurrent(
        PresentationNoMatchScreenDestination
    )

    private fun navigateToFailureScreen() = navManager.navigateToAndClearCurrent(
        InvitationFailureScreenDestination
    )
}

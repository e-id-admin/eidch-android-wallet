package ch.admin.foitt.wallet.platform.deeplink.domain.usecase.implementation

import androidx.annotation.CheckResult
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestErrorBody
import ch.admin.foitt.openid4vc.domain.usecase.DeclinePresentation
import ch.admin.foitt.wallet.platform.deeplink.domain.repository.DeepLinkIntentRepository
import ch.admin.foitt.wallet.platform.deeplink.domain.usecase.HandleDeeplink
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError
import ch.admin.foitt.wallet.platform.invitation.domain.model.ProcessInvitationError
import ch.admin.foitt.wallet.platform.invitation.domain.model.ProcessInvitationResult
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.ProcessInvitation
import ch.admin.foitt.wallet.platform.navArgs.domain.model.CredentialOfferNavArg
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.NavigationAction
import ch.admin.foitt.wallet.platform.scaffold.extension.navigateUpOrToRoot
import ch.admin.foitt.walletcomposedestinations.destinations.CredentialOfferScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.HomeScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.InvalidCredentialErrorScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.InvitationFailureScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.NoInternetConnectionScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.OnboardingSuccessScreenDestination
import com.github.michaelbull.result.mapBoth
import com.ramcosta.composedestinations.spec.Direction
import timber.log.Timber
import javax.inject.Inject

class HandleDeeplinkImpl @Inject constructor(
    private val navManager: NavigationManager,
    private val deepLinkIntentRepository: DeepLinkIntentRepository,
    private val processInvitation: ProcessInvitation,
    private val declinePresentation: DeclinePresentation,
) : HandleDeeplink {

    @CheckResult
    override suspend operator fun invoke(fromOnboarding: Boolean): NavigationAction {
        val deepLink = deepLinkIntentRepository.get()
        Timber.d("Deeplink read: $deepLink")

        return if (deepLink == null) {
            if (fromOnboarding) {
                navigateTo(
                    direction = HomeScreenDestination,
                    fromOnboarding = true
                )
            } else {
                NavigationAction {
                    navManager.navigateUpOrToRoot()
                }
            }
        } else {
            deepLinkIntentRepository.reset()

            val nextDirection = processInvitation(deepLink)
                .mapBoth(
                    success = { invitation ->
                        handleSuccess(invitation)
                    },
                    failure = { invitationError ->
                        handleFailure(invitationError, deepLink)
                    },
                )

            navigateTo(
                direction = nextDirection,
                fromOnboarding = fromOnboarding,
            )
        }
    }

    private fun handleSuccess(invitation: ProcessInvitationResult): Direction = when (invitation) {
        is ProcessInvitationResult.CredentialOffer -> credentialOfferDirection(invitation)
        is ProcessInvitationResult.PresentationRequest,
        is ProcessInvitationResult.PresentationRequestCredentialList -> {
            // We do no support presentation deeplinks. So it is redirected to an error.
            InvalidCredentialErrorScreenDestination
        }
    }

    private suspend fun handleFailure(invitationError: ProcessInvitationError, deepLink: String): Direction = when (invitationError) {
        InvitationError.InvalidCredentialInvitation,
        InvitationError.InvalidInput -> InvalidCredentialErrorScreenDestination
        InvitationError.NetworkError -> NoInternetConnectionScreenDestination(deepLink)
        InvitationError.EmptyWallet,
        InvitationError.NoCompatibleCredential,
        InvitationError.InvalidPresentationRequest,
        InvitationError.Unexpected -> InvitationFailureScreenDestination
        is InvitationError.InvalidPresentation -> declinePresentationRequest(invitationError.responseUri)
    }

    private fun credentialOfferDirection(
        credentialOffer: ProcessInvitationResult.CredentialOffer,
    ) = CredentialOfferScreenDestination(
        CredentialOfferNavArg(
            credentialOffer.credentialId
        )
    )

    private fun navigateTo(direction: Direction, fromOnboarding: Boolean) = NavigationAction {
        if (fromOnboarding) {
            // registration -> pop whole onboarding
            navManager.navigateToAndPopUpTo(
                direction = direction,
                route = OnboardingSuccessScreenDestination.route,
            )
        } else {
            // login -> pop login
            navManager.navigateToAndClearCurrent(
                direction = direction,
            )
        }
    }

    private suspend fun declinePresentationRequest(url: String): Direction {
        declinePresentation(url = url, reason = PresentationRequestErrorBody.ErrorType.INVALID_REQUEST)
        return InvitationFailureScreenDestination
    }
}

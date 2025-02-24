package ch.admin.foitt.wallet.platform.deeplink.domain.usecase.implementation

import androidx.annotation.CheckResult
import ch.admin.foitt.wallet.platform.deeplink.domain.repository.DeepLinkIntentRepository
import ch.admin.foitt.wallet.platform.deeplink.domain.usecase.HandleDeeplink
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationErrorScreenState
import ch.admin.foitt.wallet.platform.invitation.domain.model.ProcessInvitationError
import ch.admin.foitt.wallet.platform.invitation.domain.model.ProcessInvitationResult
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.ProcessInvitation
import ch.admin.foitt.wallet.platform.navArgs.domain.model.CredentialOfferNavArg
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.NavigationAction
import ch.admin.foitt.wallet.platform.scaffold.extension.navigateUpOrToRoot
import ch.admin.foitt.walletcomposedestinations.destinations.CredentialOfferScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.EIdIntroScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.HomeScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.InvitationFailureScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.OnboardingSuccessScreenDestination
import com.github.michaelbull.result.mapBoth
import com.ramcosta.composedestinations.spec.Direction
import timber.log.Timber
import javax.inject.Inject

class HandleDeeplinkImpl @Inject constructor(
    private val navManager: NavigationManager,
    private val deepLinkIntentRepository: DeepLinkIntentRepository,
    private val environmentSetupRepository: EnvironmentSetupRepository,
    private val processInvitation: ProcessInvitation,
) : HandleDeeplink {

    @CheckResult
    override suspend operator fun invoke(fromOnboarding: Boolean): NavigationAction {
        val deepLink = deepLinkIntentRepository.get()
        Timber.d("Deeplink read: $deepLink")

        return if (deepLink == null) {
            handleStandardNavigation(fromOnboarding)
        } else {
            handleDeepLinkNavigation(
                deepLink = deepLink,
                fromOnboarding = fromOnboarding,
            )
        }
    }

    private fun handleStandardNavigation(
        fromOnboarding: Boolean,
    ) = if (fromOnboarding) {
        if (environmentSetupRepository.eIdRequestEnabled) {
            navigateTo(
                direction = EIdIntroScreenDestination,
                fromOnboarding = true
            )
        } else {
            navigateTo(
                direction = HomeScreenDestination,
                fromOnboarding = true
            )
        }
    } else {
        NavigationAction {
            navManager.navigateUpOrToRoot()
        }
    }

    private suspend fun handleDeepLinkNavigation(
        deepLink: String,
        fromOnboarding: Boolean,
    ): NavigationAction {
        deepLinkIntentRepository.reset()

        val nextDirection = processInvitation(deepLink)
            .mapBoth(
                success = { invitation ->
                    handleSuccess(invitation)
                },
                failure = { invitationError ->
                    handleFailure(invitationError)
                },
            )

        return navigateTo(
            direction = nextDirection,
            fromOnboarding = fromOnboarding,
        )
    }

    private fun handleSuccess(invitation: ProcessInvitationResult): Direction = when (invitation) {
        is ProcessInvitationResult.CredentialOffer -> credentialOfferDirection(invitation)
        is ProcessInvitationResult.PresentationRequest,
        is ProcessInvitationResult.PresentationRequestCredentialList -> {
            // We do no support presentation deeplinks. So it is redirected to an error.
            Timber.w("Presentation request on processing deeplink")
            InvitationFailureScreenDestination(InvitationErrorScreenState.UNEXPECTED)
        }
    }

    private suspend fun handleFailure(invitationError: ProcessInvitationError): Direction =
        InvitationFailureScreenDestination(invitationError.toErrorDisplay())

    private fun ProcessInvitationError.toErrorDisplay(): InvitationErrorScreenState = when (this) {
        InvitationError.NetworkError -> InvitationErrorScreenState.NETWORK_ERROR
        InvitationError.InvalidCredentialOffer,
        InvitationError.InvalidInput,
        InvitationError.CredentialOfferExpired -> InvitationErrorScreenState.INVALID_CREDENTIAL
        InvitationError.UnknownVerifier,
        InvitationError.EmptyWallet,
        InvitationError.NoCompatibleCredential,
        InvitationError.InvalidPresentationRequest,
        is InvitationError.InvalidPresentation,
        InvitationError.Unexpected -> {
            Timber.w("Unexpected state on processing deeplink")
            InvitationErrorScreenState.UNEXPECTED
        }
        InvitationError.UnknownIssuer -> InvitationErrorScreenState.UNKNOWN_ISSUER
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
}

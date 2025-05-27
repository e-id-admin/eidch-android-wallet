package ch.admin.foitt.wallet.platform.navigation.domain.model

import ch.admin.foitt.walletcomposedestinations.destinations.CredentialOfferDeclinedScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.CredentialOfferScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.DeclineCredentialOfferScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.Destination
import ch.admin.foitt.walletcomposedestinations.destinations.EIdGuardianConsentResultScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.EIdGuardianConsentScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.EIdGuardianSelectionScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.EIdGuardianshipScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.MrzChooserScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.PresentationCredentialListScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.PresentationDeclinedScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.PresentationFailureScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.PresentationInvalidCredentialErrorScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.PresentationRequestScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.PresentationSuccessScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.PresentationValidationErrorScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.PresentationVerificationErrorScreenDestination

sealed interface ComponentScope {
    val destinations: Set<Destination>

    object CredentialIssuer : ComponentScope {
        override val destinations = setOf(
            CredentialOfferScreenDestination,
            CredentialOfferDeclinedScreenDestination,
            DeclineCredentialOfferScreenDestination,
        )
    }

    object Verifier : ComponentScope {
        override val destinations = setOf(
            PresentationCredentialListScreenDestination,
            PresentationDeclinedScreenDestination,
            PresentationFailureScreenDestination,
            PresentationInvalidCredentialErrorScreenDestination,
            PresentationRequestScreenDestination,
            PresentationSuccessScreenDestination,
            PresentationValidationErrorScreenDestination,
            PresentationVerificationErrorScreenDestination,
        )
    }

    object EidApplicationProcess : ComponentScope {
        override val destinations = setOf(
            EIdGuardianshipScreenDestination,
            MrzChooserScreenDestination,
        )
    }

    object EidSIdCase : ComponentScope {
        override val destinations = setOf(
            MrzChooserScreenDestination,
            EIdGuardianSelectionScreenDestination,
            EIdGuardianConsentScreenDestination,
            EIdGuardianConsentResultScreenDestination,
        )
    }
}

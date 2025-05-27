package ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation

import ch.admin.foitt.wallet.feature.walletPairing.presentation.EIdWalletPairingScreen
import ch.admin.foitt.wallet.feature.walletPairing.presentation.EIdWalletPairingViewModel
import ch.admin.foitt.wallet.platform.scaffold.extension.screenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.EIdGuardianConsentResultScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.EIdGuardianConsentScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.EIdGuardianSelectionScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.EIdGuardianshipScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.EIdInfoScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.EIdIntroScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.EIdPrivacyPolicyScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.EIdQueueScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.EIdReadyForAvScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.EIdWalletPairingScreenDestination
import com.ramcosta.composedestinations.manualcomposablecalls.ManualComposableCallsBuilder

fun ManualComposableCallsBuilder.eIdApplicationProcessDestinations() {
    screenDestination(EIdIntroScreenDestination) { viewModel: EIdIntroViewModel ->
        EIdIntroScreen(viewModel)
    }

    screenDestination(EIdPrivacyPolicyScreenDestination) { viewModel: EIdPrivacyPolicyViewModel ->
        EIdPrivacyPolicyScreen(viewModel)
    }

    screenDestination(EIdGuardianshipScreenDestination) { viewModel: EIdGuardianshipViewModel ->
        EIdGuardianshipScreen(viewModel)
    }

    screenDestination(EIdWalletPairingScreenDestination) { viewModel: EIdWalletPairingViewModel ->
        EIdWalletPairingScreen(viewModel)
    }

    screenDestination(EIdInfoScreenDestination) { viewModel: EIdInfoViewModel ->
        EIdInfoScreen(viewModel)
    }

    screenDestination(EIdGuardianConsentResultScreenDestination) { viewModel: EIdGuardianConsentResultViewModel ->
        EIdGuardianConsentResultScreen(viewModel)
    }

    screenDestination(EIdGuardianSelectionScreenDestination) { viewModel: EIdGuardianSelectionViewModel ->
        EIdGuardianSelectionScreen(viewModel)
    }

    screenDestination(EIdGuardianConsentScreenDestination) { viewModel: EIdGuardianConsentViewModel ->
        EIdGuardianConsentScreen(viewModel)
    }

    screenDestination(EIdReadyForAvScreenDestination) { viewModel: EIdReadyForAvViewModel ->
        EIdReadyForAvScreen(viewModel)
    }

    screenDestination(EIdQueueScreenDestination) { viewModel: EIdQueueViewModel ->
        EIdQueueScreen(viewModel)
    }
}

package ch.admin.foitt.wallet.platform.navigation.utils

import ch.admin.foitt.walletcomposedestinations.destinations.AppVersionBlockedScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.BiometricLoginScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.LockScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.LockoutScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.NoDevicePinSetScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.OnboardingConfirmPassphraseScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.OnboardingErrorScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.OnboardingIntroScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.OnboardingLocalDataScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.OnboardingPassphraseConfirmationFailedScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.OnboardingPassphraseExplanationScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.OnboardingPassphraseScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.OnboardingPresentScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.PassphraseLoginScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.RegisterBiometricsScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.StartScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.UserPrivacyPolicyScreenDestination

// Destinations that should not trigger a lock screen
// it implicitly means that the database should either not exist, or be closed and encrypted, on these destinations
val blackListedDestinationsLockScreen = listOf(
    StartScreenDestination,
    NoDevicePinSetScreenDestination,
    AppVersionBlockedScreenDestination,
    LockScreenDestination,
    OnboardingIntroScreenDestination,
    OnboardingPresentScreenDestination,
    OnboardingLocalDataScreenDestination,
    UserPrivacyPolicyScreenDestination,
    OnboardingPassphraseExplanationScreenDestination,
    OnboardingPassphraseScreenDestination,
    OnboardingConfirmPassphraseScreenDestination,
    OnboardingPassphraseConfirmationFailedScreenDestination,
    RegisterBiometricsScreenDestination,
    PassphraseLoginScreenDestination,
    BiometricLoginScreenDestination,
    OnboardingErrorScreenDestination,
    LockoutScreenDestination,
)

// destinations that should not trigger the session timeout navigation
val blackListedDestinationsSessionTimeout = blackListedDestinationsLockScreen

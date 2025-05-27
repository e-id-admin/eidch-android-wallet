package ch.admin.foitt.wallet.app.presentation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import ch.admin.foitt.wallet.feature.changeLogin.presentation.ConfirmNewPassphraseScreen
import ch.admin.foitt.wallet.feature.changeLogin.presentation.ConfirmNewPassphraseViewModel
import ch.admin.foitt.wallet.feature.changeLogin.presentation.EnterCurrentPassphraseScreen
import ch.admin.foitt.wallet.feature.changeLogin.presentation.EnterCurrentPassphraseViewModel
import ch.admin.foitt.wallet.feature.changeLogin.presentation.EnterNewPassphraseScreen
import ch.admin.foitt.wallet.feature.changeLogin.presentation.EnterNewPassphraseViewModel
import ch.admin.foitt.wallet.feature.credentialDetail.presentation.CredentialDetailScreen
import ch.admin.foitt.wallet.feature.credentialDetail.presentation.CredentialDetailViewModel
import ch.admin.foitt.wallet.feature.credentialDetail.presentation.CredentialDetailWrongDataScreen
import ch.admin.foitt.wallet.feature.credentialDetail.presentation.CredentialDetailWrongDataViewModel
import ch.admin.foitt.wallet.feature.credentialOffer.presentation.CredentialOfferDeclinedScreen
import ch.admin.foitt.wallet.feature.credentialOffer.presentation.CredentialOfferDeclinedViewModel
import ch.admin.foitt.wallet.feature.credentialOffer.presentation.CredentialOfferScreen
import ch.admin.foitt.wallet.feature.credentialOffer.presentation.CredentialOfferViewModel
import ch.admin.foitt.wallet.feature.credentialOffer.presentation.CredentialOfferWrongDataScreen
import ch.admin.foitt.wallet.feature.credentialOffer.presentation.CredentialOfferWrongDataViewModel
import ch.admin.foitt.wallet.feature.credentialOffer.presentation.DeclineCredentialOfferScreen
import ch.admin.foitt.wallet.feature.credentialOffer.presentation.DeclineCredentialOfferViewModel
import ch.admin.foitt.wallet.feature.eIdApplicationProcess.presentation.eIdApplicationProcessDestinations
import ch.admin.foitt.wallet.feature.home.presentation.BetaIdScreen
import ch.admin.foitt.wallet.feature.home.presentation.BetaIdViewModel
import ch.admin.foitt.wallet.feature.home.presentation.HomeScreen
import ch.admin.foitt.wallet.feature.home.presentation.HomeViewModel
import ch.admin.foitt.wallet.feature.login.presentation.BiometricLoginScreen
import ch.admin.foitt.wallet.feature.login.presentation.BiometricLoginViewModel
import ch.admin.foitt.wallet.feature.login.presentation.LockScreen
import ch.admin.foitt.wallet.feature.login.presentation.LockViewModel
import ch.admin.foitt.wallet.feature.login.presentation.LockoutScreen
import ch.admin.foitt.wallet.feature.login.presentation.LockoutViewModel
import ch.admin.foitt.wallet.feature.login.presentation.PassphraseLoginScreen
import ch.admin.foitt.wallet.feature.login.presentation.PassphraseLoginViewModel
import ch.admin.foitt.wallet.feature.login.presentation.UnsecuredDeviceScreen
import ch.admin.foitt.wallet.feature.login.presentation.UnsecuredDeviceViewModel
import ch.admin.foitt.wallet.feature.mrzScan.presentation.MrzChooserScreen
import ch.admin.foitt.wallet.feature.mrzScan.presentation.MrzChooserViewModel
import ch.admin.foitt.wallet.feature.mrzScan.presentation.MrzScanPermissionScreen
import ch.admin.foitt.wallet.feature.mrzScan.presentation.MrzScanPermissionViewModel
import ch.admin.foitt.wallet.feature.onboarding.presentation.OnboardingConfirmPassphraseScreen
import ch.admin.foitt.wallet.feature.onboarding.presentation.OnboardingConfirmPassphraseViewModel
import ch.admin.foitt.wallet.feature.onboarding.presentation.OnboardingErrorScreen
import ch.admin.foitt.wallet.feature.onboarding.presentation.OnboardingErrorViewModel
import ch.admin.foitt.wallet.feature.onboarding.presentation.OnboardingIntroScreen
import ch.admin.foitt.wallet.feature.onboarding.presentation.OnboardingIntroViewModel
import ch.admin.foitt.wallet.feature.onboarding.presentation.OnboardingLocalDataScreen
import ch.admin.foitt.wallet.feature.onboarding.presentation.OnboardingLocalDataViewModel
import ch.admin.foitt.wallet.feature.onboarding.presentation.OnboardingPassphraseConfirmationFailedScreen
import ch.admin.foitt.wallet.feature.onboarding.presentation.OnboardingPassphraseConfirmationFailedViewModel
import ch.admin.foitt.wallet.feature.onboarding.presentation.OnboardingPassphraseExplanationScreen
import ch.admin.foitt.wallet.feature.onboarding.presentation.OnboardingPassphraseExplanationViewModel
import ch.admin.foitt.wallet.feature.onboarding.presentation.OnboardingPassphraseScreen
import ch.admin.foitt.wallet.feature.onboarding.presentation.OnboardingPassphraseViewModel
import ch.admin.foitt.wallet.feature.onboarding.presentation.OnboardingPresentScreen
import ch.admin.foitt.wallet.feature.onboarding.presentation.OnboardingPresentViewModel
import ch.admin.foitt.wallet.feature.onboarding.presentation.OnboardingSuccessScreen
import ch.admin.foitt.wallet.feature.onboarding.presentation.OnboardingSuccessViewModel
import ch.admin.foitt.wallet.feature.onboarding.presentation.RegisterBiometricsScreen
import ch.admin.foitt.wallet.feature.onboarding.presentation.RegisterBiometricsViewModel
import ch.admin.foitt.wallet.feature.onboarding.presentation.UserPrivacyPolicyScreen
import ch.admin.foitt.wallet.feature.onboarding.presentation.UserPrivacyPolicyViewModel
import ch.admin.foitt.wallet.feature.presentationRequest.presentation.PresentationCredentialListScreen
import ch.admin.foitt.wallet.feature.presentationRequest.presentation.PresentationCredentialListViewModel
import ch.admin.foitt.wallet.feature.presentationRequest.presentation.PresentationDeclinedScreen
import ch.admin.foitt.wallet.feature.presentationRequest.presentation.PresentationDeclinedViewModel
import ch.admin.foitt.wallet.feature.presentationRequest.presentation.PresentationFailureScreen
import ch.admin.foitt.wallet.feature.presentationRequest.presentation.PresentationFailureViewModel
import ch.admin.foitt.wallet.feature.presentationRequest.presentation.PresentationInvalidCredentialErrorScreen
import ch.admin.foitt.wallet.feature.presentationRequest.presentation.PresentationInvalidCredentialErrorViewModel
import ch.admin.foitt.wallet.feature.presentationRequest.presentation.PresentationRequestScreen
import ch.admin.foitt.wallet.feature.presentationRequest.presentation.PresentationRequestViewModel
import ch.admin.foitt.wallet.feature.presentationRequest.presentation.PresentationSuccessScreen
import ch.admin.foitt.wallet.feature.presentationRequest.presentation.PresentationSuccessViewModel
import ch.admin.foitt.wallet.feature.presentationRequest.presentation.PresentationValidationErrorScreen
import ch.admin.foitt.wallet.feature.presentationRequest.presentation.PresentationValidationErrorViewModel
import ch.admin.foitt.wallet.feature.presentationRequest.presentation.PresentationVerificationErrorScreen
import ch.admin.foitt.wallet.feature.presentationRequest.presentation.PresentationVerificationErrorViewModel
import ch.admin.foitt.wallet.feature.qrscan.presentation.QrScanPermissionScreen
import ch.admin.foitt.wallet.feature.qrscan.presentation.QrScanPermissionViewModel
import ch.admin.foitt.wallet.feature.qrscan.presentation.QrScannerScreen
import ch.admin.foitt.wallet.feature.qrscan.presentation.QrScannerViewModel
import ch.admin.foitt.wallet.feature.settings.presentation.SettingsScreen
import ch.admin.foitt.wallet.feature.settings.presentation.SettingsViewModel
import ch.admin.foitt.wallet.feature.settings.presentation.biometrics.AuthWithPassphraseScreen
import ch.admin.foitt.wallet.feature.settings.presentation.biometrics.AuthWithPassphraseViewModel
import ch.admin.foitt.wallet.feature.settings.presentation.biometrics.EnableBiometricsErrorScreen
import ch.admin.foitt.wallet.feature.settings.presentation.biometrics.EnableBiometricsErrorViewModel
import ch.admin.foitt.wallet.feature.settings.presentation.biometrics.EnableBiometricsLockoutScreen
import ch.admin.foitt.wallet.feature.settings.presentation.biometrics.EnableBiometricsLockoutViewModel
import ch.admin.foitt.wallet.feature.settings.presentation.biometrics.EnableBiometricsScreen
import ch.admin.foitt.wallet.feature.settings.presentation.biometrics.EnableBiometricsViewModel
import ch.admin.foitt.wallet.feature.settings.presentation.impressum.ImpressumScreen
import ch.admin.foitt.wallet.feature.settings.presentation.impressum.ImpressumViewModel
import ch.admin.foitt.wallet.feature.settings.presentation.language.LanguageScreen
import ch.admin.foitt.wallet.feature.settings.presentation.language.LanguageViewModel
import ch.admin.foitt.wallet.feature.settings.presentation.licences.LicencesScreen
import ch.admin.foitt.wallet.feature.settings.presentation.licences.LicencesViewModel
import ch.admin.foitt.wallet.feature.settings.presentation.security.DataAnalysisScreen
import ch.admin.foitt.wallet.feature.settings.presentation.security.DataAnalysisViewModel
import ch.admin.foitt.wallet.feature.settings.presentation.security.SecuritySettingsScreen
import ch.admin.foitt.wallet.feature.settings.presentation.security.SecuritySettingsViewModel
import ch.admin.foitt.wallet.platform.invitation.presentation.InvitationFailureScreen
import ch.admin.foitt.wallet.platform.invitation.presentation.InvitationFailureViewModel
import ch.admin.foitt.wallet.platform.scaffold.extension.screenDestination
import ch.admin.foitt.wallet.platform.screens.presentation.ErrorScreen
import ch.admin.foitt.wallet.platform.screens.presentation.ErrorViewModel
import ch.admin.foitt.wallet.platform.versionEnforcement.presentation.AppVersionBlockedScreen
import ch.admin.foitt.wallet.platform.versionEnforcement.presentation.AppVersionBlockedViewModel
import ch.admin.foitt.walletcomposedestinations.NavGraphs
import ch.admin.foitt.walletcomposedestinations.destinations.AppVersionBlockedScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.AuthWithPassphraseScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.BetaIdScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.BiometricLoginScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.ConfirmNewPassphraseScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.CredentialDetailScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.CredentialDetailWrongDataScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.CredentialOfferDeclinedScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.CredentialOfferScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.CredentialOfferWrongDataScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.DataAnalysisScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.DeclineCredentialOfferScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.EnableBiometricsErrorScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.EnableBiometricsLockoutScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.EnableBiometricsScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.EnterCurrentPassphraseScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.EnterNewPassphraseScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.ErrorScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.HomeScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.ImpressumScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.InvitationFailureScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.LanguageScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.LicencesScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.LockScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.LockoutScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.MrzChooserScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.MrzScanPermissionScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.OnboardingConfirmPassphraseScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.OnboardingErrorScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.OnboardingIntroScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.OnboardingLocalDataScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.OnboardingPassphraseConfirmationFailedScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.OnboardingPassphraseExplanationScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.OnboardingPassphraseScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.OnboardingPresentScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.OnboardingSuccessScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.PassphraseLoginScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.PresentationCredentialListScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.PresentationDeclinedScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.PresentationFailureScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.PresentationInvalidCredentialErrorScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.PresentationRequestScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.PresentationSuccessScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.PresentationValidationErrorScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.PresentationVerificationErrorScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.QrScanPermissionScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.QrScannerScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.RegisterBiometricsScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.SecuritySettingsScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.SettingsScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.StartScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.UnsecuredDeviceScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.UserPrivacyPolicyScreenDestination
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.spec.NavHostEngine

@Composable
fun NavigationHost(
    engine: NavHostEngine,
    navController: NavHostController,
) {
    DestinationsNavHost(
        navGraph = NavGraphs.root,
        engine = engine,
        navController = navController,
    ) {
        screenDestination(StartScreenDestination) { viewModel: StartViewModel ->
            StartScreen(viewModel)
        }

        screenDestination(LockScreenDestination) { viewModel: LockViewModel ->
            LockScreen(viewModel)
        }

        screenDestination(UnsecuredDeviceScreenDestination) { viewModel: UnsecuredDeviceViewModel ->
            UnsecuredDeviceScreen(viewModel)
        }

        screenDestination(AppVersionBlockedScreenDestination) { viewModel: AppVersionBlockedViewModel ->
            AppVersionBlockedScreen(viewModel)
        }

        screenDestination(OnboardingIntroScreenDestination) { viewModel: OnboardingIntroViewModel ->
            OnboardingIntroScreen(viewModel)
        }

        screenDestination(OnboardingPresentScreenDestination) { viewModel: OnboardingPresentViewModel ->
            OnboardingPresentScreen(viewModel)
        }

        screenDestination(OnboardingLocalDataScreenDestination) { viewModel: OnboardingLocalDataViewModel ->
            OnboardingLocalDataScreen(viewModel)
        }

        screenDestination(UserPrivacyPolicyScreenDestination) { viewModel: UserPrivacyPolicyViewModel ->
            UserPrivacyPolicyScreen(viewModel)
        }

        screenDestination(OnboardingPassphraseExplanationScreenDestination) { viewModel: OnboardingPassphraseExplanationViewModel ->
            OnboardingPassphraseExplanationScreen(viewModel)
        }

        screenDestination(OnboardingPassphraseScreenDestination) { viewModel: OnboardingPassphraseViewModel ->
            OnboardingPassphraseScreen(viewModel)
        }

        screenDestination(OnboardingConfirmPassphraseScreenDestination) { viewModel: OnboardingConfirmPassphraseViewModel ->
            OnboardingConfirmPassphraseScreen(viewModel)
        }

        screenDestination(
            OnboardingPassphraseConfirmationFailedScreenDestination
        ) { viewModel: OnboardingPassphraseConfirmationFailedViewModel ->
            OnboardingPassphraseConfirmationFailedScreen(viewModel)
        }

        screenDestination(RegisterBiometricsScreenDestination) { viewModel: RegisterBiometricsViewModel ->
            RegisterBiometricsScreen(viewModel)
        }

        screenDestination(OnboardingSuccessScreenDestination) { viewModel: OnboardingSuccessViewModel ->
            OnboardingSuccessScreen(viewModel)
        }

        screenDestination(OnboardingErrorScreenDestination) { viewModel: OnboardingErrorViewModel ->
            OnboardingErrorScreen(viewModel)
        }

        screenDestination(EnableBiometricsLockoutScreenDestination) { viewModel: EnableBiometricsLockoutViewModel ->
            EnableBiometricsLockoutScreen(viewModel)
        }

        screenDestination(EnableBiometricsScreenDestination) { viewModel: EnableBiometricsViewModel ->
            EnableBiometricsScreen(viewModel)
        }

        screenDestination(EnableBiometricsErrorScreenDestination) { viewModel: EnableBiometricsErrorViewModel ->
            EnableBiometricsErrorScreen(viewModel)
        }

        screenDestination(PassphraseLoginScreenDestination) { viewModel: PassphraseLoginViewModel ->
            PassphraseLoginScreen(viewModel)
        }

        screenDestination(BiometricLoginScreenDestination) { viewModel: BiometricLoginViewModel ->
            BiometricLoginScreen(viewModel)
        }

        screenDestination(SettingsScreenDestination) { viewModel: SettingsViewModel ->
            SettingsScreen(viewModel)
        }

        screenDestination(SecuritySettingsScreenDestination) { viewModel: SecuritySettingsViewModel ->
            SecuritySettingsScreen(viewModel)
        }

        screenDestination(DataAnalysisScreenDestination) { _: DataAnalysisViewModel ->
            DataAnalysisScreen()
        }

        screenDestination(LanguageScreenDestination) { viewModel: LanguageViewModel ->
            LanguageScreen(viewModel)
        }

        screenDestination(ImpressumScreenDestination) { viewModel: ImpressumViewModel ->
            ImpressumScreen(viewModel)
        }

        screenDestination(LicencesScreenDestination) { viewModel: LicencesViewModel ->
            LicencesScreen(viewModel)
        }

        screenDestination(QrScanPermissionScreenDestination) { viewModel: QrScanPermissionViewModel ->
            QrScanPermissionScreen(viewModel)
        }

        screenDestination(QrScannerScreenDestination) { viewModel: QrScannerViewModel ->
            QrScannerScreen(viewModel)
        }

        screenDestination(InvitationFailureScreenDestination) { viewModel: InvitationFailureViewModel ->
            InvitationFailureScreen(viewModel)
        }

        screenDestination(CredentialOfferScreenDestination) { viewModel: CredentialOfferViewModel ->
            CredentialOfferScreen(viewModel)
        }

        screenDestination(DeclineCredentialOfferScreenDestination) { viewModel: DeclineCredentialOfferViewModel ->
            DeclineCredentialOfferScreen(viewModel)
        }

        screenDestination(CredentialOfferDeclinedScreenDestination) { viewModel: CredentialOfferDeclinedViewModel ->
            CredentialOfferDeclinedScreen(viewModel)
        }

        screenDestination(CredentialOfferWrongDataScreenDestination) { viewModel: CredentialOfferWrongDataViewModel ->
            CredentialOfferWrongDataScreen()
        }

        screenDestination(PresentationRequestScreenDestination) { viewModel: PresentationRequestViewModel ->
            PresentationRequestScreen(viewModel)
        }

        screenDestination(PresentationSuccessScreenDestination) { viewModel: PresentationSuccessViewModel ->
            PresentationSuccessScreen(viewModel)
        }

        screenDestination(PresentationFailureScreenDestination) { viewModel: PresentationFailureViewModel ->
            PresentationFailureScreen(viewModel)
        }

        screenDestination(PresentationValidationErrorScreenDestination) { viewModel: PresentationValidationErrorViewModel ->
            PresentationValidationErrorScreen(viewModel)
        }

        screenDestination(PresentationInvalidCredentialErrorScreenDestination) { viewModel: PresentationInvalidCredentialErrorViewModel ->
            PresentationInvalidCredentialErrorScreen(viewModel)
        }

        screenDestination(PresentationVerificationErrorScreenDestination) { viewModel: PresentationVerificationErrorViewModel ->
            PresentationVerificationErrorScreen(viewModel)
        }

        screenDestination(PresentationCredentialListScreenDestination) { viewModel: PresentationCredentialListViewModel ->
            PresentationCredentialListScreen(viewModel)
        }

        screenDestination(PresentationDeclinedScreenDestination) { viewModel: PresentationDeclinedViewModel ->
            PresentationDeclinedScreen(viewModel)
        }

        screenDestination(HomeScreenDestination) { viewModel: HomeViewModel ->
            HomeScreen(viewModel)
        }

        screenDestination(CredentialDetailScreenDestination) { viewModel: CredentialDetailViewModel ->
            CredentialDetailScreen(viewModel)
        }

        screenDestination(CredentialDetailWrongDataScreenDestination) { viewmodel: CredentialDetailWrongDataViewModel ->
            CredentialDetailWrongDataScreen()
        }

        screenDestination(AuthWithPassphraseScreenDestination) { viewModel: AuthWithPassphraseViewModel ->
            AuthWithPassphraseScreen(viewModel)
        }

        screenDestination(EnterCurrentPassphraseScreenDestination) { viewModel: EnterCurrentPassphraseViewModel ->
            EnterCurrentPassphraseScreen(viewModel)
        }

        screenDestination(ConfirmNewPassphraseScreenDestination) { viewModel: ConfirmNewPassphraseViewModel ->
            ConfirmNewPassphraseScreen(viewModel)
        }

        screenDestination(EnterNewPassphraseScreenDestination) { viewModel: EnterNewPassphraseViewModel ->
            EnterNewPassphraseScreen(viewModel)
        }

        screenDestination(ErrorScreenDestination) { viewModel: ErrorViewModel ->
            ErrorScreen(viewModel)
        }

        screenDestination(LockoutScreenDestination) { viewModel: LockoutViewModel ->
            LockoutScreen(viewModel)
        }

        screenDestination(BetaIdScreenDestination) { viewModel: BetaIdViewModel ->
            BetaIdScreen(viewModel)
        }

        eIdApplicationProcessDestinations()

        screenDestination(MrzScanPermissionScreenDestination) { viewModel: MrzScanPermissionViewModel ->
            MrzScanPermissionScreen(viewModel)
        }
        screenDestination(MrzChooserScreenDestination) { viewModel: MrzChooserViewModel ->
            MrzChooserScreen(viewModel)
        }
    }
}

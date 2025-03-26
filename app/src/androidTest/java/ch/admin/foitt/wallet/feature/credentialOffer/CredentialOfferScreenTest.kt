package ch.admin.foitt.wallet.feature.credentialOffer

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import ch.admin.foitt.wallet.app.MainActivity
import ch.admin.foitt.wallet.feature.credentialOffer.domain.usecase.CredentialOfferScreen
import ch.admin.foitt.wallet.feature.home.screens.HomeScreen
import ch.admin.foitt.wallet.feature.onboarding.screens.OnboardingBiometricScreen
import ch.admin.foitt.wallet.feature.onboarding.screens.OnboardingIntroScreen
import ch.admin.foitt.wallet.feature.onboarding.screens.OnboardingLocalDataScreen
import ch.admin.foitt.wallet.feature.onboarding.screens.OnboardingPasswordExplanationScreen
import ch.admin.foitt.wallet.feature.onboarding.screens.OnboardingPresentScreen
import ch.admin.foitt.wallet.feature.onboarding.screens.OnboardingSuccessScreen
import ch.admin.foitt.wallet.feature.onboarding.screens.OnboardingUserPrivacyScreen
import ch.admin.foitt.wallet.feature.onboarding.screens.PasswordConfirmationScreen
import ch.admin.foitt.wallet.feature.onboarding.screens.PasswordEntryScreen
import ch.admin.foitt.wallet.feature.qrscan.screens.QrScanPermissionScreen
import ch.admin.foitt.wallet.feature.qrscan.screens.QrScannerScreen
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.screens.EIdRequestIntroScreen
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class CredentialOfferScreenTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    var activityRule = createAndroidComposeRule<MainActivity>()

    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun test_credential_offer_not_onboarded_until_offer() = runTest {
        val introScreen = OnboardingIntroScreen(activityRule)
        introScreen.isDisplayed()
        introScreen.nextScreen()
        val localDataScreen = OnboardingLocalDataScreen(activityRule)
        localDataScreen.isDisplayed()
        localDataScreen.nextScreen()
        val presentScreen = OnboardingPresentScreen(activityRule)
        presentScreen.isDisplayed()
        presentScreen.nextScreen()
        val userPrivacyScreen = OnboardingUserPrivacyScreen(activityRule)
        userPrivacyScreen.isDisplayed()
        userPrivacyScreen.accept()
        val pinExplanationScreen = OnboardingPasswordExplanationScreen(activityRule)
        pinExplanationScreen.isDisplayed()
        pinExplanationScreen.nextScreen()
        val passphrase = "123456"
        val passwordEntryScreen = PasswordEntryScreen(activityRule)
        passwordEntryScreen.enterPin(passphrase)
        passwordEntryScreen.nextScreen()
        val passwordConfirmationScreen = PasswordConfirmationScreen(activityRule)
        passwordConfirmationScreen.enterPin(passphrase)
        passwordEntryScreen.nextScreen()
        val biometricScreen = OnboardingBiometricScreen(activityRule)
        biometricScreen.isDisplayed()
        biometricScreen.noBiometric()
        val successScreen = OnboardingSuccessScreen(activityRule)
        successScreen.isDisplayed()
        successScreen.nextScreen()

        val eidIntroScreen = EIdRequestIntroScreen(activityRule)
        eidIntroScreen.isDisplayed()
        eidIntroScreen.nextScreen()

        val homeScreen = HomeScreen(activityRule)
        homeScreen.isDisplayed()
        homeScreen.nextScreen()

        val cameraPermissionScreen = QrScanPermissionScreen(activityRule)
        cameraPermissionScreen.isDisplayed()
        cameraPermissionScreen.nextScreen()

        // accept camera permission
        device.wait(Until.hasObject(By.text("While using the app")), 5000)
        device.findObject(By.text("While using the app")).click()

        val scannerScreen = QrScannerScreen(activityRule)
        scannerScreen.isDisplayed()

        val credentialOfferScreen = CredentialOfferScreen(activityRule)
        credentialOfferScreen.isDisplayed()
        credentialOfferScreen.acceptCredential()

        homeScreen.credentialListIsDisplayed()
    }
}

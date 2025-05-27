package ch.admin.foitt.wallet.feature.presentationRequest

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import ch.admin.foitt.wallet.app.MainActivity
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
import ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase.PresentationRequestScreen
import ch.admin.foitt.wallet.feature.qrscan.infra.implementation.FakeQrScannerImpl
import ch.admin.foitt.wallet.feature.qrscan.screens.QrScanPermissionScreen
import ch.admin.foitt.wallet.feature.qrscan.screens.QrScannerScreen
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.screens.EIdRequestIntroScreen
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class PresentationRequestScreenTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    var activityRule = createAndroidComposeRule<MainActivity>()

    private lateinit var fakeQrScanner: FakeQrScannerImpl

    private val device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Inject
    fun setFakeQrScanner(qrScanner: FakeQrScannerImpl) {
        fakeQrScanner = qrScanner
    }

    @Test
    fun test_credential_request_not_onboarded_until_request() = runTest(testDispatcher) {
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
        homeScreen.isDisplayedWithCredentials()
        homeScreen.nextScreen()

        val cameraPermissionScreen = QrScanPermissionScreen(activityRule)
        cameraPermissionScreen.isDisplayed()
        cameraPermissionScreen.nextScreen()

        // accept camera permission
        device.wait(Until.hasObject(By.text("While using the app")), 5000)
        device.findObject(By.text("While using the app")).click()

        fakeQrScanner.setFakeQrCode("https://example.org/api/v1/request-object/83182112-7b6f-4911-8ae9-8f8d8a750981")

        val scannerScreen = QrScannerScreen(activityRule)
        // sometimes the screen is too fast
        delay(100)
        scannerScreen.isDisplayed()

        val requestScreen = PresentationRequestScreen(activityRule)
        requestScreen.isDisplayed()
    }
}

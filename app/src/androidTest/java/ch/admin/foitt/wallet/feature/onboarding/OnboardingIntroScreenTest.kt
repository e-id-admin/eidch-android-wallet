package ch.admin.foitt.wallet.feature.onboarding

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.admin.foitt.wallet.app.MainActivity
import ch.admin.foitt.wallet.feature.home.screens.HomeScreen
import ch.admin.foitt.wallet.feature.onboarding.screens.OnboardingBiometricScreen
import ch.admin.foitt.wallet.feature.onboarding.screens.OnboardingIntroScreen
import ch.admin.foitt.wallet.feature.onboarding.screens.OnboardingLocalDataScreen
import ch.admin.foitt.wallet.feature.onboarding.screens.OnboardingPinExplanationScreen
import ch.admin.foitt.wallet.feature.onboarding.screens.OnboardingPresentScreen
import ch.admin.foitt.wallet.feature.onboarding.screens.OnboardingSuccessScreen
import ch.admin.foitt.wallet.feature.onboarding.screens.OnboardingUserPrivacyScreen
import ch.admin.foitt.wallet.feature.onboarding.screens.PinConfirmationScreen
import ch.admin.foitt.wallet.feature.onboarding.screens.PinEntryScreen
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class OnboardingIntroScreenTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    var activityRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun test_App_starts_with_IntroScreen() = runTest {
        val introScreen = OnboardingIntroScreen(activityRule)
        introScreen.isDisplayed()
    }

    @Test
    fun test_navigation() = runTest {
        val introScreen = OnboardingIntroScreen(activityRule)
        introScreen.isDisplayed()
        introScreen.nextScreen()
        val presentScreen = OnboardingPresentScreen(activityRule)
        presentScreen.isDisplayed()
        presentScreen.nextScreen()
        val localDataScreen = OnboardingLocalDataScreen(activityRule)
        localDataScreen.isDisplayed()
        localDataScreen.nextScreen()
        val userPrivacyScreen = OnboardingUserPrivacyScreen(activityRule)
        userPrivacyScreen.isDisplayed()
        userPrivacyScreen.accept()
        val pinExplanationScreen = OnboardingPinExplanationScreen(activityRule)
        pinExplanationScreen.isDisplayed()
        pinExplanationScreen.nextScreen()
    }

    @Test
    fun test_back_navigation() = runTest {
        val introScreen = OnboardingIntroScreen(activityRule)
        introScreen.isDisplayed()
        introScreen.nextScreen()
        val presentScreen = OnboardingPresentScreen(activityRule)
        presentScreen.isDisplayed()
        presentScreen.nextScreen()
        val localDataScreen = OnboardingLocalDataScreen(activityRule)
        localDataScreen.isDisplayed()
        localDataScreen.nextScreen()
        val userPrivacyScreen = OnboardingUserPrivacyScreen(activityRule)
        userPrivacyScreen.isDisplayed()
        userPrivacyScreen.accept()
        val pinExplanationScreen = OnboardingPinExplanationScreen(activityRule)
        pinExplanationScreen.isDisplayed()
        pinExplanationScreen.clickBack()
        userPrivacyScreen.isDisplayed()
        userPrivacyScreen.clickBack()
        localDataScreen.isDisplayed()
        localDataScreen.clickBack()
        presentScreen.isDisplayed()
        presentScreen.clickBack()
        introScreen.isDisplayed()

    }

    @Test
    fun test_back_swipe_navigation() = runTest {
        val introScreen = OnboardingIntroScreen(activityRule)
        introScreen.isDisplayed()
        introScreen.nextScreen()
        val presentScreen = OnboardingPresentScreen(activityRule)
        presentScreen.isDisplayed()
        presentScreen.nextScreen()
        val localDataScreen = OnboardingLocalDataScreen(activityRule)
        localDataScreen.isDisplayed()
        localDataScreen.swipeBack()
        presentScreen.isDisplayed()
        presentScreen.swipeBack()
        introScreen.isDisplayed()
        introScreen.swipeBack()
        introScreen.isDisplayed()
    }

    @Test
    fun test_decline_user_privacy() = runTest {
        val userPrivacyScreen = OnboardingUserPrivacyScreen(activityRule)
        userPrivacyScreen.navigateToScreen()
        userPrivacyScreen.decline()
        val pinExplanationScreen = OnboardingPinExplanationScreen(activityRule)
        pinExplanationScreen.isDisplayed()
    }

    @Test
    fun test_decline_then_accept_user_privacy() = runTest {
        val userPrivacyScreen = OnboardingUserPrivacyScreen(activityRule)
        userPrivacyScreen.navigateToScreen()
        userPrivacyScreen.decline()
        val pinExplanationScreen = OnboardingPinExplanationScreen(activityRule)
        pinExplanationScreen.isDisplayed()
        pinExplanationScreen.clickBack()
        userPrivacyScreen.isDisplayed()
        userPrivacyScreen.accept()
        pinExplanationScreen.isDisplayed()
    }

    @Test
    fun test_full_onboarding_flow() = runTest {
        val passphrase = "123456"
        val pinEntryScreen = PinEntryScreen(activityRule)
        pinEntryScreen.navigateToScreen()
        pinEntryScreen.enterPin(passphrase)
        pinEntryScreen.nextScreen()
        val pinConfirmationScreen = PinConfirmationScreen(activityRule)
        pinConfirmationScreen.enterPin(passphrase)
        pinEntryScreen.nextScreen()
        val biometricScreen = OnboardingBiometricScreen(activityRule)
        biometricScreen.isDisplayed()
        biometricScreen.noBiometric()
        val successScreen = OnboardingSuccessScreen(activityRule)
        successScreen.isDisplayed()
        successScreen.nextScreen()
        val homeScreen = HomeScreen(activityRule)
        homeScreen.isDisplayed()

    }

}
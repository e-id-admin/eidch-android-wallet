package ch.admin.foitt.wallet.feature.onboarding

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.admin.foitt.wallet.app.MainActivity
import ch.admin.foitt.wallet.feature.onboarding.screens.OnboardingBiometricScreen
import ch.admin.foitt.wallet.feature.onboarding.screens.PasswordConfirmationScreen
import ch.admin.foitt.wallet.feature.onboarding.screens.PasswordEntryScreen
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class OnboardingPasswordTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    var activityRule = createAndroidComposeRule<MainActivity>()

    @Before
    fun init() {
        hiltRule.inject()
    }

    @Test
    fun enter_valid_pin_test() = runTest {
        val passwordEntryScreen = PasswordEntryScreen(activityRule)
        passwordEntryScreen.navigateToScreen()
        passwordEntryScreen.enterPin("test123")
        passwordEntryScreen.nextScreen()
        val passwordConfirmationScreen = PasswordConfirmationScreen(activityRule)
        passwordConfirmationScreen.isDisplayed()
        passwordConfirmationScreen.enterPin("test123")
        passwordConfirmationScreen.nextScreen()
        val biometricScreen = OnboardingBiometricScreen(activityRule)
        biometricScreen.isDisplayed()
    }

    @Test
    fun enter_short_pin() = runTest {
        val passwordEntryScreen = PasswordEntryScreen(activityRule)
        passwordEntryScreen.navigateToScreen()
        passwordEntryScreen.enterPin("test1")
        passwordEntryScreen.nextScreen()
        passwordEntryScreen.errorIsDisplayed()
    }

    @Test
    fun enter_mismatched_pin() = runTest {
        val passwordEntryScreen = PasswordEntryScreen(activityRule)
        passwordEntryScreen.navigateToScreen()
        passwordEntryScreen.enterPin("test12")
        passwordEntryScreen.nextScreen()
        val passwordConfirmationScreen = PasswordConfirmationScreen(activityRule)
        passwordConfirmationScreen.isDisplayed()
        passwordConfirmationScreen.enterPin("test13")
        passwordConfirmationScreen.nextScreen()
        passwordConfirmationScreen.errorIsDisplayed()
        passwordConfirmationScreen.isDisplayed()
    }

    @Test
    fun pin_entry_show_password_test() = runTest {
        val password = "test12"
        val maskedPassword = "••••••"
        val passwordEntryScreen = PasswordEntryScreen(activityRule)
        passwordEntryScreen.navigateToScreen()
        passwordEntryScreen.enterPin(password)
        passwordEntryScreen.passwordField.assertTextEquals(maskedPassword)
        passwordEntryScreen.showPassphrase.performClick()
        passwordEntryScreen.passwordField.assertTextEquals(password)
        passwordEntryScreen.showPassphrase.performClick()
        passwordEntryScreen.passwordField.assertTextEquals(maskedPassword)
    }

    @Test
    fun pin_confirmation_show_password_test() = runTest {
        val password = "test12"
        val maskedPassword = "••••••"
        val passwordEntryScreen = PasswordEntryScreen(activityRule)
        passwordEntryScreen.navigateToScreen()
        passwordEntryScreen.enterPin(password)
        passwordEntryScreen.nextScreen()
        val passwordConfirmationScreen = PasswordConfirmationScreen(activityRule)
        passwordEntryScreen.enterPin(password)
        passwordConfirmationScreen.passwordField.assertTextEquals(maskedPassword)
        passwordConfirmationScreen.showPassphrase.performClick()
        passwordConfirmationScreen.passwordField.assertTextEquals(password)
        passwordConfirmationScreen.showPassphrase.performClick()
        passwordConfirmationScreen.passwordField.assertTextEquals(maskedPassword)
    }

}

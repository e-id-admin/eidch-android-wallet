package ch.admin.foitt.wallet.feature.onboarding

import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.admin.foitt.wallet.app.MainActivity
import ch.admin.foitt.wallet.feature.onboarding.screens.OnboardingBiometricScreen
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
class OnboardingPinTest {

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
        val pinEntryScreen = PinEntryScreen(activityRule)
        pinEntryScreen.navigateToScreen()
        pinEntryScreen.enterPin("test123")
        pinEntryScreen.nextScreen()
        val pinConfirmationScreen = PinConfirmationScreen(activityRule)
        pinConfirmationScreen.isDisplayed()
        pinConfirmationScreen.enterPin("test123")
        pinConfirmationScreen.nextScreen()
        val biometricScreen = OnboardingBiometricScreen(activityRule)
        biometricScreen.isDisplayed()
    }

    @Test
    fun enter_short_pin() = runTest {
        val pinEntryScreen = PinEntryScreen(activityRule)
        pinEntryScreen.navigateToScreen()
        pinEntryScreen.enterPin("test1")
        pinEntryScreen.nextScreen()
        pinEntryScreen.errorIsDisplayed()
    }

    @Test
    fun enter_mismatched_pin() = runTest {
        val pinEntryScreen = PinEntryScreen(activityRule)
        pinEntryScreen.navigateToScreen()
        pinEntryScreen.enterPin("test12")
        pinEntryScreen.nextScreen()
        val pinConfirmationScreen = PinConfirmationScreen(activityRule)
        pinConfirmationScreen.isDisplayed()
        pinConfirmationScreen.enterPin("test13")
        pinConfirmationScreen.nextScreen()
        pinConfirmationScreen.errorIsDisplayed()
        pinConfirmationScreen.isDisplayed()
    }

    @Test
    fun pin_entry_show_password_test() = runTest {
        val password = "test12"
        val maskedPassword = "••••••"
        val pinEntryScreen = PinEntryScreen(activityRule)
        pinEntryScreen.navigateToScreen()
        pinEntryScreen.enterPin(password)
        pinEntryScreen.pinField.assertTextEquals(maskedPassword)
        pinEntryScreen.showPassphrase.performClick()
        pinEntryScreen.pinField.assertTextEquals(password)
        pinEntryScreen.showPassphrase.performClick()
        pinEntryScreen.pinField.assertTextEquals(maskedPassword)
    }

    @Test
    fun pin_confirmation_show_password_test() = runTest {
        val password = "test12"
        val maskedPassword = "••••••"
        val pinEntryScreen = PinEntryScreen(activityRule)
        pinEntryScreen.navigateToScreen()
        pinEntryScreen.enterPin(password)
        pinEntryScreen.nextScreen()
        val pinConfirmationScreen = PinConfirmationScreen(activityRule)
        pinEntryScreen.enterPin(password)
        pinConfirmationScreen.pinField.assertTextEquals(maskedPassword)
        pinConfirmationScreen.showPassphrase.performClick()
        pinConfirmationScreen.pinField.assertTextEquals(password)
        pinConfirmationScreen.showPassphrase.performClick()
        pinConfirmationScreen.pinField.assertTextEquals(maskedPassword)
    }

}

package ch.admin.foitt.wallet.feature.onboarding.screens

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import ch.admin.foitt.wallet.platform.utils.TestTags

class PinEntryScreen(composeTestRule : ComposeContentTestRule) : OnboardingBaseScreen(composeTestRule) {
    val title = composeTestRule.onNodeWithTag(TestTags.TOP_BAR_TITLE.name)
    val pinField = composeTestRule.onNodeWithTag(TestTags.PIN_FIELD.name)
    val showPassphrase = composeTestRule.onNodeWithTag(TestTags.SHOW_PASSPHRASE_ICON.name)
    val error = composeTestRule.onNodeWithTag(TestTags.ERROR.name)

    @OptIn(ExperimentalTestApi::class)
    override fun isDisplayed() {
        composeTestRule.waitUntilAtLeastOneExists(hasText("Enter password (deepl)"), 10000)
        title.assertIsDisplayed()
        title.assertTextEquals("Enter password (deepl)")
        pinField.assertIsDisplayed()
        continueButton.assertIsDisplayed()
        showPassphrase.assertIsDisplayed()
    }

    fun enterPin(pin: String){
        pinField.assertIsDisplayed()
        pinField.performTextClearance()
        pinField.performTextInput(pin)
    }

    fun errorIsDisplayed() {
        error.assertIsDisplayed()
    }

    fun navigateToScreen() {
        val pinExplanationScreen = OnboardingPinExplanationScreen(composeTestRule)
        pinExplanationScreen.navigateToScreen()
        pinExplanationScreen.nextScreen()
        isDisplayed()
    }




}
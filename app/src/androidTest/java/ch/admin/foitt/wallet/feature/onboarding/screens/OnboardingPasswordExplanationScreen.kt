package ch.admin.foitt.wallet.feature.onboarding.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import ch.admin.foitt.wallet.platform.utils.TestTags

class OnboardingPasswordExplanationScreen(composeTestRule: ComposeContentTestRule) : OnboardingBaseScreen(composeTestRule){
    override val image = composeTestRule.onNodeWithTag(TestTags.PIN_EXPLANATION_ICON.name)
    val backButton = composeTestRule.onNodeWithTag(TestTags.BACK_BUTTON.name)


    override fun isDisplayed() {
        image.isDisplayed()
        continueButton.isDisplayed()
    }
    fun clickBack() {
        backButton.apply {
            assertIsDisplayed()
            performClick()
        }
    }

    fun navigateToScreen() {
        val privacyScreen = OnboardingUserPrivacyScreen(composeTestRule)
        privacyScreen.navigateToScreen()
        privacyScreen.accept()
    }
}

package ch.admin.foitt.wallet.feature.onboarding.screens

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import ch.admin.foitt.wallet.platform.utils.TestTags

class OnboardingLocalDataScreen(composeTestRule: ComposeContentTestRule) : OnboardingBaseScreen(composeTestRule){
    override val imageIdentifier = TestTags.LOCAL_DATA_ICON.name
    override val image = composeTestRule.onNodeWithTag(TestTags.LOCAL_DATA_ICON.name)
    val backButton = composeTestRule.onNodeWithTag(TestTags.BACK_BUTTON.name)

    fun clickBack() {
        backButton.apply {
            assertIsDisplayed()
            performClick()
        }
    }

    fun navigateToScreen() {
        val presentScreen = OnboardingPresentScreen(composeTestRule)
        presentScreen.navigateToScreen()
        presentScreen.nextScreen()
        isDisplayed()
    }

}

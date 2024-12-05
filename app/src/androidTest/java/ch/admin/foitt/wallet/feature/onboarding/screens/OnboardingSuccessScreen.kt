package ch.admin.foitt.wallet.feature.onboarding.screens

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import ch.admin.foitt.wallet.platform.utils.TestTags

class OnboardingSuccessScreen(composeTestRule: ComposeContentTestRule) : OnboardingBaseScreen(composeTestRule) {
    override val imageIdentifier = TestTags.SUCCESS_ICON.name
    override val image = composeTestRule.onNodeWithTag(TestTags.SUCCESS_ICON.name)
    val backButton = composeTestRule.onNodeWithTag(TestTags.BACK_BUTTON.name)

    @OptIn(ExperimentalTestApi::class)
    override fun isDisplayed() {
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag(imageIdentifier), 10000)
        image.assertIsDisplayed()
        continueButton.assertIsDisplayed()
    }

    fun clickBack() {
        backButton.apply {
            assertIsDisplayed()
            performClick()
        }
    }

}

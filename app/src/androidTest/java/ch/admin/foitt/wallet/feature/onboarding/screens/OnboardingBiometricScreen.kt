package ch.admin.foitt.wallet.feature.onboarding.screens

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import ch.admin.foitt.wallet.platform.screens.BaseScreen
import ch.admin.foitt.wallet.platform.utils.TestTags

class OnboardingBiometricScreen(composeTestRule: ComposeContentTestRule) : BaseScreen(composeTestRule) {
    val image = composeTestRule.onNodeWithTag(TestTags.BIOMETRICS_UNAVAILABLE_ICON.name)
    val noBiometricsButton = composeTestRule.onNodeWithTag(TestTags.NO_BIOMETRICS_BUTTON.name)
    val settingsButton = composeTestRule.onNodeWithTag(TestTags.TO_SETTING_BUTTON.name)
    val backButton = composeTestRule.onNodeWithTag(TestTags.BACK_BUTTON.name)

    fun clickBack() {
        backButton.apply {
            assertIsDisplayed()
            performClick()
        }
    }

    @OptIn(ExperimentalTestApi::class)
    override fun isDisplayed() {
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag(TestTags.BIOMETRICS_UNAVAILABLE_ICON.name), 10000)
        image.assertIsDisplayed()
        noBiometricsButton.assertIsDisplayed()
        settingsButton.assertIsDisplayed()
    }

    fun noBiometric() {
        noBiometricsButton.apply {
            assertIsDisplayed()
            performClick()
        }
    }

    fun goToSettings() {
        settingsButton.apply {
            assertIsDisplayed()
            performClick()
        }
    }
}

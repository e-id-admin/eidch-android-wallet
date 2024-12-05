package ch.admin.foitt.wallet.feature.onboarding.screens

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import ch.admin.foitt.wallet.platform.utils.TestTags

class OnboardingIntroScreen(composeTestRule: ComposeContentTestRule) : OnboardingBaseScreen(composeTestRule) {
    override val imageIdentifier = TestTags.INTRO_ICON.name
    override val image = composeTestRule.onNodeWithTag(TestTags.INTRO_ICON.name)
    override val continueButton = composeTestRule.onNodeWithTag(TestTags.START_BUTTON.name)
}

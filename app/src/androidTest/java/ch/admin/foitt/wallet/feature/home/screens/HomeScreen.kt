package ch.admin.foitt.wallet.feature.home.screens

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import ch.admin.foitt.wallet.platform.screens.BaseScreen
import ch.admin.foitt.wallet.platform.utils.TestTags

class HomeScreen(composeTestRule: ComposeContentTestRule) : BaseScreen(composeTestRule) {
    val noCredentialIcon = composeTestRule.onNodeWithTag(TestTags.NO_CREDENTIAL_ICON.name)
    val menuButton = composeTestRule.onNodeWithTag(TestTags.MENU_BUTTON.name)

    @OptIn(ExperimentalTestApi::class)
    override fun isDisplayed() {
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag(TestTags.NO_CREDENTIAL_ICON.name), 10000)
        noCredentialIcon.assertIsDisplayed()
        menuButton.assertIsDisplayed()
    }
}

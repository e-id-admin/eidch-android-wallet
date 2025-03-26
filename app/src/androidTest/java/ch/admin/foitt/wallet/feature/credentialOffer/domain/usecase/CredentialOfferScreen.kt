package ch.admin.foitt.wallet.feature.credentialOffer.domain.usecase

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import ch.admin.foitt.wallet.platform.screens.BaseScreen
import ch.admin.foitt.wallet.platform.utils.TestTags

class CredentialOfferScreen(composeTestRule: ComposeContentTestRule) : BaseScreen(composeTestRule) {
    private val credential = composeTestRule.onNodeWithTag(TestTags.OFFER_CREDENTIAL.name)
    private val acceptButton = composeTestRule.onNodeWithTag(TestTags.ACCEPT_BUTTON.name)
    private val declineButton = composeTestRule.onNodeWithTag(TestTags.DECLINE_BUTTON.name)

    @OptIn(ExperimentalTestApi::class)
    override fun isDisplayed() {
        composeTestRule.waitUntilAtLeastOneExists(hasTestTag(TestTags.OFFER_CREDENTIAL.name), 10000)
        credential.assertIsDisplayed()
        acceptButton.assertIsDisplayed()
        declineButton.assertIsDisplayed()
    }

    fun acceptCredential() {
        acceptButton.apply {
            assertIsDisplayed()
            performClick()
        }
    }
}

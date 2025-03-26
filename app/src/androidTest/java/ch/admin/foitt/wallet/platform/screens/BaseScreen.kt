package ch.admin.foitt.wallet.platform.screens

import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeRight

open class BaseScreen(val composeTestRule: ComposeContentTestRule) {

    open fun isDisplayed() {}

    fun swipeBack() {
        composeTestRule.onRoot().performTouchInput {
            swipeRight()
        }
    }
}

package ch.admin.foitt.wallet.platform.utils

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.fragment.app.FragmentActivity

val LocalActivity = staticCompositionLocalOf<FragmentActivity> {
    error(message = "No FragmentActivity provided")
}

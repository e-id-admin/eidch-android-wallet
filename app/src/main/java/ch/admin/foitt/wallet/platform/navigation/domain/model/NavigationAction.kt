package ch.admin.foitt.wallet.platform.navigation.domain.model

import androidx.annotation.MainThread

fun interface NavigationAction {
    @MainThread
    fun navigate()
}

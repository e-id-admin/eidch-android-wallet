package ch.admin.foitt.wallet.platform.deeplink.domain.usecase

import androidx.annotation.CheckResult
import ch.admin.foitt.wallet.platform.navigation.domain.model.NavigationAction

fun interface HandleDeeplink {
    @CheckResult
    suspend operator fun invoke(fromOnboarding: Boolean): NavigationAction
}

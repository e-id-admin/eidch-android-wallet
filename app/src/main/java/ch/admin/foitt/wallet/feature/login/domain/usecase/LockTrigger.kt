package ch.admin.foitt.wallet.feature.login.domain.usecase

import androidx.annotation.CheckResult
import ch.admin.foitt.wallet.platform.navigation.domain.model.NavigationAction
import kotlinx.coroutines.flow.StateFlow

fun interface LockTrigger {
    @CheckResult
    suspend operator fun invoke(): StateFlow<NavigationAction>
}

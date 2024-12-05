package ch.admin.foitt.wallet.feature.sessionTimeout.domain

import androidx.annotation.CheckResult
import ch.admin.foitt.wallet.platform.userInteraction.domain.model.UserInteraction
import kotlinx.coroutines.flow.StateFlow

interface UserInteractionFlow {
    @CheckResult
    suspend operator fun invoke(): StateFlow<UserInteraction>
}

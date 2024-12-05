package ch.admin.foitt.wallet.platform.invitation.domain.usecase

import androidx.annotation.CheckResult
import ch.admin.foitt.wallet.platform.invitation.domain.model.ProcessInvitationResult
import ch.admin.foitt.wallet.platform.navigation.domain.model.NavigationAction

interface HandleInvitationProcessingSuccess {
    @CheckResult
    suspend operator fun invoke(successResult: ProcessInvitationResult): NavigationAction
}

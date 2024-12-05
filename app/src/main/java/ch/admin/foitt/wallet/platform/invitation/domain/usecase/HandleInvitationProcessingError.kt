package ch.admin.foitt.wallet.platform.invitation.domain.usecase

import androidx.annotation.CheckResult
import ch.admin.foitt.wallet.platform.invitation.domain.model.ProcessInvitationError
import ch.admin.foitt.wallet.platform.navigation.domain.model.NavigationAction

fun interface HandleInvitationProcessingError {
    @CheckResult
    suspend operator fun invoke(
        failureResult: ProcessInvitationError,
        invitationUri: String
    ): NavigationAction
}

package ch.admin.foitt.wallet.platform.invitation.domain.usecase

import ch.admin.foitt.wallet.platform.invitation.domain.model.ProcessInvitationError
import ch.admin.foitt.wallet.platform.invitation.domain.model.ProcessInvitationResult
import com.github.michaelbull.result.Result

fun interface ProcessInvitation {
    suspend operator fun invoke(invitationUri: String): Result<ProcessInvitationResult, ProcessInvitationError>
}

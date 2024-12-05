package ch.admin.foitt.wallet.platform.invitation.domain.usecase

import androidx.annotation.CheckResult
import ch.admin.foitt.openid4vc.domain.model.Invitation
import ch.admin.foitt.wallet.platform.invitation.domain.model.ValidateInvitationError
import com.github.michaelbull.result.Result

fun interface ValidateInvitation {
    @CheckResult
    suspend operator fun invoke(input: String): Result<Invitation, ValidateInvitationError>
}

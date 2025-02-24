package ch.admin.foitt.wallet.platform.navArgs.domain.model

import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationErrorScreenState

data class InvitationFailureNavArg(
    val invitationError: InvitationErrorScreenState,
)

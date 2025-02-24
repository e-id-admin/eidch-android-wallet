package ch.admin.foitt.wallet.platform.invitation.domain.model

/**
 * Parameter for the error screen state
 */
enum class InvitationErrorScreenState {
    INVALID_CREDENTIAL,
    UNKNOWN_ISSUER,
    NETWORK_ERROR,
    UNEXPECTED,
}

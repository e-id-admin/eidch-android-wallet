package ch.admin.foitt.wallet.platform.database.domain.model

/**
 * Credential Status provided by an online status list
 */
enum class CredentialStatus {
    VALID,
    REVOKED,
    SUSPENDED,
    UNSUPPORTED,
    UNKNOWN,
}

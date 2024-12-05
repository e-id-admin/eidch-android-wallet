package ch.admin.foitt.wallet.platform.database.domain.model

enum class CredentialStatus {
    VALID,
    REVOKED,
    SUSPENDED,
    EXPIRED,
    UNSUPPORTED,
    UNKNOWN,
}

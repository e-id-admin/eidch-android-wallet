package ch.admin.foitt.wallet.platform.credentialStatus.domain.model

internal enum class CredentialStatusListFlag(val mask: Int) {
    REVOKED(1),
    SUSPENDED(1 shl 1),
}

internal fun CredentialStatusListFlag.flagIn(bitValue: Int): Boolean = bitValue and mask != 0

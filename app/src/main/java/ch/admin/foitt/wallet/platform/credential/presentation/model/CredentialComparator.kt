package ch.admin.foitt.wallet.platform.credential.presentation.model

data class CredentialComparator(
    val state: CredentialCardState,
    val groupOrder: Int,
    val createdAt: Long,
)

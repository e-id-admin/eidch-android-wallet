package ch.admin.foitt.wallet.platform.ssi.domain.model

data class CredentialClaimText(
    override val localizedKey: String,
    val value: String,
) : CredentialClaimData

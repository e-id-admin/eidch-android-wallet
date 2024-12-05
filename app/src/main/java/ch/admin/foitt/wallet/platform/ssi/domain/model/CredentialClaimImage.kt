package ch.admin.foitt.wallet.platform.ssi.domain.model

data class CredentialClaimImage(
    override val localizedKey: String,
    val imageData: ByteArray,
) : CredentialClaimData {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CredentialClaimImage

        if (localizedKey != other.localizedKey) return false
        return imageData.contentEquals(other.imageData)
    }

    override fun hashCode(): Int {
        var result = localizedKey.hashCode()
        result = 31 * result + imageData.contentHashCode()
        return result
    }
}

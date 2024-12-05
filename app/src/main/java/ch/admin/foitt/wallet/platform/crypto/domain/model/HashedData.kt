package ch.admin.foitt.wallet.platform.crypto.domain.model

data class HashedData(val hash: ByteArray, val salt: ByteArray) {
    val hashHexString: String
        get() = hash.toHexString()

    private fun ByteArray.toHexString(): String =
        joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as HashedData

        if (!hash.contentEquals(other.hash)) return false
        if (!salt.contentEquals(other.salt)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = hash.contentHashCode()
        result = 31 * result + salt.contentHashCode()
        return result
    }
}

package ch.admin.foitt.wallet.platform.passphrase.domain.model

data class CiphertextWrapper(
    val ciphertext: ByteArray,
    val initializationVector: ByteArray,
) {

    // Automatically Generated equals method
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CiphertextWrapper

        if (!ciphertext.contentEquals(other.ciphertext)) return false
        if (!initializationVector.contentEquals(other.initializationVector)) return false

        return true
    }

    // Automatically Generated equals method
    override fun hashCode(): Int {
        var result = ciphertext.contentHashCode()
        result = 31 * result + initializationVector.contentHashCode()
        return result
    }
}

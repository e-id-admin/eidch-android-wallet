package ch.admin.foitt.wallet.platform.passphrase.domain.model

data class PepperedData(val hash: ByteArray, val initializationVector: ByteArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PepperedData

        if (!hash.contentEquals(other.hash)) return false
        if (!initializationVector.contentEquals(other.initializationVector)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = hash.contentHashCode()
        result = 31 * result + initializationVector.contentHashCode()
        return result
    }
}

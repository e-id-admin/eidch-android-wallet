package ch.admin.foitt.sriValidator.domain.implementation

import ch.admin.foitt.sriValidator.domain.SRIValidator
import ch.admin.foitt.sriValidator.domain.model.SRIError
import ch.admin.foitt.sriValidator.domain.model.SRIError.UnsupportedAlgorithm
import java.security.MessageDigest
import java.util.Base64
import javax.inject.Inject

class SRIValidatorImpl @Inject constructor() : SRIValidator {
    override fun validate(data: ByteArray, integrity: String): Boolean {
        val (algorithm, digest) = integrity.split("-", limit = 2).let { splits ->
            if (splits.size != 2) {
                throw SRIError.MalformedIntegrity
            }
            listOf(splits[0], splits[1])
        }

        if (algorithm.lowercase() !in supportedAlgorithms) throw UnsupportedAlgorithm(algorithm)

        val messageDigest = MessageDigest.getInstance(algorithm)
        val dataHash = messageDigest.digest(data)
        val dataHashBase64 = dataHash.toBase64ByteArray()
        return dataHashBase64.contentEquals(digest.encodeToByteArray())
    }

    private fun ByteArray.toBase64ByteArray(): ByteArray =
        Base64.getEncoder().withoutPadding().encode(this)

    companion object {
        private const val SHA256 = "sha256"
        private const val SHA384 = "sha384"
        private const val SHA512 = "sha512"
        val supportedAlgorithms = listOf(SHA256, SHA384, SHA512)
    }
}

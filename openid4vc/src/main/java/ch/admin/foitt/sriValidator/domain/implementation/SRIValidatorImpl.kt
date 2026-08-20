package ch.admin.foitt.sriValidator.domain.implementation

import ch.admin.foitt.openid4vc.utils.toNonUrlEncodedBase64String
import ch.admin.foitt.sriValidator.domain.SRIValidator
import ch.admin.foitt.sriValidator.domain.model.SRIError
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.binding
import java.security.MessageDigest
import javax.inject.Inject

class SRIValidatorImpl @Inject constructor() : SRIValidator {
    override fun invoke(data: ByteArray, integrity: String): Result<Unit, SRIError> = binding {
        val (algorithm, digest) = integrity.split("-", limit = 2).let { splits ->
            if (splits.size != 2) {
                Err(SRIError.MalformedIntegrity).bind<SRIError>()
            }
            listOf(splits[0], splits[1])
        }

        if (algorithm.lowercase() !in supportedAlgorithms) {
            Err(SRIError.UnsupportedAlgorithm(algorithm)).bind<SRIError>()
        }

        val messageDigest = MessageDigest.getInstance(algorithm)
        val dataHash = messageDigest.digest(data)
        val dataHashBase64 = dataHash.toNonUrlEncodedBase64String()

        if (dataHashBase64 != digest) {
            Err(SRIError.ValidationFailed).bind<SRIError>()
        }
    }

    companion object {
        private const val SHA256 = "sha256"
        private const val SHA384 = "sha384"
        private const val SHA512 = "sha512"
        val supportedAlgorithms = listOf(SHA256, SHA384, SHA512)
    }
}

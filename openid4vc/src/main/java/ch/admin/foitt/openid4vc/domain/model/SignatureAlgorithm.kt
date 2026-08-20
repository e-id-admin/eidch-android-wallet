package ch.admin.foitt.openid4vc.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Algorithms the wallet supports for signature validation.
 *
 * In contrast to [SigningAlgorithm], these algorithms are only used to verify
 * signatures of received artifacts (JWTs, trust statements, credentials, ...),
 * not to generate keys or create signatures.
 */
@Serializable
enum class SignatureAlgorithm(override val stdName: String) : Algorithm {
    @SerialName("ES512")
    ES512("ES512"),

    @SerialName("ES256")
    ES256("ES256"),

    @SerialName("EdDSA")
    EdDSA("EdDSA"),
    ;

    companion object {
        fun fromStdName(name: String): SignatureAlgorithm? = when (name) {
            "Ed25519" -> EdDSA
            else -> entries.firstOrNull { it.stdName == name }
        }

        fun fromStdNameOrThrow(name: String): SignatureAlgorithm = checkNotNull(fromStdName(name)) {
            "unsupported algorithm: $name"
        }
    }
}

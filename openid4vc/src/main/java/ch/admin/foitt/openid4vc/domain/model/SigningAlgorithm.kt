package ch.admin.foitt.openid4vc.domain.model

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.Curve
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Algorithms the wallet supports for key generation and signing.
 *
 * In contrast to [SignatureAlgorithm], these algorithms are used to generate the
 * wallet's own key pairs and create signatures (proofs, DPoP, key binding, ...).
 */
@Serializable
enum class SigningAlgorithm(override val stdName: String) : Algorithm {
    @SerialName("ES512")
    ES512("ES512"),

    @SerialName("ES256")
    ES256("ES256"),
    ;

    companion object {
        fun fromStdName(name: String): SigningAlgorithm? = entries.firstOrNull { it.stdName == name }
    }
}

fun SigningAlgorithm.toJWSAlgorithm(): JWSAlgorithm = when (this) {
    SigningAlgorithm.ES256 -> JWSAlgorithm.ES256
    SigningAlgorithm.ES512 -> JWSAlgorithm.ES512
}

fun SigningAlgorithm.toCurve(): Curve = when (this) {
    SigningAlgorithm.ES256 -> Curve.P_256
    SigningAlgorithm.ES512 -> Curve.P_521
}

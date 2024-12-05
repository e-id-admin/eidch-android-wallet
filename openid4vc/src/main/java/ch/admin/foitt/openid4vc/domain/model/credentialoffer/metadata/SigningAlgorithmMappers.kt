package ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata

import android.security.keystore.KeyProperties
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.Curve
import java.security.spec.AlgorithmParameterSpec
import java.security.spec.ECGenParameterSpec

internal fun SigningAlgorithm.toKeyAlgorithm() = when (this) {
    SigningAlgorithm.ES256,
    SigningAlgorithm.ES512 -> KeyProperties.KEY_ALGORITHM_EC
}

internal fun SigningAlgorithm.toAlgorithmParameterSpec(): AlgorithmParameterSpec = when (this) {
    SigningAlgorithm.ES256,
    SigningAlgorithm.ES512 -> ECGenParameterSpec(toCurve().stdName)
}

internal fun SigningAlgorithm.toDigest() = when (this) {
    SigningAlgorithm.ES256 -> KeyProperties.DIGEST_SHA256
    SigningAlgorithm.ES512 -> KeyProperties.DIGEST_SHA512
}

internal fun SigningAlgorithm.toJWSAlgorithm(): JWSAlgorithm = when (this) {
    SigningAlgorithm.ES256 -> JWSAlgorithm.ES256
    SigningAlgorithm.ES512 -> JWSAlgorithm.ES512
}

internal fun SigningAlgorithm.toCurve(): Curve = when (this) {
    SigningAlgorithm.ES256 -> Curve.P_256
    SigningAlgorithm.ES512 -> Curve.P_521
}

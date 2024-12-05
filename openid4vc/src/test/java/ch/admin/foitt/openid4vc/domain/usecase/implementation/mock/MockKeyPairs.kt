package ch.admin.foitt.openid4vc.domain.usecase.implementation.mock

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.JWSKeyPair
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.SigningAlgorithm
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.SecureRandom
import java.security.spec.ECGenParameterSpec

object MockKeyPairs {
    val VALID_KEY_PAIR = createValidKeyPair()
    val UNSUPPORTED_KEY_PAIR = createUnsupportedKeyPair()
    val INVALID_KEY_PAIR = createInvalidPrivateKeyKeyPair()
}

private fun createValidKeyPair(): JWSKeyPair {
    val generator = KeyPairGenerator.getInstance("EC")
    generator.initialize(ECGenParameterSpec("secp521r1"), SecureRandom())
    return createJwsKeyPair(generator)
}

private fun createUnsupportedKeyPair(): JWSKeyPair {
    val generator = KeyPairGenerator.getInstance("RSA")
    return createJwsKeyPair(generator)
}

private fun createInvalidPrivateKeyKeyPair(): JWSKeyPair {
    val keyPair = createValidKeyPair()
    val otherKeyPair = createUnsupportedKeyPair()
    return JWSKeyPair(
        algorithm = SigningAlgorithm.ES512,
        keyPair = KeyPair(keyPair.keyPair.public, otherKeyPair.keyPair.private),
        keyId = MockCredentialOffer.KEY_ID
    )
}

fun createJwsKeyPair(generator: KeyPairGenerator): JWSKeyPair {
    val keyPair = generator.generateKeyPair()
    return JWSKeyPair(
        algorithm = SigningAlgorithm.ES512,
        keyPair = keyPair,
        keyId = MockCredentialOffer.KEY_ID
    )
}

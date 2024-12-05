package ch.admin.foitt.openid4vc.domain.model.credentialoffer

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.SigningAlgorithm
import java.security.KeyPair

interface SigningKeyPair {
    val keyPair: KeyPair
    val keyId: String
}

data class JWSKeyPair(
    val algorithm: SigningAlgorithm,
    override val keyPair: KeyPair,
    override val keyId: String
) : SigningKeyPair

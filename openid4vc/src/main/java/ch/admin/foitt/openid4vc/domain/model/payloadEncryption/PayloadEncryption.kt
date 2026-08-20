package ch.admin.foitt.openid4vc.domain.model.payloadEncryption

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialRequestEncryption
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialResponseEncryption

data class PayloadEncryption(
    val requestEncryption: CredentialRequestEncryption,
    val responseEncryption: CredentialResponseEncryption,
    val responseEncryptionKeyPair: PayloadEncryptionKeyPair,
)

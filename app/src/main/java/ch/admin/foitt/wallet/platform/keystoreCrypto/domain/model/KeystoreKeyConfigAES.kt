package ch.admin.foitt.wallet.platform.keystoreCrypto.domain.model

import android.security.keystore.KeyProperties

data class KeystoreKeyConfigAES constructor(
    override val encryptionKeyAlias: String,
    override val userAuthenticationRequired: Boolean,
    override val randomizedEncryptionRequired: Boolean,
) : KeystoreKeyConfig {
    override val encryptionKeyPurpose: Int = KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
    override val encryptionKeySize: Int = 256
    override val gcmAuthTagLength: Int = 128
    override val encryptionBlockMode: String = KeyProperties.BLOCK_MODE_GCM
    override val encryptionPaddings: String = KeyProperties.ENCRYPTION_PADDING_NONE
    override val encryptionAlgorithm: String = KeyProperties.KEY_ALGORITHM_AES
}

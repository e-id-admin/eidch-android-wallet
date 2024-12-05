package ch.admin.foitt.wallet.platform.keystoreCrypto.domain.model

import android.os.Build
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import ch.admin.foitt.openid4vc.utils.Constants.ANDROID_KEY_STORE

interface KeystoreKeyConfig {
    @Suppress("SameReturnValue")
    val keystoreName: String get() = ANDROID_KEY_STORE
    val encryptionKeyAlias: String
    val encryptionKeyPurpose: Int
    val encryptionKeySize: Int
    val gcmAuthTagLength: Int
    val encryptionBlockMode: String
    val encryptionPaddings: String
    val encryptionAlgorithm: String
    val userAuthenticationRequired: Boolean
    val randomizedEncryptionRequired: Boolean

    val encryptionTransformation: String
        get() = "$encryptionAlgorithm/$encryptionBlockMode/$encryptionPaddings"

    // Devices with <API29 do not have yet a Keystore that supports this parameter.
    @get:RequiresApi(Build.VERSION_CODES.R)
    @Suppress("SameReturnValue")
    val allowedKeyStoreAuthenticators: Int
        get() = KeyProperties.AUTH_BIOMETRIC_STRONG
}

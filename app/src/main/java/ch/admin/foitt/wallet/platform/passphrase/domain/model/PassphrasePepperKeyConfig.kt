package ch.admin.foitt.wallet.platform.passphrase.domain.model

import android.security.keystore.KeyProperties
import ch.admin.foitt.wallet.platform.keystoreCrypto.domain.model.KeystoreKeyConfig
import ch.admin.foitt.wallet.platform.keystoreCrypto.domain.model.KeystoreKeyConfigAES
import javax.inject.Inject

class PassphrasePepperKeyConfig @Inject constructor(
    override val encryptionKeyPurpose: Int = KeyProperties.PURPOSE_ENCRYPT
) : KeystoreKeyConfig by KeystoreKeyConfigAES(
    encryptionKeyAlias = "walletPassphrasePepperKey",
    userAuthenticationRequired = false,
    randomizedEncryptionRequired = false,
)

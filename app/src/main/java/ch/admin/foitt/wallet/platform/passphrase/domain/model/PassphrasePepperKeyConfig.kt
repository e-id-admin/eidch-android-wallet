package ch.admin.foitt.wallet.platform.passphrase.domain.model

import ch.admin.foitt.wallet.platform.keystoreCrypto.domain.model.KeystoreKeyConfig
import ch.admin.foitt.wallet.platform.keystoreCrypto.domain.model.KeystoreKeyConfigAES
import javax.inject.Inject

class PassphrasePepperKeyConfig @Inject constructor() : KeystoreKeyConfig by KeystoreKeyConfigAES(
    encryptionKeyAlias = "walletPassphrasePepperKey",
    userAuthenticationRequired = false,
    randomizedEncryptionRequired = false,
)

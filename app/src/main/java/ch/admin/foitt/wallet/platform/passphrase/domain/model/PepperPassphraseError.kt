package ch.admin.foitt.wallet.platform.passphrase.domain.model

import ch.admin.foitt.wallet.platform.keystoreCrypto.domain.model.GetCipherForEncryptionError

sealed interface PepperPassphraseError {
    data class Unexpected(val throwable: Throwable) : PepperPassphraseError
}

//region Error to Error mappings
fun GetCipherForEncryptionError.toPepperPassphraseError(): PepperPassphraseError = when (this) {
    is GetCipherForEncryptionError.InvalidKeyError -> PepperPassphraseError.Unexpected(Exception(""))
    is GetCipherForEncryptionError.Unexpected -> PepperPassphraseError.Unexpected(this.throwable)
}
//endregion

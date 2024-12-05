package ch.admin.foitt.wallet.platform.keystoreCrypto.domain.model

import java.security.InvalidKeyException

sealed interface GetCipherForDecryptionError {
    data object KeyInvalidated : GetCipherForDecryptionError
    data class Unexpected(
        val throwable: Throwable
    ) : GetCipherForDecryptionError
}

internal fun Throwable.toGetCipherForDecryptionError(): GetCipherForDecryptionError = when (this) {
    is InvalidKeyException -> GetCipherForDecryptionError.KeyInvalidated
    else -> GetCipherForDecryptionError.Unexpected(this)
}

sealed interface GetCipherForEncryptionError {
    object InvalidKeyError : GetCipherForEncryptionError

    data class Unexpected(
        val throwable: Throwable
    ) : GetCipherForEncryptionError
}

internal fun Throwable.toGetCipherForEncryptionError(): GetCipherForEncryptionError = when (this) {
    is InvalidKeyException -> GetCipherForEncryptionError.InvalidKeyError
    else -> GetCipherForEncryptionError.Unexpected(this)
}

sealed interface GetOrCreateSecretKeyError {
    val throwable: Throwable

    data class Unexpected(
        override val throwable: Throwable
    ) : GetOrCreateSecretKeyError
}

sealed interface DeleteSecretKeyError {
    data class Unexpected(val throwable: Throwable) : DeleteSecretKeyError
}

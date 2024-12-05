package ch.admin.foitt.wallet.platform.passphrase.domain.usecase

import ch.admin.foitt.wallet.platform.passphrase.domain.model.EncryptAndSavePassphraseError
import com.github.michaelbull.result.Result
import javax.crypto.Cipher

fun interface EncryptAndSavePassphrase {
    suspend operator fun invoke(
        passphrase: ByteArray,
        encryptionCipher: Cipher,
    ): Result<Unit, EncryptAndSavePassphraseError>
}

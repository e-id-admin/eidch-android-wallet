package ch.admin.foitt.wallet.platform.passphrase.domain.usecase

import androidx.annotation.CheckResult
import ch.admin.foitt.wallet.platform.passphrase.domain.model.LoadAndDecryptPassphraseError
import com.github.michaelbull.result.Result
import javax.crypto.Cipher

fun interface LoadAndDecryptPassphrase {
    @CheckResult
    suspend operator fun invoke(decryptionCipher: Cipher): Result<ByteArray, LoadAndDecryptPassphraseError>
}

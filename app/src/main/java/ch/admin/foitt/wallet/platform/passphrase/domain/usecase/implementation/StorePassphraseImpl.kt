package ch.admin.foitt.wallet.platform.passphrase.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.di.IoDispatcher
import ch.admin.foitt.wallet.platform.passphrase.domain.model.StorePassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.model.toStorePassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.EncryptAndSavePassphrase
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.HashPassphrase
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.PepperPassphrase
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.StorePassphrase
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.crypto.Cipher
import javax.inject.Inject

class StorePassphraseImpl @Inject constructor(
    private val hashPassphrase: HashPassphrase,
    private val encryptAndSavePassphrase: EncryptAndSavePassphrase,
    private val pepperPassphrase: PepperPassphrase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : StorePassphrase {
    override suspend fun invoke(
        pin: String,
        encryptionCipher: Cipher,
    ): Result<Unit, StorePassphraseError> = withContext(ioDispatcher) {
        coroutineBinding {
            val pinHash = hashPassphrase(
                pin = pin,
                initializeSalt = false,
            ).mapError { error ->
                error.toStorePassphraseError()
            }.bind()

            val pinHashPeppered = pepperPassphrase(
                passphrase = pinHash.hash,
                initializePepper = false,
            ).mapError { error ->
                error.toStorePassphraseError()
            }.bind()

            Timber.d("Passphrase, CryptoObject present, encrypting passphrase")
            encryptAndSavePassphrase(
                pinHashPeppered.hash,
                encryptionCipher,
            ).mapError { error ->
                error.toStorePassphraseError()
            }.bind()
        }
    }
}

package ch.admin.foitt.wallet.platform.passphrase.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.database.domain.usecase.CreateAppDatabase
import ch.admin.foitt.wallet.platform.di.IoDispatcher
import ch.admin.foitt.wallet.platform.passphrase.domain.model.InitializePassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.model.toInitializePassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.repository.PepperIvRepository
import ch.admin.foitt.wallet.platform.passphrase.domain.repository.SaltRepository
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.EncryptAndSavePassphrase
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.HashPassphrase
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.InitializePassphrase
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.PepperPassphrase
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.crypto.Cipher
import javax.inject.Inject

@Suppress("LongParameterList")
class InitializePassphraseImpl @Inject constructor(
    private val hashPassphrase: HashPassphrase,
    private val saltRepository: SaltRepository,
    private val pepperIvRepository: PepperIvRepository,
    private val encryptAndSavePassphrase: EncryptAndSavePassphrase,
    private val createAppDatabase: CreateAppDatabase,
    private val pepperPassphrase: PepperPassphrase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : InitializePassphrase {
    override suspend fun invoke(
        pin: String,
        encryptionCipher: Cipher?,
    ): Result<Unit, InitializePassphraseError> = withContext(ioDispatcher) {
        coroutineBinding {
            val pinHash = hashPassphrase(
                pin = pin,
                initializeSalt = true,
            ).mapError { error ->
                error.toInitializePassphraseError()
            }.bind()

            val pinHashPeppered = pepperPassphrase(
                passphrase = pinHash.hash,
                initializePepper = true,
            ).mapError { error ->
                error.toInitializePassphraseError()
            }.bind()

            createAppDatabase(
                passphrase = pinHashPeppered.hash,
            ).mapError { error ->
                error.toInitializePassphraseError()
            }.bind()

            saltRepository.save(pinHash.salt)
            pepperIvRepository.save(pinHashPeppered.initializationVector)

            if (encryptionCipher != null) {
                Timber.d("Passphrase, CryptoObject present, encrypting passphrase")
                encryptAndSavePassphrase(
                    pinHashPeppered.hash,
                    encryptionCipher,
                ).mapError { error ->
                    error.toInitializePassphraseError()
                }.bind()
            }
        }
    }
}

package ch.admin.foitt.wallet.platform.passphrase.data.repository

import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import ch.admin.foitt.wallet.platform.passphrase.domain.model.CiphertextWrapper
import ch.admin.foitt.wallet.platform.passphrase.domain.repository.PassphraseRepository
import ch.admin.foitt.wallet.platform.utils.base64StringToByteArray
import ch.admin.foitt.wallet.platform.utils.toBase64String
import timber.log.Timber
import javax.inject.Inject

internal class PassphraseRepositoryImpl @Inject constructor(
    private val sharedPreferences: EncryptedSharedPreferences,
) : PassphraseRepository {

    private val passphraseKey = "wallet_passphrase"
    private val initializationVectorKey = "wallet_passphrase_iv"

    private var passphraseWasDeleted: Boolean = false

    override suspend fun getPassphrase(): CiphertextWrapper {
        val base64EncryptedPassphrase = sharedPreferences.getString(passphraseKey, null) ?: ""
        val base64InitializationVector = sharedPreferences.getString(initializationVectorKey, null) ?: ""
        val encryptedPassphraseBytes = base64EncryptedPassphrase.base64StringToByteArray()
        val initializationVectorBytes = base64InitializationVector.base64StringToByteArray()

        Timber.d("Passphrase retrieved")

        return CiphertextWrapper(
            ciphertext = encryptedPassphraseBytes,
            initializationVector = initializationVectorBytes,
        )
    }

    override suspend fun savePassphrase(passphraseWrapper: CiphertextWrapper) {
        passphraseWasDeleted = false
        // Simply converting the ByteArrays to Strings leads to weird results with shared preferences.
        // Base64 conversion is safe.
        val base64CipherText = passphraseWrapper.ciphertext.toBase64String()
        val base64InitVector = passphraseWrapper.initializationVector.toBase64String()

        sharedPreferences.edit {
            putString(passphraseKey, base64CipherText)
            putString(initializationVectorKey, base64InitVector)
        }
        Timber.d("Passphrase saved")
    }

    override suspend fun deletePassphrase() {
        sharedPreferences.edit {
            putString(passphraseKey, null)
            putString(initializationVectorKey, null)
        }
        Timber.d("Passphrase deleted")
    }

    override suspend fun savePassphraseWasDeleted(passphraseWasDeleted: Boolean) {
        this.passphraseWasDeleted = passphraseWasDeleted
    }

    override suspend fun getPassphraseWasDeleted() = passphraseWasDeleted
}

package ch.admin.foitt.wallet.feature.changeLogin.data.repository

import androidx.security.crypto.EncryptedSharedPreferences
import ch.admin.foitt.wallet.feature.changeLogin.domain.repository.NewPassphraseConfirmationAttemptsRepository
import javax.inject.Inject

class NewPassphraseConfirmationAttemptsRepositoryImpl @Inject constructor(
    private val sharedPreferences: EncryptedSharedPreferences,
) : NewPassphraseConfirmationAttemptsRepository {

    private val prefKey = "new_passphrase_confirmation_attempts"

    override fun getAttempts(): Int {
        return sharedPreferences.getInt(prefKey, 0)
    }

    override fun increase() {
        val attempts = getAttempts() + 1
        with(sharedPreferences.edit()) {
            putInt(prefKey, attempts)
            apply()
        }
    }

    override fun deleteAttempts() {
        with(sharedPreferences.edit()) {
            remove(prefKey)
            apply()
        }
    }
}

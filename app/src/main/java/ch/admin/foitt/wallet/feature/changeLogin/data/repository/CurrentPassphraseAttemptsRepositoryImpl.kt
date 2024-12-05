package ch.admin.foitt.wallet.feature.changeLogin.data.repository

import androidx.security.crypto.EncryptedSharedPreferences
import ch.admin.foitt.wallet.feature.changeLogin.domain.repository.CurrentPassphraseAttemptsRepository
import javax.inject.Inject

class CurrentPassphraseAttemptsRepositoryImpl @Inject constructor(
    private val sharedPreferences: EncryptedSharedPreferences,
) : CurrentPassphraseAttemptsRepository {

    private val prefKey = "current_passphrase_attempts"

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

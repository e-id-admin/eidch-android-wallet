package ch.admin.foitt.wallet.feature.login.data.repository

import androidx.security.crypto.EncryptedSharedPreferences
import ch.admin.foitt.wallet.feature.login.domain.repository.LoginAttemptsRepository
import javax.inject.Inject

class LoginAttemptsRepositoryImpl @Inject constructor(
    private val sharedPreferences: EncryptedSharedPreferences,
) : LoginAttemptsRepository {

    private val prefKey = "login_attempts"

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

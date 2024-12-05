package ch.admin.foitt.wallet.feature.login.data.repository

import androidx.security.crypto.EncryptedSharedPreferences
import ch.admin.foitt.wallet.feature.login.domain.repository.LockoutStartRepository
import javax.inject.Inject

class DeviceLockoutStartRepository @Inject constructor(
    private val sharedPreferences: EncryptedSharedPreferences,
) : LockoutStartRepository {

    private val prefKey = "lockout_starting_time"

    override fun getStartingTime(): Long {
        return sharedPreferences.getLong(prefKey, -1)
    }

    override fun deleteStartingTime() {
        with(sharedPreferences.edit()) {
            remove(prefKey)
            apply()
        }
    }

    override fun saveStartingTime(uptime: Long) {
        with(sharedPreferences.edit()) {
            putLong(prefKey, uptime)
            apply()
        }
    }
}

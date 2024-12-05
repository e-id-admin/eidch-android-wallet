package ch.admin.foitt.wallet.platform.appSetupState.data.repository

import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import ch.admin.foitt.wallet.platform.appSetupState.domain.repository.UseBiometricLoginRepository
import javax.inject.Inject

class UseBiometricLoginRepositoryImpl @Inject constructor(
    private val sharedPreferences: EncryptedSharedPreferences,
) : UseBiometricLoginRepository {

    private val prefKey = "use_biometrics_for_login"

    override suspend fun getUseBiometricLogin() = sharedPreferences.getBoolean(prefKey, false)

    override suspend fun saveUseBiometricLogin(isEnabled: Boolean) = sharedPreferences.edit {
        putBoolean(prefKey, isEnabled)
    }
}

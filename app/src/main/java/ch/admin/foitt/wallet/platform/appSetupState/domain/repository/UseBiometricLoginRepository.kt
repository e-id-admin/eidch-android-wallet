package ch.admin.foitt.wallet.platform.appSetupState.domain.repository

interface UseBiometricLoginRepository {
    suspend fun saveUseBiometricLogin(isEnabled: Boolean)
    suspend fun getUseBiometricLogin(): Boolean
}

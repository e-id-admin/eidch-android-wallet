package ch.admin.foitt.wallet.platform.biometrics.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.appSetupState.domain.repository.UseBiometricLoginRepository
import ch.admin.foitt.wallet.platform.biometrics.domain.usecase.SaveUseBiometricLogin
import javax.inject.Inject

class SaveUseBiometricLoginImpl @Inject constructor(
    private val repo: UseBiometricLoginRepository,
) : SaveUseBiometricLogin {
    override suspend fun invoke(isEnabled: Boolean) = repo.saveUseBiometricLogin(isEnabled)
}

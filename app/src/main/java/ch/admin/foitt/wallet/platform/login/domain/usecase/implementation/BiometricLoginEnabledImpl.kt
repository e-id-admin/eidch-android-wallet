package ch.admin.foitt.wallet.platform.login.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.appSetupState.domain.repository.UseBiometricLoginRepository
import ch.admin.foitt.wallet.platform.login.domain.usecase.BiometricLoginEnabled
import javax.inject.Inject

class BiometricLoginEnabledImpl @Inject constructor(
    private val repo: UseBiometricLoginRepository,
) : BiometricLoginEnabled {

    override suspend fun invoke(): Boolean = repo.getUseBiometricLogin()
}

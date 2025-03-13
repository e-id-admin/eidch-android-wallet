package ch.admin.foitt.wallet.platform.login.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.login.domain.repository.LoginAttemptsRepository
import ch.admin.foitt.wallet.platform.login.domain.usecase.IncreaseFailedLoginAttemptsCounter
import javax.inject.Inject

class IncreaseFailedLoginAttemptsCounterImpl @Inject constructor(
    private val loginAttemptsRepository: LoginAttemptsRepository,
) : IncreaseFailedLoginAttemptsCounter {
    override fun invoke() = loginAttemptsRepository.increase()
}

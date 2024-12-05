package ch.admin.foitt.wallet.feature.login.domain.usecase.implementation

import ch.admin.foitt.wallet.feature.login.domain.repository.LoginAttemptsRepository
import ch.admin.foitt.wallet.feature.login.domain.usecase.IncreaseFailedLoginAttemptsCounter
import javax.inject.Inject

class IncreaseFailedLoginAttemptsCounterImpl @Inject constructor(
    private val loginAttemptsRepository: LoginAttemptsRepository,
) : IncreaseFailedLoginAttemptsCounter {
    override fun invoke() = loginAttemptsRepository.increase()
}

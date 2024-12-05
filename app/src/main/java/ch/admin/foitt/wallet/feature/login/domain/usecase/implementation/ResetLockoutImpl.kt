package ch.admin.foitt.wallet.feature.login.domain.usecase.implementation

import ch.admin.foitt.wallet.feature.login.domain.repository.LockoutStartRepository
import ch.admin.foitt.wallet.feature.login.domain.repository.LoginAttemptsRepository
import ch.admin.foitt.wallet.feature.login.domain.usecase.ResetLockout
import javax.inject.Inject

class ResetLockoutImpl @Inject constructor(
    private val lockoutStartRepository: LockoutStartRepository,
    private val loginAttemptsRepository: LoginAttemptsRepository,
) : ResetLockout {
    override fun invoke() {
        lockoutStartRepository.deleteStartingTime()
        loginAttemptsRepository.deleteAttempts()
    }
}

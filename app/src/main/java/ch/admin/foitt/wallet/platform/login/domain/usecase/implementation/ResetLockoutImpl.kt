package ch.admin.foitt.wallet.platform.login.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.login.domain.repository.LockoutStartRepository
import ch.admin.foitt.wallet.platform.login.domain.repository.LoginAttemptsRepository
import ch.admin.foitt.wallet.platform.login.domain.usecase.ResetLockout
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

package ch.admin.foitt.wallet.feature.changeLogin.domain.usecase.implementation

import ch.admin.foitt.wallet.feature.changeLogin.domain.repository.NewPassphraseConfirmationAttemptsRepository
import ch.admin.foitt.wallet.feature.changeLogin.domain.usecase.IncreaseFailedNewPassphraseConfirmationAttemptsCounter
import javax.inject.Inject

class IncreaseFailedNewPassphraseConfirmationAttemptsCounterImpl @Inject constructor(
    private val newPassphraseConfirmationAttemptsRepository: NewPassphraseConfirmationAttemptsRepository,
) : IncreaseFailedNewPassphraseConfirmationAttemptsCounter {
    override fun invoke() = newPassphraseConfirmationAttemptsRepository.increase()
}

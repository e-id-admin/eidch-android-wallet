package ch.admin.foitt.wallet.feature.changeLogin.domain.usecase.implementation

import ch.admin.foitt.wallet.feature.changeLogin.domain.repository.CurrentPassphraseAttemptsRepository
import ch.admin.foitt.wallet.feature.changeLogin.domain.usecase.IncreaseFailedCurrentPassphraseAttemptsCounter
import javax.inject.Inject

class IncreaseFailedCurrentPassphraseAttemptsCounterImpl @Inject constructor(
    private val currentPassphraseAttemptsRepository: CurrentPassphraseAttemptsRepository,
) : IncreaseFailedCurrentPassphraseAttemptsCounter {
    override fun invoke() = currentPassphraseAttemptsRepository.increase()
}

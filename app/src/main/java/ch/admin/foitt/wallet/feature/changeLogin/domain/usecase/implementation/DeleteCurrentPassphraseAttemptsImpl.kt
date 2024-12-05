package ch.admin.foitt.wallet.feature.changeLogin.domain.usecase.implementation

import ch.admin.foitt.wallet.feature.changeLogin.domain.repository.CurrentPassphraseAttemptsRepository
import ch.admin.foitt.wallet.feature.changeLogin.domain.usecase.DeleteCurrentPassphraseAttempts
import javax.inject.Inject

class DeleteCurrentPassphraseAttemptsImpl @Inject constructor(
    private val currentPassphraseAttemptsRepository: CurrentPassphraseAttemptsRepository
) : DeleteCurrentPassphraseAttempts {
    override fun invoke() = currentPassphraseAttemptsRepository.deleteAttempts()
}

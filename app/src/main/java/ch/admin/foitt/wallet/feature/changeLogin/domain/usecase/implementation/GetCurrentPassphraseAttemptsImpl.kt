package ch.admin.foitt.wallet.feature.changeLogin.domain.usecase.implementation

import ch.admin.foitt.wallet.feature.changeLogin.domain.Constants.MAX_CURRENT_PASSPHRASE_ATTEMPTS
import ch.admin.foitt.wallet.feature.changeLogin.domain.repository.CurrentPassphraseAttemptsRepository
import ch.admin.foitt.wallet.feature.changeLogin.domain.usecase.GetCurrentPassphraseAttempts
import javax.inject.Inject

class GetCurrentPassphraseAttemptsImpl @Inject constructor(
    private val currentPassphraseAttemptsRepository: CurrentPassphraseAttemptsRepository,
) : GetCurrentPassphraseAttempts {
    override fun invoke(): Int = MAX_CURRENT_PASSPHRASE_ATTEMPTS - currentPassphraseAttemptsRepository.getAttempts()
}

package ch.admin.foitt.wallet.feature.changeLogin.domain.usecase.implementation

import ch.admin.foitt.wallet.feature.changeLogin.domain.repository.NewPassphraseConfirmationAttemptsRepository
import ch.admin.foitt.wallet.feature.changeLogin.domain.usecase.DeleteNewPassphraseConfirmationAttempts
import javax.inject.Inject

class DeleteNewPassphraseConfirmationAttemptsImpl @Inject constructor(
    private val newPassphraseConfirmationAttemptsRepository: NewPassphraseConfirmationAttemptsRepository,
) : DeleteNewPassphraseConfirmationAttempts {
    override fun invoke() = newPassphraseConfirmationAttemptsRepository.deleteAttempts()
}

package ch.admin.foitt.wallet.feature.changeLogin.domain.usecase.implementation

import ch.admin.foitt.wallet.feature.changeLogin.domain.Constants.MAX_NEW_PASSPHRASE_CONFIRMATION_ATTEMPTS
import ch.admin.foitt.wallet.feature.changeLogin.domain.repository.NewPassphraseConfirmationAttemptsRepository
import ch.admin.foitt.wallet.feature.changeLogin.domain.usecase.GetNewPassphraseConfirmationAttempts
import javax.inject.Inject

class GetNewPassphraseConfirmationAttemptsImpl @Inject constructor(
    private val newPassphraseConfirmationAttemptsRepository: NewPassphraseConfirmationAttemptsRepository,
) : GetNewPassphraseConfirmationAttempts {
    override fun invoke(): Int = MAX_NEW_PASSPHRASE_CONFIRMATION_ATTEMPTS - newPassphraseConfirmationAttemptsRepository.getAttempts()
}

package ch.admin.foitt.wallet.platform.passphraseInput.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.passphraseInput.domain.model.PassphraseConstraints
import ch.admin.foitt.wallet.platform.passphraseInput.domain.model.PassphraseValidationState
import ch.admin.foitt.wallet.platform.passphraseInput.domain.usecase.ValidatePassphrase
import javax.inject.Inject

class ValidatePassphraseImpl @Inject constructor(
    private val constraints: PassphraseConstraints,
) : ValidatePassphrase {

    override fun invoke(passphrase: String): PassphraseValidationState {
        val state = when {
            passphrase.length < constraints.minLength -> PassphraseValidationState.INVALID_MIN_LENGTH
            else -> PassphraseValidationState.VALID
        }
        return state
    }
}

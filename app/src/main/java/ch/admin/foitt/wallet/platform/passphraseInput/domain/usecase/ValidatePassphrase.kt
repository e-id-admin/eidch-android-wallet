package ch.admin.foitt.wallet.platform.passphraseInput.domain.usecase

import ch.admin.foitt.wallet.platform.passphraseInput.domain.model.PassphraseValidationState

interface ValidatePassphrase {
    operator fun invoke(passphrase: String): PassphraseValidationState
}

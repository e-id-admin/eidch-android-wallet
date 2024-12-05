package ch.admin.foitt.wallet.platform.passphrase.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.passphrase.domain.repository.PassphraseRepository
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.SavePassphraseWasDeleted
import javax.inject.Inject

class SavePassphraseWasDeletedImpl @Inject constructor(
    private val passphraseRepository: PassphraseRepository,
) : SavePassphraseWasDeleted {
    override suspend fun invoke(passphraseWasDeleted: Boolean) {
        passphraseRepository.savePassphraseWasDeleted(passphraseWasDeleted)
    }
}

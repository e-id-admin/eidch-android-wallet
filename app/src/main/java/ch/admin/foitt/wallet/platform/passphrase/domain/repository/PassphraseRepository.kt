package ch.admin.foitt.wallet.platform.passphrase.domain.repository

import ch.admin.foitt.wallet.platform.passphrase.domain.model.CiphertextWrapper

interface PassphraseRepository {
    suspend fun getPassphrase(): CiphertextWrapper
    suspend fun savePassphrase(passphraseWrapper: CiphertextWrapper)
    suspend fun deletePassphrase()
    suspend fun savePassphraseWasDeleted(passphraseWasDeleted: Boolean)
    suspend fun getPassphraseWasDeleted(): Boolean
}

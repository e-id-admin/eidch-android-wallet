package ch.admin.foitt.wallet.feature.changeLogin.domain.repository

interface NewPassphraseConfirmationAttemptsRepository {
    fun getAttempts(): Int
    fun increase()
    fun deleteAttempts()
}

package ch.admin.foitt.wallet.feature.changeLogin.domain.repository

interface CurrentPassphraseAttemptsRepository {
    fun getAttempts(): Int
    fun increase()
    fun deleteAttempts()
}

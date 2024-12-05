package ch.admin.foitt.wallet.feature.login.domain.repository

interface LoginAttemptsRepository {
    fun getAttempts(): Int
    fun increase()
    fun deleteAttempts()
}

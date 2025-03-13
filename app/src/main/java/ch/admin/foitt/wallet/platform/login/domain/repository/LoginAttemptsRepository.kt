package ch.admin.foitt.wallet.platform.login.domain.repository

interface LoginAttemptsRepository {
    fun getAttempts(): Int
    fun increase()
    fun deleteAttempts()
}

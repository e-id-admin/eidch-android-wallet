package ch.admin.foitt.wallet.platform.login.domain.repository

interface LockoutStartRepository {
    fun getStartingTime(): Long
    fun saveStartingTime(uptime: Long)
    fun deleteStartingTime()
}

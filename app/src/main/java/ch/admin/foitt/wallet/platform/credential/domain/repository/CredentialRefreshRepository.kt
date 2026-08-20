package ch.admin.foitt.wallet.platform.credential.domain.repository

interface CredentialRefreshRepository {
    /**
     * @return true when no refresh has run within the cooldown window.
     */
    fun isRefreshDue(): Boolean

    /**
     * Claims/stamps the cooldown window. Call when a refresh actually starts.
     */
    fun markRefreshed()
}

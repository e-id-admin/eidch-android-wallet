package ch.admin.foitt.wallet.platform.credential.data

import ch.admin.foitt.wallet.platform.credential.domain.repository.CredentialRefreshRepository
import java.time.Instant
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject

class CredentialRefreshRepositoryImpl @Inject constructor() : CredentialRefreshRepository {
    private val lastRefreshAt = AtomicLong(0L)

    override fun isRefreshDue(): Boolean =
        Instant.now().epochSecond - lastRefreshAt.get() >= COOLDOWN_SECONDS

    override fun markRefreshed() {
        lastRefreshAt.set(Instant.now().epochSecond)
    }

    companion object {
        private const val COOLDOWN_SECONDS = 15L
    }
}

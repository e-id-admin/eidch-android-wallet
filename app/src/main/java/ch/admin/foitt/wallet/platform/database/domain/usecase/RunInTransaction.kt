package ch.admin.foitt.wallet.platform.database.domain.usecase

interface RunInTransaction {
    suspend operator fun <V> invoke(block: suspend () -> V): V?
}

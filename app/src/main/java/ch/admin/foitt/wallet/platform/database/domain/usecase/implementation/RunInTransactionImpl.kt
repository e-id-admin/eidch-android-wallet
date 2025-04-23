package ch.admin.foitt.wallet.platform.database.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.database.domain.repository.DatabaseRepository
import ch.admin.foitt.wallet.platform.database.domain.usecase.RunInTransaction
import javax.inject.Inject

class RunInTransactionImpl @Inject constructor(
    private val databaseRepository: DatabaseRepository,
) : RunInTransaction {
    override suspend fun <V> invoke(block: suspend () -> V): V? = databaseRepository.runInTransaction(block)
}

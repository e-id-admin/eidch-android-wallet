package ch.admin.foitt.wallet.platform.database.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.database.domain.model.OpenDatabaseError
import ch.admin.foitt.wallet.platform.database.domain.repository.DatabaseRepository
import ch.admin.foitt.wallet.platform.database.domain.usecase.OpenAppDatabase
import com.github.michaelbull.result.Result
import javax.inject.Inject

class OpenAppDatabaseImpl @Inject constructor(
    private val databaseRepository: DatabaseRepository,
) : OpenAppDatabase {

    override suspend fun invoke(passphrase: ByteArray): Result<Unit, OpenDatabaseError> = databaseRepository.open(passphrase = passphrase)
}

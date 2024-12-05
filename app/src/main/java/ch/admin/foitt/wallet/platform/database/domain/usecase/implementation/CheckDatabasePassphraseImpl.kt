package ch.admin.foitt.wallet.platform.database.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.database.domain.model.OpenDatabaseError
import ch.admin.foitt.wallet.platform.database.domain.repository.DatabaseRepository
import ch.admin.foitt.wallet.platform.database.domain.usecase.CheckDatabasePassphrase
import com.github.michaelbull.result.Result
import javax.inject.Inject

class CheckDatabasePassphraseImpl @Inject constructor(
    private val databaseRepository: DatabaseRepository,
) : CheckDatabasePassphrase {
    override suspend fun invoke(passphrase: ByteArray): Result<Unit, OpenDatabaseError> =
        databaseRepository.checkIfCorrectPassphrase(passphrase)
}

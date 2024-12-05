package ch.admin.foitt.wallet.platform.database.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.database.domain.model.ChangeDatabasePassphraseError
import ch.admin.foitt.wallet.platform.database.domain.repository.DatabaseRepository
import ch.admin.foitt.wallet.platform.database.domain.usecase.ChangeDatabasePassphrase
import ch.admin.foitt.wallet.platform.di.IoDispatcher
import com.github.michaelbull.result.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ChangeDatabasePassphraseImpl @Inject constructor(
    @IoDispatcher private val coroutineDispatcher: CoroutineDispatcher,
    private val databaseRepository: DatabaseRepository,
) : ChangeDatabasePassphrase {
    override suspend fun invoke(newPassphrase: ByteArray): Result<Unit, ChangeDatabasePassphraseError> = withContext(coroutineDispatcher) {
        databaseRepository.changePassphrase(newPassphrase)
    }
}

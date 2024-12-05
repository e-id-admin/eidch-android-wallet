package ch.admin.foitt.wallet.platform.ssi.data.repository

import ch.admin.foitt.wallet.platform.database.data.dao.CredentialDao
import ch.admin.foitt.wallet.platform.database.data.dao.DaoProvider
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import ch.admin.foitt.wallet.platform.di.IoDispatcher
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialRepo
import ch.admin.foitt.wallet.platform.utils.suspendUntilNonNull
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.Instant
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class CredentialRepoImpl @Inject constructor(
    daoProvider: DaoProvider,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : CredentialRepo {

    override suspend fun getAll(): Result<List<Credential>, CredentialRepositoryError> = runSuspendCatching {
        withContext(ioDispatcher) {
            dao().getAll()
        }
    }.mapError { throwable ->
        Timber.e(throwable)
        SsiError.Unexpected(throwable)
    }

    override suspend fun getAllIds(): Result<List<Long>, CredentialRepositoryError> = runSuspendCatching {
        withContext(ioDispatcher) {
            dao().getAllIds()
        }
    }.mapError { throwable ->
        Timber.e(throwable)
        SsiError.Unexpected(throwable)
    }

    override suspend fun getById(id: Long): Result<Credential?, CredentialRepositoryError> = runSuspendCatching {
        withContext(ioDispatcher) {
            dao().getById(id)
        }
    }.mapError { throwable ->
        Timber.e(throwable)
        SsiError.Unexpected(throwable)
    }

    override suspend fun deleteById(id: Long): Result<Unit, CredentialRepositoryError> = runSuspendCatching {
        withContext(ioDispatcher) {
            dao().deleteById(id)
        }
    }.mapError { throwable ->
        Timber.e(throwable)
        SsiError.Unexpected(throwable)
    }

    override suspend fun updateStatusByCredentialId(credentialId: Long, status: CredentialStatus): Result<Int, CredentialRepositoryError> =
        runSuspendCatching {
            withContext(ioDispatcher) {
                dao().updateStatusByCredentialId(
                    id = credentialId,
                    status = status,
                    updatedAt = Instant.now().epochSecond,
                )
            }
        }.mapError { throwable ->
            Timber.e(throwable)
            SsiError.Unexpected(throwable)
        }

    private suspend fun dao(): CredentialDao = suspendUntilNonNull { daoFlow.value }
    private val daoFlow = daoProvider.credentialDaoFlow
}

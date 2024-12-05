package ch.admin.foitt.wallet.platform.ssi.data.repository

import ch.admin.foitt.wallet.platform.database.data.dao.CredentialClaimDao
import ch.admin.foitt.wallet.platform.database.data.dao.DaoProvider
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim
import ch.admin.foitt.wallet.platform.di.IoDispatcher
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialClaimRepo
import ch.admin.foitt.wallet.platform.utils.suspendUntilNonNull
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class CredentialClaimRepoImpl @Inject constructor(
    daoProvider: DaoProvider,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : CredentialClaimRepo {
    override suspend fun insert(credentialClaim: CredentialClaim): Result<Long, CredentialClaimRepositoryError> = runSuspendCatching {
        withContext(ioDispatcher) {
            dao().insert(credentialClaim = credentialClaim)
        }
    }.mapError { throwable ->
        Timber.e(throwable)
        SsiError.Unexpected(throwable)
    }

    override suspend fun getByCredentialId(credentialId: Long): Result<List<CredentialClaim>, CredentialClaimRepositoryError> =
        runSuspendCatching {
            withContext(ioDispatcher) {
                dao().getByCredentialId(credentialId)
            }
        }.mapError { throwable ->
            Timber.e(throwable)
            SsiError.Unexpected(throwable)
        }

    private suspend fun dao(): CredentialClaimDao = suspendUntilNonNull { daoFlow.value }
    private val daoFlow = daoProvider.credentialClaimDaoFlow
}

package ch.admin.foitt.wallet.platform.ssi.data.repository

import ch.admin.foitt.wallet.platform.database.data.dao.CredentialClaimDisplayDao
import ch.admin.foitt.wallet.platform.database.data.dao.DaoProvider
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimDisplay
import ch.admin.foitt.wallet.platform.di.IoDispatcher
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimDisplayRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialClaimDisplayRepo
import ch.admin.foitt.wallet.platform.utils.suspendUntilNonNull
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class CredentialClaimDisplayRepoImpl @Inject constructor(
    daoProvider: DaoProvider,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : CredentialClaimDisplayRepo {
    override suspend fun insertAll(
        credentialClaimDisplays: Collection<CredentialClaimDisplay>
    ): Result<Unit, CredentialClaimDisplayRepositoryError> = runSuspendCatching {
        withContext(ioDispatcher) {
            dao().insertAll(credentialClaimDisplays)
        }
    }.mapError { throwable ->
        Timber.e(throwable)
        SsiError.Unexpected(throwable)
    }

    override suspend fun getByClaimId(
        claimId: Long,
    ): Result<List<CredentialClaimDisplay>, CredentialClaimDisplayRepositoryError> =
        runSuspendCatching {
            withContext(ioDispatcher) {
                dao().getByClaimId(claimId)
            }
        }.mapError { throwable ->
            Timber.e(throwable)
            SsiError.Unexpected(throwable)
        }

    private suspend fun dao(): CredentialClaimDisplayDao = suspendUntilNonNull { daoFlow.value }
    private val daoFlow = daoProvider.credentialClaimDisplayDaoFlow
}

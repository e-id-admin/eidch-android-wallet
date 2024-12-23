package ch.admin.foitt.wallet.platform.ssi.data.repository

import ch.admin.foitt.wallet.platform.database.data.dao.CredentialIssuerDisplayDao
import ch.admin.foitt.wallet.platform.database.data.dao.DaoProvider
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialIssuerDisplay
import ch.admin.foitt.wallet.platform.di.IoDispatcher
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialIssuerDisplayRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.toCredentialIssuerDisplayRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialIssuerDisplayRepo
import ch.admin.foitt.wallet.platform.utils.catchAndMap
import ch.admin.foitt.wallet.platform.utils.suspendUntilNonNull
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.withContext
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class CredentialIssuerDisplayRepoImpl @Inject constructor(
    daoProvider: DaoProvider,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : CredentialIssuerDisplayRepo {
    override fun getIssuerDisplaysFlow(
        credentialId: Long
    ): Flow<Result<List<CredentialIssuerDisplay>, CredentialIssuerDisplayRepositoryError>> =
        daoFlow.flatMapLatest { dao ->
            dao?.getCredentialIssuerDisplaysByIdFlow(credentialId)
                ?.catchAndMap(Throwable::toCredentialIssuerDisplayRepositoryError) ?: emptyFlow()
        }

    override suspend fun getIssuerDisplays(
        credentialId: Long
    ): Result<List<CredentialIssuerDisplay>, CredentialIssuerDisplayRepositoryError> =
        runSuspendCatching {
            withContext(ioDispatcher) {
                dao().getCredentialIssuerDisplaysById(credentialId)
            }
        }.mapError(
            Throwable::toCredentialIssuerDisplayRepositoryError
        )

    private suspend fun dao(): CredentialIssuerDisplayDao = suspendUntilNonNull { daoFlow.value }
    private val daoFlow = daoProvider.credentialIssuerDisplayDaoFlow
}

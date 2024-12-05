package ch.admin.foitt.wallet.feature.credentialDetail.data.repository

import ch.admin.foitt.wallet.feature.credentialDetail.domain.repository.CredentialDetailRepository
import ch.admin.foitt.wallet.platform.database.data.dao.DaoProvider
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialWithDetails
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.toCredentialRepositoryError
import ch.admin.foitt.wallet.platform.utils.catchAndMap
import com.github.michaelbull.result.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class CredentialDetailRepositoryImpl @Inject constructor(
    daoProvider: DaoProvider,
) : CredentialDetailRepository {
    override fun getCredentialDetailByIdFlow(id: Long): Flow<Result<CredentialWithDetails?, CredentialRepositoryError>> =
        credentialWithDetailsDaoFlow.flatMapLatest { dao ->
            dao?.getCredentialWithDetailsFlowById(id)
                ?.catchAndMap(Throwable::toCredentialRepositoryError) ?: emptyFlow()
        }

    private val credentialWithDetailsDaoFlow = daoProvider.credentialWithDetailsDaoFlow
}

package ch.admin.foitt.wallet.platform.eIdApplicationProcess.data.repository

import ch.admin.foitt.wallet.platform.database.data.dao.DaoProvider
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestCaseWithState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestCaseWithStateRepositoryError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.toEIdRequestCaseWithStateRepositoryError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestCaseWithStateRepository
import ch.admin.foitt.wallet.platform.utils.catchAndMap
import com.github.michaelbull.result.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class EIdRequestCaseWithStateRepositoryImpl @Inject constructor(
    daoProvider: DaoProvider,
) : EIdRequestCaseWithStateRepository {
    override fun getEIdRequestCasesWithStatesFlow(): Flow<Result<List<EIdRequestCaseWithState>, EIdRequestCaseWithStateRepositoryError>> =
        eIdRequestCaseWithStateDaoFlow.flatMapLatest { dao ->
            dao?.getEIdCasesWithStatesFlow()
                ?.catchAndMap { throwable ->
                    throwable.toEIdRequestCaseWithStateRepositoryError("getEIdRequestCasesWithStatesFlow error")
                } ?: emptyFlow()
        }

    private val eIdRequestCaseWithStateDaoFlow = daoProvider.eIdRequestCaseWithStateDaoFlow
}

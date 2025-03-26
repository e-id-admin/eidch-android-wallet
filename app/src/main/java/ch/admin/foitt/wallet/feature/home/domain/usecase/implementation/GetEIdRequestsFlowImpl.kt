package ch.admin.foitt.wallet.feature.home.domain.usecase.implementation

import ch.admin.foitt.wallet.feature.home.domain.model.GetEIdRequestsFlowError
import ch.admin.foitt.wallet.feature.home.domain.model.toGetEIdRequestsFlowError
import ch.admin.foitt.wallet.feature.home.domain.usecase.GetEIdRequestsFlow
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestCaseWithState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestCaseWithStateRepositoryError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestCaseWithStateRepository
import ch.admin.foitt.wallet.platform.utils.mapError
import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetEIdRequestsFlowImpl @Inject constructor(
    private val eIdRequestCaseWithStateRepository: EIdRequestCaseWithStateRepository,
) : GetEIdRequestsFlow {
    override fun invoke(): Flow<Result<List<EIdRequestCaseWithState>, GetEIdRequestsFlowError>> =
        eIdRequestCaseWithStateRepository.getEIdRequestCasesWithStatesFlow()
            .mapError(EIdRequestCaseWithStateRepositoryError::toGetEIdRequestsFlowError)
}

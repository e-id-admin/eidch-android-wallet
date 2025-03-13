package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestCaseWithState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestCaseWithStateRepositoryError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.GetEIdRequestsError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.toGetEIdRequestsError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestCaseWithStateRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.GetEIdRequestsFlow
import ch.admin.foitt.wallet.platform.utils.mapError
import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetEIdRequestsFlowImpl @Inject constructor(
    private val eIdRequestCaseWithStateRepository: EIdRequestCaseWithStateRepository,
) : GetEIdRequestsFlow {
    override fun invoke(): Flow<Result<List<EIdRequestCaseWithState>, GetEIdRequestsError>> =
        eIdRequestCaseWithStateRepository.getEIdRequestCasesWithStatesFlow()
            .mapError(EIdRequestCaseWithStateRepositoryError::toGetEIdRequestsError)
}

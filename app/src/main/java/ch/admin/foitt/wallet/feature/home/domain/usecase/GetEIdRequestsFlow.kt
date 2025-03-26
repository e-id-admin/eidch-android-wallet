package ch.admin.foitt.wallet.feature.home.domain.usecase

import ch.admin.foitt.wallet.feature.home.domain.model.GetEIdRequestsFlowError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestCaseWithState
import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.Flow

interface GetEIdRequestsFlow {
    operator fun invoke(): Flow<Result<List<EIdRequestCaseWithState>, GetEIdRequestsFlowError>>
}

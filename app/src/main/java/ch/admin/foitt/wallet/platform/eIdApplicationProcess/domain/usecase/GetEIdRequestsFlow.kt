package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestCaseWithState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.GetEIdRequestsError
import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.Flow

interface GetEIdRequestsFlow {
    operator fun invoke(): Flow<Result<List<EIdRequestCaseWithState>, GetEIdRequestsError>>
}

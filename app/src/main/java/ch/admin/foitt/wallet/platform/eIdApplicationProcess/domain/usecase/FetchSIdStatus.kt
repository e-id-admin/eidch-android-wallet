package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.StateRequestError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.StateResponse
import com.github.michaelbull.result.Result

interface FetchSIdStatus {
    suspend operator fun invoke(caseId: String): Result<StateResponse, StateRequestError>
}

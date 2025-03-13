package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.ApplyRequest
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.ApplyRequestError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.CaseResponse
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.StateRequestError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.StateResponse
import com.github.michaelbull.result.Result

interface SIdRepository {
    suspend fun fetchSIdCase(applyRequest: ApplyRequest): Result<CaseResponse, ApplyRequestError>
    suspend fun fetchSIdState(caseId: String): Result<StateResponse, StateRequestError>
}

package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.StateResponse

interface UpdateSIdStatusByCaseId {
    suspend operator fun invoke(caseId: String, stateResponse: StateResponse)
}

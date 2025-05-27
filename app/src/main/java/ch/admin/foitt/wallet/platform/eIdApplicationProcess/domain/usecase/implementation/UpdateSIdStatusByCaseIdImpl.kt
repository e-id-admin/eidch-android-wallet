package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestStateRepositoryError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.StateResponse
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.toUpdateEIdRequestStateError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestStateRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.UpdateSIdStatusByCaseId
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class UpdateSIdStatusByCaseIdImpl @Inject constructor(
    private val eIdRequestStateRepository: EIdRequestStateRepository,
) : UpdateSIdStatusByCaseId {
    override suspend fun invoke(caseId: String, stateResponse: StateResponse) {
        eIdRequestStateRepository.updateStatusByCaseId(
            caseId = caseId,
            stateResponse = stateResponse
        ).mapError(EIdRequestStateRepositoryError::toUpdateEIdRequestStateError)
    }
}

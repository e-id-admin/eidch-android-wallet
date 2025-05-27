package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestStateRepositoryError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.StateResponse
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.toUpdateEIdRequestStateError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestStateRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.FetchSIdStatus
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.UpdateAllSIdStatuses
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.onFailure
import com.github.michaelbull.result.onSuccess
import timber.log.Timber
import javax.inject.Inject

class UpdateAllSIdStatusesImpl @Inject constructor(
    private val eIdRequestStateRepository: EIdRequestStateRepository,
    private val fetchSIdStatus: FetchSIdStatus
) : UpdateAllSIdStatuses {
    override suspend fun invoke() {
        eIdRequestStateRepository.getAllCaseIds()
            .onSuccess { eIdRequestCaseIds ->
                for (caseId in eIdRequestCaseIds) {
                    fetchSIdState(caseId)
                }
            }
            .onFailure { error ->
                Timber.d("Could not get Case Ids for status update")
            } // silently fail
    }

    private suspend fun fetchSIdState(caseId: String) {
        fetchSIdStatus(caseId)
            .onSuccess { stateResponse ->
                updateState(caseId, stateResponse)
            }
            .onFailure { error ->
                Timber.d(message = "Could not get case Id &s for status update, caseId $caseId")
            } // silently fail
    }

    private suspend fun updateState(caseId: String, stateResponse: StateResponse) {
        eIdRequestStateRepository.updateStatusByCaseId(
            caseId = caseId,
            stateResponse = stateResponse
        ).mapError(EIdRequestStateRepositoryError::toUpdateEIdRequestStateError)
    }
}

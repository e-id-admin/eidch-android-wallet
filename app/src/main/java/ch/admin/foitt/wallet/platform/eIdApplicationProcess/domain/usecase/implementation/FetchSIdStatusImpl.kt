package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.StateRequestError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.StateResponse
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.SIdRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.FetchSIdStatus
import com.github.michaelbull.result.Result
import javax.inject.Inject

class FetchSIdStatusImpl @Inject constructor(
    private val sIdRepository: SIdRepository
) : FetchSIdStatus {
    override suspend fun invoke(caseId: String): Result<StateResponse, StateRequestError> {
        return sIdRepository.fetchSIdState(caseId)
    }
}

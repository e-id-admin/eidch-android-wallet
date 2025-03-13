package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.ApplyRequest
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.ApplyRequestError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.CaseResponse
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.SIdRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.FetchSIdCase
import com.github.michaelbull.result.Result
import javax.inject.Inject

class FetchSIdCaseImpl @Inject constructor(
    private val sIdRepository: SIdRepository
) : FetchSIdCase {
    override suspend fun invoke(applyRequest: ApplyRequest): Result<CaseResponse, ApplyRequestError> {
        return sIdRepository.fetchSIdCase(applyRequest)
    }
}

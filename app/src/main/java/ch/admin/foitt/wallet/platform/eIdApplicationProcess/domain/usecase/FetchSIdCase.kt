package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.ApplyRequest
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.ApplyRequestError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.CaseResponse
import com.github.michaelbull.result.Result

interface FetchSIdCase {
    suspend operator fun invoke(applyRequest: ApplyRequest): Result<CaseResponse, ApplyRequestError>
}

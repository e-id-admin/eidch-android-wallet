package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestCaseRepositoryError
import com.github.michaelbull.result.Result

interface AddWalletPairingId {
    suspend operator fun invoke(caseId: String, pairingId: String): Result<Unit, EIdRequestCaseRepositoryError>
}

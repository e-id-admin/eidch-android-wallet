package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestCaseRepositoryError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestCaseRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.AddWalletPairingId
import com.github.michaelbull.result.Result
import javax.inject.Inject

class AddWalletPairingIdImpl @Inject constructor(
    private val repository: EIdRequestCaseRepository,
) : AddWalletPairingId {
    override suspend fun invoke(caseId: String, pairingId: String): Result<Unit, EIdRequestCaseRepositoryError> =
        repository.addPairingId(caseId, pairingId)
}

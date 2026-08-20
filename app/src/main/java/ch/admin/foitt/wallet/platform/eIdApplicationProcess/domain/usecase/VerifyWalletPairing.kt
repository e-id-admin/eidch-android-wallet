package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.StateResponse
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.VerifyWalletPairingError
import com.github.michaelbull.result.Result

interface VerifyWalletPairing {
    suspend operator fun invoke(caseId: String, remoteState: StateResponse): Result<Unit, VerifyWalletPairingError>
}

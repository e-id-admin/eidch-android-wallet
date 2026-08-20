package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestQueueState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.StateResponse
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.VerifyWalletPairingError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.toVerifyWalletPairingError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestCaseRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.VerifyWalletPairing
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import timber.log.Timber
import javax.inject.Inject

class VerifyWalletPairingImpl @Inject constructor(
    private val repository: EIdRequestCaseRepository,
) : VerifyWalletPairing {
    override suspend fun invoke(
        caseId: String,
        remoteState: StateResponse
    ): Result<Unit, VerifyWalletPairingError> = coroutineBinding {
        if (remoteState.state != EIdRequestQueueState.IN_AUTO_VERIFICATION) {
            Timber.e("VerifyWalletPairing: State is not IN_AUTO_VERIFICATION (state: ${remoteState.state})")
            Err(EIdRequestError.Unexpected(IllegalStateException("Verification called in wrong state: ${remoteState.state}"))).bind<Unit>()
        }

        val localPairingIds = repository.getPairingIds(caseId)
            .mapError { it.toVerifyWalletPairingError() }.bind()

        val remotePairingIds = remoteState.targetWallets?.pairedWallets?.map { it.walletPairingId } ?: emptyList()

        // It is OK if some of the locally attempted walletPairingIds do not show up in the SID list.
        // If there is any entry in the list for which the wallet does NOT recognise the walletPairingId,
        // then the wallet must immediately abort the e-ID request.
        val unrecognizedRemoteIds = remotePairingIds.filterNot { it in localPairingIds }

        if (unrecognizedRemoteIds.isNotEmpty()) {
            Timber.e(
                "Unauthorized pairing detected for case $caseId. " +
                    "Unrecognized Remote IDs: $unrecognizedRemoteIds, Local IDs: $localPairingIds"
            )
            Err(EIdRequestError.UnauthorizedPairing).bind<Unit>()
        }

        Ok(Unit).bind()
    }
}

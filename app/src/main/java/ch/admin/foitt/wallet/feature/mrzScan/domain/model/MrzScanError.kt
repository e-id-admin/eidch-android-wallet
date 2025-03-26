package ch.admin.foitt.wallet.feature.mrzScan.domain.model

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestCaseRepositoryError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestStateRepositoryError

interface MrzScanError {
    data class Unexpected(val cause: Throwable?) :
        SaveEIdRequestCaseError,
        SaveEIdRequestStateError
}

sealed interface SaveEIdRequestCaseError
sealed interface SaveEIdRequestStateError

internal fun EIdRequestCaseRepositoryError.toSaveEIdRequestCaseError(): SaveEIdRequestCaseError = when (this) {
    is EIdRequestError.Unexpected -> MrzScanError.Unexpected(cause)
}

internal fun EIdRequestStateRepositoryError.toSaveEIdRequestStateError(): SaveEIdRequestStateError = when (this) {
    is EIdRequestError.Unexpected -> MrzScanError.Unexpected(cause)
}

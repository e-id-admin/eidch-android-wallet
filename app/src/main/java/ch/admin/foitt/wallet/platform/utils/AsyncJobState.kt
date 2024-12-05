package ch.admin.foitt.wallet.platform.utils

import kotlinx.coroutines.CancellationException

sealed interface AsyncJobState {
    object Initial : AsyncJobState
    object InProgress : AsyncJobState
    object Finished : AsyncJobState
    data class Error(val exception: Throwable) : AsyncJobState
    data class Cancelled(val cancellationException: CancellationException) : AsyncJobState
}

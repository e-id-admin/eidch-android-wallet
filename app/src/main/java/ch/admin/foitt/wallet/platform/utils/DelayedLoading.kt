package ch.admin.foitt.wallet.platform.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Runs a given block and updates the [MutableStateFlow] after the specified delay to show a loader.
 */
fun CoroutineScope.launchWithDelayedLoading(
    isLoadingFlow: MutableStateFlow<Boolean>,
    delay: Long = 500,
    block: suspend CoroutineScope.() -> Unit
): Job {
    val loadingShowJob = launch {
        delay(delay)
        isLoadingFlow.update { true }
    }
    val blockJob = launch {
        block()
    }
    blockJob.invokeOnCompletion {
        loadingShowJob.cancel()
        isLoadingFlow.update { false }
    }
    return blockJob
}

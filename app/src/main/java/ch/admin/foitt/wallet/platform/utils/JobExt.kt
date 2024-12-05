package ch.admin.foitt.wallet.platform.utils

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

/**
 * Callback method triggered on job completion. It assumes the job is already active.
 *
 * Update the [MutableStateFlow] upon job completion.
 */
fun Job.trackCompletion(isRunningFlow: MutableStateFlow<Boolean>) {
    isRunningFlow.update { true }
    this.invokeOnCompletion {
        isRunningFlow.update { false }
    }
}

package ch.admin.foitt.wallet.platform.utils

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import timber.log.Timber
import java.time.Instant
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal suspend fun <T> suspendUntilNonNull(
    delayMillis: Long = 25,
    timeoutMillis: Long = 3000,
    block: () -> T?,
) = coroutineScope {
    suspendCancellableCoroutine { continuation ->
        val waitingJob = launch {
            val timeLimit = nowMillis() + timeoutMillis
            var stopLoop = false
            do {
                block()?.let {
                    continuation.resume(it)
                    stopLoop = true
                } ?: delay(delayMillis)

                if (nowMillis() > timeLimit) {
                    val exception = Exception("suspendUntilNonNull: Timeout while waiting on value")
                    Timber.e(exception)
                    continuation.resumeWithException(exception)
                    stopLoop = true
                }
            } while (!stopLoop)
        }
        continuation.invokeOnCancellation {
            waitingJob.cancel()
        }
    }
}

private fun nowMillis() = Instant.now().toEpochMilli()

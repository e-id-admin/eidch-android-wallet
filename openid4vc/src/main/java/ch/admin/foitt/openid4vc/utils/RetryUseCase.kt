package ch.admin.foitt.openid4vc.utils

import com.github.michaelbull.result.Result

/**
 * Retry method for suspendable usecases that return a result
 */
suspend fun <V, E> retryUseCase(
    attempts: Int = 3,
    useCase: suspend () -> Result<V, E>,
): Result<V, E> {
    var result = useCase()
    if (result.isErr) {
        repeat(attempts - 1) {
            result = useCase()
            if (result.isOk) return result
        }
    }
    return result
}

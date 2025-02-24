package ch.admin.foitt.openid4vc.domain.model

import ch.admin.eid.didresolver.didresolver.DidResolveException
import timber.log.Timber

sealed interface ResolveDidError {
    data object NetworkError : ResolveDidError
    data object ValidationFailure : ResolveDidError
    data class Unexpected(val cause: Throwable) : ResolveDidError
}

internal fun Throwable.toResolveDidError(): ResolveDidError {
    Timber.e(t = this, message = "Did resolver error")
    return when (this) {
        is DidResolveException -> ResolveDidError.ValidationFailure
        else -> ResolveDidError.Unexpected(this)
    }
}

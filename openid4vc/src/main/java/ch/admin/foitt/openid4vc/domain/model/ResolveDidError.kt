package ch.admin.foitt.openid4vc.domain.model

import ch.admin.eid.didresolver.didresolver.DidResolveException
import ch.admin.eid.didresolver.didresolver.InternalException

sealed interface ResolveDidError {
    data class NetworkError(val throwable: Throwable?) : ResolveDidError
    data class DidResolveError(val throwable: Throwable?) : ResolveDidError
    data class Unexpected(val throwable: Throwable?) : ResolveDidError
}

internal fun Throwable.toResolveDidError(): ResolveDidError {
    return when (this) {
        is InternalException,
        is DidResolveException -> ResolveDidError.DidResolveError(this)
        else -> ResolveDidError.Unexpected(this)
    }
}

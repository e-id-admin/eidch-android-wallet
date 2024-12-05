package ch.admin.foitt.openid4vc.domain.model.vcSdJwt

import ch.admin.foitt.openid4vc.domain.model.ResolveDidError

interface VcSdJwtError {
    data object NetworkError : VerifyJwtError
    data object InvalidJwt : VerifyJwtError, VerifyJwtTimestampsError
    data class Unexpected(val cause: Throwable?) :
        VerifyJwtError,
        VerifyJwtTimestampsError
}

sealed interface VerifyJwtError
internal sealed interface VerifyJwtTimestampsError

internal fun ResolveDidError.toVerifyJwtError(): VerifyJwtError {
    return when (this) {
        is ResolveDidError.DidResolveError -> VcSdJwtError.Unexpected(throwable)
        is ResolveDidError.NetworkError -> VcSdJwtError.NetworkError
        is ResolveDidError.Unexpected -> VcSdJwtError.Unexpected(throwable)
    }
}

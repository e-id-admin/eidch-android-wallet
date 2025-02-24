package ch.admin.foitt.openid4vc.domain.model.vcSdJwt

import ch.admin.foitt.openid4vc.domain.model.ResolveDidError

interface VcSdJwtError {
    data object IssuerValidationFailed : VerifyJwtError
    data object NetworkError : VerifyJwtError
    data object InvalidJwt : VerifyJwtError
    data class Unexpected(val cause: Throwable?) : VerifyJwtError
}

sealed interface VerifyJwtError

internal fun ResolveDidError.toVerifyJwtError(): VerifyJwtError = when (this) {
    is ResolveDidError.ValidationFailure -> VcSdJwtError.IssuerValidationFailed
    is ResolveDidError.NetworkError -> VcSdJwtError.NetworkError
    is ResolveDidError.Unexpected -> VcSdJwtError.Unexpected(cause)
}

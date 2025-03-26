package ch.admin.foitt.openid4vc.domain.model.vcSdJwt

import ch.admin.foitt.openid4vc.domain.model.ResolveDidError
import io.ktor.utils.io.errors.IOException

interface VcSdJwtError {
    data object IssuerValidationFailed : VerifyJwtError
    data object InvalidJwt : VerifyJwtError
    data object DidDocumentDeactivated : VerifyJwtError
    data object NetworkError :
        VerifyJwtError,
        TypeMetadataRepositoryError
    data class Unexpected(val cause: Throwable?) :
        VerifyJwtError,
        TypeMetadataRepositoryError
}

sealed interface VerifyJwtError
sealed interface TypeMetadataRepositoryError

internal fun ResolveDidError.toVerifyJwtError(): VerifyJwtError = when (this) {
    is ResolveDidError.ValidationFailure -> VcSdJwtError.IssuerValidationFailed
    is ResolveDidError.NetworkError -> VcSdJwtError.NetworkError
    is ResolveDidError.Unexpected -> VcSdJwtError.Unexpected(cause)
}

internal fun Throwable.toTypeMetadataRepositoryError(): TypeMetadataRepositoryError = when (this) {
    is IOException -> VcSdJwtError.NetworkError
    else -> VcSdJwtError.Unexpected(this)
}

package ch.admin.foitt.openid4vc.domain.model.vcSdJwt

import ch.admin.foitt.openid4vc.domain.model.ResolveDidError
import io.ktor.utils.io.errors.IOException
import timber.log.Timber

interface VcSdJwtError {
    data object IssuerValidationFailed : VerifyJwtError
    data object InvalidJwt : VerifyJwtError
    data object DidDocumentDeactivated : VerifyJwtError
    data object NetworkError :
        VerifyJwtError,
        TypeMetadataRepositoryError,
        VcSchemaRepositoryError
    data class Unexpected(val cause: Throwable?) :
        VerifyJwtError,
        TypeMetadataRepositoryError,
        VcSchemaRepositoryError
}

sealed interface VerifyJwtError
sealed interface TypeMetadataRepositoryError
sealed interface VcSchemaRepositoryError

internal fun ResolveDidError.toVerifyJwtError(): VerifyJwtError = when (this) {
    is ResolveDidError.ValidationFailure -> VcSdJwtError.IssuerValidationFailed
    is ResolveDidError.NetworkError -> VcSdJwtError.NetworkError
    is ResolveDidError.Unexpected -> VcSdJwtError.Unexpected(cause)
}

internal fun Throwable.toTypeMetadataRepositoryError(message: String): TypeMetadataRepositoryError {
    Timber.e(t = this, message = message)
    return when (this) {
        is IOException -> VcSdJwtError.NetworkError
        else -> VcSdJwtError.Unexpected(this)
    }
}

internal fun Throwable.toVcSchemaRepositoryError(message: String): VcSchemaRepositoryError {
    Timber.e(t = this, message = message)
    return when (this) {
        is IOException -> VcSdJwtError.NetworkError
        else -> VcSdJwtError.Unexpected(this)
    }
}

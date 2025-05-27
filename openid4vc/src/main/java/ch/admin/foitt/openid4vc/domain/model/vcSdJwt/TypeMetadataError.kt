package ch.admin.foitt.openid4vc.domain.model.vcSdJwt

import ch.admin.foitt.openid4vc.utils.JsonError
import ch.admin.foitt.openid4vc.utils.JsonParsingError
import ch.admin.foitt.openid4vc.utils.SafeGetError
import ch.admin.foitt.openid4vc.utils.SafeGetUrlError
import ch.admin.foitt.sriValidator.domain.model.SRIError
import timber.log.Timber
import java.io.IOException

interface TypeMetadataError {
    data object InvalidData : FetchTypeMetadataError
    data object NetworkError :
        TypeMetadataRepositoryError,
        FetchTypeMetadataError
    data class Unexpected(val cause: Throwable?) :
        TypeMetadataRepositoryError,
        FetchTypeMetadataError
}

sealed interface TypeMetadataRepositoryError
sealed interface FetchTypeMetadataError

internal fun Throwable.toTypeMetadataRepositoryError(message: String): TypeMetadataRepositoryError {
    Timber.e(t = this, message = message)
    return when (this) {
        is IOException -> TypeMetadataError.NetworkError
        else -> TypeMetadataError.Unexpected(this)
    }
}

internal fun SafeGetUrlError.toFetchTypeMetadataByFormatError(): FetchTypeMetadataError = when (this) {
    SafeGetError.Unexpected -> TypeMetadataError.Unexpected(null)
}

internal fun TypeMetadataRepositoryError.toFetchTypeMetadataByFormatError(): FetchTypeMetadataError = when (this) {
    is TypeMetadataError.NetworkError -> this
    is TypeMetadataError.Unexpected -> this
}

internal fun JsonParsingError.toFetchTypeMetadataByFormatError(): FetchTypeMetadataError = when (this) {
    is JsonError.Unexpected -> TypeMetadataError.Unexpected(throwable)
}

internal fun SRIError.toFetchTypeMetadataByFormatError(): FetchTypeMetadataError = when (this) {
    is SRIError.ValidationFailed,
    is SRIError.MalformedIntegrity,
    is SRIError.UnsupportedAlgorithm -> TypeMetadataError.InvalidData
}

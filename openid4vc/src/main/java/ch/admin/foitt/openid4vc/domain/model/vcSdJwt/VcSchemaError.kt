package ch.admin.foitt.openid4vc.domain.model.vcSdJwt

import ch.admin.foitt.sriValidator.domain.model.SRIError
import timber.log.Timber
import java.io.IOException

interface VcSchemaError {
    data object InvalidVcSchema : FetchVcSchemaError
    data object NetworkError :
        VcSchemaRepositoryError,
        FetchVcSchemaError
    data class Unexpected(val cause: Throwable?) :
        VcSchemaRepositoryError,
        FetchVcSchemaError
}

sealed interface FetchVcSchemaError
sealed interface VcSchemaRepositoryError

internal fun Throwable.toVcSchemaRepositoryError(message: String): VcSchemaRepositoryError {
    Timber.e(t = this, message = message)
    return when (this) {
        is IOException -> VcSchemaError.NetworkError
        else -> VcSchemaError.Unexpected(this)
    }
}

internal fun VcSchemaRepositoryError.toFetchVcSchemaError(): FetchVcSchemaError = when (this) {
    is VcSchemaError.NetworkError -> this
    is VcSchemaError.Unexpected -> this
}

internal fun SRIError.toFetchVcSchemaError(): FetchVcSchemaError = when (this) {
    is SRIError.ValidationFailed,
    is SRIError.MalformedIntegrity,
    is SRIError.UnsupportedAlgorithm -> VcSchemaError.InvalidVcSchema
}

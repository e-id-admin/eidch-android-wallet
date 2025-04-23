package ch.admin.foitt.wallet.platform.oca.domain.model

import ch.admin.foitt.jsonSchema.domain.model.JsonSchemaError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.TypeMetadataRepositoryError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSchemaRepositoryError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtError
import ch.admin.foitt.wallet.platform.utils.JsonError
import ch.admin.foitt.wallet.platform.utils.JsonParsingError
import timber.log.Timber
import java.io.IOException

sealed interface OcaError {
    data object InvalidOca : FetchOcaBundleByFormatError
    data object NetworkError :
        OcaRepositoryError,
        FetchOcaBundleByFormatError

    data class Unexpected(val cause: Throwable?) :
        OcaRepositoryError,
        FetchOcaBundleByFormatError
}

sealed interface OcaRepositoryError
sealed interface FetchOcaBundleByFormatError

fun Throwable.toOcaRepositoryError(message: String): OcaRepositoryError {
    Timber.e(t = this, message = message)
    return when (this) {
        is IOException -> OcaError.NetworkError
        else -> OcaError.Unexpected(this)
    }
}

fun Throwable.toFetchOcaBundleByFormatError(message: String): FetchOcaBundleByFormatError {
    Timber.e(t = this, message = message)
    return when (this) {
        is IOException -> OcaError.NetworkError
        else -> OcaError.Unexpected(this)
    }
}

fun OcaRepositoryError.toFetchOcaBundleByFormatError(): FetchOcaBundleByFormatError = when (this) {
    is OcaError.NetworkError -> this
    is OcaError.Unexpected -> this
}

fun TypeMetadataRepositoryError.toFetchOcaBundleByFormatError(): FetchOcaBundleByFormatError = when (this) {
    is VcSdJwtError.NetworkError -> OcaError.NetworkError
    is VcSdJwtError.Unexpected -> OcaError.Unexpected(cause)
}

internal fun JsonParsingError.toFetchOcaBundleByFormatError(): FetchOcaBundleByFormatError = when (this) {
    is JsonError.Unexpected -> OcaError.Unexpected(throwable)
}

internal fun VcSchemaRepositoryError.toFetchOcaBundleByFormatError(): FetchOcaBundleByFormatError = when (this) {
    is VcSdJwtError.NetworkError -> OcaError.NetworkError
    is VcSdJwtError.Unexpected -> OcaError.Unexpected(cause)
}

internal fun JsonSchemaError.toFetchOcaBundleByFormatError(): FetchOcaBundleByFormatError = when (this) {
    JsonSchemaError.Unexpected -> OcaError.InvalidOca
}

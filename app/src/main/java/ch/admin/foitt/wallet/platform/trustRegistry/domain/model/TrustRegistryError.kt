package ch.admin.foitt.wallet.platform.trustRegistry.domain.model

import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VerifyJwtError
import ch.admin.foitt.wallet.platform.utils.JsonError
import ch.admin.foitt.wallet.platform.utils.JsonParsingError
import timber.log.Timber

interface TrustRegistryError {
    data class Unexpected(val cause: Throwable?) :
        GetTrustUrlFromDidError,
        FetchAnyCredentialTrustStatementError,
        ValidateTrustStatementError,
        TrustStatementRepositoryError,
        FetchTrustStatementFromDidError
}

sealed interface TrustStatementRepositoryError
sealed interface GetTrustUrlFromDidError {
    data class NoTrustRegistryMapping(val message: String) : GetTrustUrlFromDidError
}
sealed interface FetchAnyCredentialTrustStatementError
sealed interface ValidateTrustStatementError
sealed interface FetchTrustStatementFromDidError

fun VerifyJwtError.toValidateTrustStatementError(): ValidateTrustStatementError = when (this) {
    VcSdJwtError.NetworkError,
    VcSdJwtError.InvalidJwt,
    VcSdJwtError.IssuerValidationFailed -> TrustRegistryError.Unexpected(null)
    is VcSdJwtError.Unexpected -> TrustRegistryError.Unexpected(cause)
}

fun Throwable.toGetTrustUrlFromDidError(message: String): GetTrustUrlFromDidError {
    Timber.e(message = message, t = this)
    return TrustRegistryError.Unexpected(this)
}

fun Throwable.toTrustStatementRepositoryError(): TrustStatementRepositoryError {
    Timber.e(this)
    return TrustRegistryError.Unexpected(this)
}

fun Throwable.toValidateTrustStatementError(): ValidateTrustStatementError {
    Timber.e(this)
    return TrustRegistryError.Unexpected(this)
}

fun Throwable.toFetchTrustStatementFromDidError(): FetchTrustStatementFromDidError {
    Timber.e(this)
    return TrustRegistryError.Unexpected(this)
}

fun GetTrustUrlFromDidError.toFetchTrustStatementFromDidError(): FetchTrustStatementFromDidError = when (this) {
    is TrustRegistryError.Unexpected -> this
    is GetTrustUrlFromDidError.NoTrustRegistryMapping -> {
        Timber.w(message = this.message)
        TrustRegistryError.Unexpected(null)
    }
}

fun TrustStatementRepositoryError.toFetchTrustStatementFromDidError(): FetchTrustStatementFromDidError = when (this) {
    is TrustRegistryError.Unexpected -> this
}

fun JsonParsingError.toValidateTrustStatementError(): ValidateTrustStatementError = when (this) {
    is JsonError.Unexpected -> TrustRegistryError.Unexpected(throwable)
}

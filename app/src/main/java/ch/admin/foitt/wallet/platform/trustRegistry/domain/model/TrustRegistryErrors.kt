package ch.admin.foitt.wallet.platform.trustRegistry.domain.model

import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VerifyJwtError
import timber.log.Timber

interface TrustRegistryErrors {
    data class Unexpected(val cause: Throwable?) :
        GetTrustUrlFromDidError,
        FetchAnyCredentialTrustStatementError,
        ValidateTrustStatementError,
        TrustStatementRepositoryError,
        FetchTrustStatementFromDidError
}

sealed interface TrustStatementRepositoryError
sealed interface GetTrustUrlFromDidError
sealed interface FetchAnyCredentialTrustStatementError
sealed interface ValidateTrustStatementError
sealed interface FetchTrustStatementFromDidError

fun VerifyJwtError.toValidateTrustStatementError(): ValidateTrustStatementError = when (this) {
    VcSdJwtError.NetworkError,
    VcSdJwtError.InvalidJwt -> TrustRegistryErrors.Unexpected(null)
    is VcSdJwtError.Unexpected -> TrustRegistryErrors.Unexpected(cause)
}

fun Throwable.toGetTrustUrlFromDidError(): GetTrustUrlFromDidError {
    Timber.e(this)
    return TrustRegistryErrors.Unexpected(this)
}

fun Throwable.toFetchAnyCredentialTrustStatementError(): FetchAnyCredentialTrustStatementError {
    Timber.e(this)
    return TrustRegistryErrors.Unexpected(this)
}

fun Throwable.toTrustStatementRepositoryError(): TrustStatementRepositoryError {
    Timber.e(this)
    return TrustRegistryErrors.Unexpected(this)
}

fun Throwable.toValidateTrustStatementError(): ValidateTrustStatementError {
    Timber.e(this)
    return TrustRegistryErrors.Unexpected(this)
}

fun Throwable.toFetchTrustStatementFromDidError(): FetchTrustStatementFromDidError {
    Timber.e(this)
    return TrustRegistryErrors.Unexpected(this)
}

fun FetchTrustStatementFromDidError.toFetchAnyCredentialTrustStatementError(): FetchAnyCredentialTrustStatementError = when (this) {
    is TrustRegistryErrors.Unexpected -> this
}

fun GetTrustUrlFromDidError.toFetchTrustStatementFromDidError(): FetchTrustStatementFromDidError = when (this) {
    is TrustRegistryErrors.Unexpected -> this
}

fun TrustStatementRepositoryError.toFetchTrustStatementFromDidError(): FetchTrustStatementFromDidError = when (this) {
    is TrustRegistryErrors.Unexpected -> this
}

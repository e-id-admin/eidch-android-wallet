package ch.admin.foitt.wallet.platform.credentialStatus.domain.model

import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VerifyJwtError
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.GetAnyCredentialError
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialStatusError.NetworkError
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialStatusError.Unexpected
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.utils.JsonError
import ch.admin.foitt.wallet.platform.utils.JsonParsingError
import timber.log.Timber

interface CredentialStatusError {
    data object NetworkError :
        UpdateCredentialStatusError,
        FetchCredentialStatusError,
        FetchStatusFromTokenStatusListError,
        ValidateTokenStatusStatusListError
    data class Unexpected(val cause: Throwable?) :
        UpdateCredentialStatusError,
        FetchCredentialStatusError,
        FetchStatusFromTokenStatusListError,
        ValidateTokenStatusStatusListError,
        ParseTokenStatusStatusListError
}

sealed interface UpdateCredentialStatusError
sealed interface FetchCredentialStatusError
sealed interface FetchStatusFromTokenStatusListError
sealed interface ValidateTokenStatusStatusListError
sealed interface ParseTokenStatusStatusListError

internal fun FetchCredentialStatusError.toUpdateCredentialStatusError(): UpdateCredentialStatusError = when (this) {
    is NetworkError -> this
    is Unexpected -> this
}

internal fun CredentialRepositoryError.toUpdateCredentialStatusError(): UpdateCredentialStatusError = when (this) {
    is SsiError.Unexpected -> Unexpected(cause)
}

internal fun GetAnyCredentialError.toUpdateCredentialStatusError(): UpdateCredentialStatusError = when (this) {
    is CredentialError.Unexpected -> Unexpected(cause)
}

internal fun FetchStatusFromTokenStatusListError.toFetchCredentialStatusError(): FetchCredentialStatusError = when (this) {
    is NetworkError -> this
    is Unexpected -> this
}

internal fun ParseTokenStatusStatusListError.toFetchStatusFromTokenStatusListError(): FetchStatusFromTokenStatusListError = when (this) {
    is Unexpected -> this
}

internal fun ValidateTokenStatusStatusListError.toFetchStatusFromTokenStatusListError(): FetchStatusFromTokenStatusListError = when (this) {
    is NetworkError -> this
    is Unexpected -> this
}

internal fun JsonParsingError.toValidateTokenStatusListError(): ValidateTokenStatusStatusListError = when (this) {
    is JsonError.Unexpected -> Unexpected(throwable)
}

fun VerifyJwtError.toValidateTokenStatusListError(): ValidateTokenStatusStatusListError = when (this) {
    VcSdJwtError.NetworkError -> NetworkError
    VcSdJwtError.InvalidJwt -> Unexpected(Exception("Validation failed"))
    is VcSdJwtError.Unexpected -> Unexpected(cause)
}

internal fun Throwable.toValidateTokenStatusStatusListError(): ValidateTokenStatusStatusListError {
    Timber.e(this)
    return Unexpected(this)
}

internal fun Throwable.toParseTokenStatusStatusListError(): ParseTokenStatusStatusListError {
    Timber.e(this)
    return Unexpected(this)
}

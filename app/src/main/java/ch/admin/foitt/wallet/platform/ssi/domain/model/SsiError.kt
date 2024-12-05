@file:Suppress("TooManyFunctions")

package ch.admin.foitt.wallet.platform.ssi.domain.model

import timber.log.Timber

interface SsiError {
    data class Unexpected(val cause: Throwable?) :
        CredentialClaimDisplayRepositoryError,
        CredentialClaimRepositoryError,
        CredentialDisplayRepositoryError,
        CredentialIssuerDisplayRepositoryError,
        CredentialIssuerRepositoryError,
        CredentialRepositoryError,
        GetAllCredentialsError,
        GetCredentialByIdError,
        DeleteCredentialError,
        GetCredentialClaimsError,
        GetCredentialClaimDisplayError,
        GetCredentialClaimDataError,
        MapToCredentialClaimDataError,
        CredentialOfferRepositoryError,
        GetCredentialIssuerDisplayFlowError
}

sealed interface CredentialClaimDisplayRepositoryError
sealed interface CredentialClaimRepositoryError
sealed interface CredentialDisplayRepositoryError
sealed interface CredentialIssuerDisplayRepositoryError
sealed interface CredentialIssuerRepositoryError
sealed interface CredentialRepositoryError
sealed interface GetAllCredentialsError
sealed interface GetCredentialByIdError
sealed interface DeleteCredentialError
sealed interface GetCredentialClaimsError
sealed interface GetCredentialClaimDisplayError
sealed interface GetCredentialClaimDataError
sealed interface MapToCredentialClaimDataError
sealed interface CredentialOfferRepositoryError
sealed interface GetCredentialIssuerDisplayFlowError

internal fun CredentialRepositoryError.toGetAllCredentialsError() = when (this) {
    is SsiError.Unexpected -> SsiError.Unexpected(cause)
}

internal fun CredentialRepositoryError.toDeleteCredentialError() = when (this) {
    is SsiError.Unexpected -> SsiError.Unexpected(cause)
}

internal fun CredentialClaimRepositoryError.toGetCredentialClaimsError() = when (this) {
    is SsiError.Unexpected -> SsiError.Unexpected(cause)
}

internal fun CredentialClaimDisplayRepositoryError.toGetCredentialClaimDisplayError() = when (this) {
    is SsiError.Unexpected -> SsiError.Unexpected(cause)
}

internal fun GetCredentialClaimDisplayError.toGetCredentialClaimDataError() = when (this) {
    is SsiError.Unexpected -> SsiError.Unexpected(cause)
}

internal fun MapToCredentialClaimDataError.toGetCredentialClaimDataError() = when (this) {
    is SsiError.Unexpected -> this
}

internal fun Throwable.toMapToCredentialClaimDataError(): MapToCredentialClaimDataError {
    Timber.e(this)
    return SsiError.Unexpected(this)
}

internal fun Throwable.toCredentialRepositoryError(): CredentialRepositoryError {
    Timber.e(this)
    return SsiError.Unexpected(this)
}

internal fun Throwable.toCredentialOfferRepositoryError(): CredentialOfferRepositoryError {
    Timber.e(this)
    return SsiError.Unexpected(this)
}

internal fun Throwable.toCredentialIssuerDisplayRepositoryError(): CredentialIssuerDisplayRepositoryError {
    Timber.e(this)
    return SsiError.Unexpected(this)
}

internal fun CredentialIssuerDisplayRepositoryError.toGetCredentialIssuerDisplayFlowError(): GetCredentialIssuerDisplayFlowError =
    when (this) {
        is SsiError.Unexpected -> this
    }

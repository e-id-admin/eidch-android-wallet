@file:Suppress("TooManyFunctions")

package ch.admin.foitt.wallet.platform.ssi.domain.model

import ch.admin.foitt.wallet.platform.credential.domain.model.AnyCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.MapToCredentialDisplayDataError
import timber.log.Timber

interface SsiError {
    data class Unexpected(val cause: Throwable?) :
        CredentialClaimDisplayRepositoryError,
        CredentialClaimRepositoryError,
        CredentialIssuerDisplayRepositoryError,
        CredentialRepositoryError,
        CredentialWithDisplaysRepositoryError,
        CredentialWithDisplaysAndClaimsRepositoryError,
        DeleteCredentialError,
        MapToCredentialClaimDataError,
        CredentialOfferRepositoryError,
        GetCredentialDetailFlowError,
        GetCredentialsWithDisplaysFlowError
}

sealed interface CredentialClaimDisplayRepositoryError
sealed interface CredentialClaimRepositoryError
sealed interface CredentialIssuerDisplayRepositoryError
sealed interface CredentialRepositoryError
sealed interface CredentialWithDisplaysRepositoryError
sealed interface CredentialWithDisplaysAndClaimsRepositoryError
sealed interface CredentialOfferRepositoryError
sealed interface DeleteCredentialError
sealed interface MapToCredentialClaimDataError
sealed interface GetCredentialDetailFlowError
sealed interface GetCredentialsWithDisplaysFlowError

internal fun CredentialRepositoryError.toDeleteCredentialError() = when (this) {
    is SsiError.Unexpected -> SsiError.Unexpected(cause)
}

internal fun Throwable.toMapToCredentialClaimDataError(message: String): MapToCredentialClaimDataError {
    Timber.e(t = this, message = message)
    return SsiError.Unexpected(this)
}

internal fun Throwable.toCredentialOfferRepositoryError(message: String): CredentialOfferRepositoryError {
    Timber.e(t = this, message = message)
    return SsiError.Unexpected(this)
}

internal fun Throwable.toCredentialIssuerDisplayRepositoryError(message: String): CredentialIssuerDisplayRepositoryError {
    Timber.e(t = this, message = message)
    return SsiError.Unexpected(this)
}
internal fun Throwable.toCredentialWithDisplaysRepositoryError(message: String): CredentialWithDisplaysRepositoryError {
    Timber.e(t = this, message = message)
    return SsiError.Unexpected(this)
}

internal fun Throwable.toCredentialWithDisplaysAndClaimsRepositoryError(message: String): CredentialWithDisplaysAndClaimsRepositoryError {
    Timber.e(t = this, message = message)
    return SsiError.Unexpected(this)
}

internal fun MapToCredentialClaimDataError.toGetCredentialDetailFlowError(): GetCredentialDetailFlowError = when (this) {
    is SsiError.Unexpected -> this
}

internal fun CredentialWithDisplaysAndClaimsRepositoryError.toGetCredentialDetailFlowError(): GetCredentialDetailFlowError = when (this) {
    is SsiError.Unexpected -> this
}

internal fun CredentialWithDisplaysRepositoryError.toGetCredentialWithDisplaysFlowError():
    GetCredentialsWithDisplaysFlowError = when (this) {
    is SsiError.Unexpected -> this
}

internal fun AnyCredentialError.toGetCredentialDetailFlowError(): GetCredentialDetailFlowError = when (this) {
    is CredentialError.Unexpected -> SsiError.Unexpected(cause)
}

internal fun AnyCredentialError.toGetCredentialsWithDisplaysFlowError(): GetCredentialsWithDisplaysFlowError = when (this) {
    is CredentialError.Unexpected -> SsiError.Unexpected(cause)
}

internal fun MapToCredentialDisplayDataError.toGetCredentialDetailFlowError(): GetCredentialDetailFlowError = when (this) {
    is CredentialError.Unexpected -> SsiError.Unexpected(cause)
}

internal fun MapToCredentialDisplayDataError.toGetCredentialsWithDisplaysFlowError(): GetCredentialsWithDisplaysFlowError = when (this) {
    is CredentialError.Unexpected -> SsiError.Unexpected(cause)
}

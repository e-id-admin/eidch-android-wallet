package ch.admin.foitt.wallet.platform.credential.domain.model

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchCredentialByConfigError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchIssuerCredentialInformationError
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialOfferRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.utils.JsonError
import ch.admin.foitt.wallet.platform.utils.JsonParsingError
import timber.log.Timber
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError as OpenIdCredentialOfferError

sealed interface CredentialError {
    data object InvalidGrant : FetchCredentialError
    data object UnsupportedGrantType : FetchCredentialError
    data object UnsupportedCredentialIdentifier : FetchCredentialError
    data object UnsupportedProofType : FetchCredentialError
    data object UnsupportedCryptographicSuite : FetchCredentialError
    data object InvalidCredentialOffer : FetchCredentialError
    data object UnsupportedCredentialFormat : FetchCredentialError, SaveCredentialError
    data object CredentialParsingError : FetchCredentialError, SaveCredentialError
    data object IntegrityCheckFailed : FetchCredentialError
    data object InvalidMetadataClaims : FetchCredentialError, SaveCredentialError
    data object DatabaseError : FetchCredentialError, SaveCredentialError
    data object NetworkError : FetchCredentialError
    data class Unexpected(val cause: Throwable?) :
        FetchCredentialError,
        SaveCredentialError,
        GetAnyCredentialError,
        GetAnyCredentialsError
}

sealed interface FetchCredentialError
sealed interface SaveCredentialError
sealed interface GetAnyCredentialError
sealed interface GetAnyCredentialsError

fun FetchIssuerCredentialInformationError.toFetchCredentialError(): FetchCredentialError = when (this) {
    OpenIdCredentialOfferError.NetworkInfoError -> CredentialError.NetworkError
    is OpenIdCredentialOfferError.Unexpected -> CredentialError.Unexpected(cause)
}

fun FetchCredentialByConfigError.toFetchCredentialError(): FetchCredentialError = when (this) {
    is OpenIdCredentialOfferError.InvalidGrant -> CredentialError.InvalidGrant
    is OpenIdCredentialOfferError.InvalidCredentialOffer -> CredentialError.InvalidCredentialOffer
    is OpenIdCredentialOfferError.UnsupportedCryptographicSuite -> CredentialError.UnsupportedCryptographicSuite
    is OpenIdCredentialOfferError.UnsupportedGrantType -> CredentialError.UnsupportedGrantType
    is OpenIdCredentialOfferError.UnsupportedProofType -> CredentialError.UnsupportedProofType
    is OpenIdCredentialOfferError.IntegrityCheckFailed -> CredentialError.IntegrityCheckFailed
    is OpenIdCredentialOfferError.NetworkInfoError -> CredentialError.NetworkError
    is OpenIdCredentialOfferError.UnsupportedCredentialIdentifier -> CredentialError.UnsupportedCredentialIdentifier
    is OpenIdCredentialOfferError.UnsupportedCredentialFormat -> CredentialError.UnsupportedCredentialFormat
    is OpenIdCredentialOfferError.Unexpected -> CredentialError.Unexpected(cause)
}

fun SaveCredentialError.toFetchCredentialError(): FetchCredentialError = when (this) {
    is CredentialError.CredentialParsingError -> this
    is CredentialError.DatabaseError -> this
    is CredentialError.Unexpected -> this
    is CredentialError.UnsupportedCredentialFormat -> this
    is CredentialError.InvalidMetadataClaims -> this
}

fun JsonParsingError.toSaveCredentialError(): SaveCredentialError = when (this) {
    is JsonError.Unexpected -> CredentialError.UnsupportedCredentialFormat
}

fun Throwable.toSaveCredentialError(): SaveCredentialError {
    Timber.e(this)
    return CredentialError.Unexpected(this)
}

fun CredentialOfferRepositoryError.toSaveCredentialError(): SaveCredentialError = when (this) {
    is SsiError.Unexpected -> CredentialError.Unexpected(cause)
}

fun CredentialRepositoryError.toGetAnyCredentialError(): GetAnyCredentialError = when (this) {
    is SsiError.Unexpected -> CredentialError.Unexpected(cause)
}

fun Throwable.toGetAnyCredentialError(): GetAnyCredentialError {
    Timber.e(this)
    return CredentialError.Unexpected(this)
}

fun CredentialRepositoryError.toGetAnyCredentialsError(): GetAnyCredentialsError = when (this) {
    is SsiError.Unexpected -> CredentialError.Unexpected(cause)
}

fun Throwable.toGetAnyCredentialsError(): GetAnyCredentialsError {
    Timber.e(this)
    return CredentialError.Unexpected(this)
}

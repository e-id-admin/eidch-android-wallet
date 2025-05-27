@file:Suppress("TooManyFunctions")

package ch.admin.foitt.wallet.platform.credential.domain.model

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchCredentialByConfigError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.FetchIssuerCredentialInformationError
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError.CredentialParsingError
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError.DatabaseError
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError.IntegrityCheckFailed
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError.InvalidCredentialOffer
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError.InvalidGrant
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError.InvalidJsonScheme
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError.InvalidMetadataClaims
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError.NetworkError
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError.Unexpected
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError.UnknownIssuer
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError.UnsupportedCredentialFormat
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError.UnsupportedCredentialIdentifier
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError.UnsupportedCryptographicSuite
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError.UnsupportedGrantType
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError.UnsupportedProofType
import ch.admin.foitt.wallet.platform.oca.domain.model.FetchVcMetadataByFormatError
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaError
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
    data object InvalidJsonScheme : FetchCredentialError
    data object DatabaseError : FetchCredentialError, SaveCredentialError
    data object NetworkError : FetchCredentialError
    data object UnknownIssuer : FetchCredentialError
    data class Unexpected(val cause: Throwable?) :
        FetchCredentialError,
        SaveCredentialError,
        GetAnyCredentialError,
        GetAnyCredentialsError,
        AnyCredentialError,
        MapToCredentialDisplayDataError
}

sealed interface FetchCredentialError
sealed interface SaveCredentialError
sealed interface GetAnyCredentialError
sealed interface GetAnyCredentialsError
sealed interface AnyCredentialError
sealed interface MapToCredentialDisplayDataError

fun FetchIssuerCredentialInformationError.toFetchCredentialError(): FetchCredentialError = when (this) {
    OpenIdCredentialOfferError.NetworkInfoError -> NetworkError
    is OpenIdCredentialOfferError.Unexpected -> Unexpected(cause)
}

fun FetchCredentialByConfigError.toFetchCredentialError(): FetchCredentialError = when (this) {
    is OpenIdCredentialOfferError.InvalidGrant -> InvalidGrant
    is OpenIdCredentialOfferError.InvalidCredentialOffer -> InvalidCredentialOffer
    is OpenIdCredentialOfferError.UnsupportedCryptographicSuite -> UnsupportedCryptographicSuite
    is OpenIdCredentialOfferError.UnsupportedGrantType -> UnsupportedGrantType
    is OpenIdCredentialOfferError.UnsupportedProofType -> UnsupportedProofType
    is OpenIdCredentialOfferError.IntegrityCheckFailed -> IntegrityCheckFailed
    is OpenIdCredentialOfferError.NetworkInfoError -> NetworkError
    is OpenIdCredentialOfferError.UnsupportedCredentialIdentifier -> UnsupportedCredentialIdentifier
    is OpenIdCredentialOfferError.UnsupportedCredentialFormat -> UnsupportedCredentialFormat
    is OpenIdCredentialOfferError.Unexpected -> Unexpected(cause)
    is OpenIdCredentialOfferError.UnknownIssuer -> UnknownIssuer
}

fun SaveCredentialError.toFetchCredentialError(): FetchCredentialError = when (this) {
    is CredentialParsingError -> this
    is DatabaseError -> this
    is Unexpected -> this
    is UnsupportedCredentialFormat -> this
    is InvalidMetadataClaims -> this
}

fun JsonParsingError.toSaveCredentialError(): SaveCredentialError = when (this) {
    is JsonError.Unexpected -> UnsupportedCredentialFormat
}

fun Throwable.toSaveCredentialError(message: String): SaveCredentialError {
    Timber.e(t = this, message = message)
    return Unexpected(this)
}

fun CredentialOfferRepositoryError.toSaveCredentialError(): SaveCredentialError = when (this) {
    is SsiError.Unexpected -> Unexpected(cause)
}

fun CredentialRepositoryError.toGetAnyCredentialError(): GetAnyCredentialError = when (this) {
    is SsiError.Unexpected -> Unexpected(cause)
}

fun CredentialRepositoryError.toGetAnyCredentialsError(): GetAnyCredentialsError = when (this) {
    is SsiError.Unexpected -> Unexpected(cause)
}

fun AnyCredentialError.toGetAnyCredentialError(): GetAnyCredentialError = when (this) {
    is Unexpected -> this
}

fun Throwable.toAnyCredentialError(message: String): AnyCredentialError {
    Timber.e(t = this, message = message)
    return Unexpected(this)
}

fun FetchVcMetadataByFormatError.toFetchCredentialError(): FetchCredentialError = when (this) {
    is OcaError.InvalidOca -> InvalidCredentialOffer
    is OcaError.NetworkError -> NetworkError
    is OcaError.Unexpected -> Unexpected(cause)
    OcaError.InvalidJsonScheme -> InvalidJsonScheme
}

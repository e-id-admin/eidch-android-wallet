package ch.admin.foitt.openid4vc.domain.model.credentialoffer

import ch.admin.foitt.openid4vc.domain.model.CreateJWSKeyPairError
import ch.admin.foitt.openid4vc.domain.model.KeyPairError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.TypeMetadataRepositoryError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VerifyJwtError
import ch.admin.foitt.openid4vc.utils.JsonError
import ch.admin.foitt.openid4vc.utils.JsonParsingError
import timber.log.Timber

interface CredentialOfferError {
    data object InvalidGrant :
        FetchCredentialByConfigError, FetchVerifiableCredentialError, FetchCredentialError
    data object UnsupportedGrantType :
        FetchCredentialByConfigError, FetchVerifiableCredentialError, FetchCredentialError
    data object UnsupportedCredentialIdentifier : FetchCredentialByConfigError
    data object UnsupportedProofType :
        FetchCredentialByConfigError, FetchVerifiableCredentialError, FetchCredentialError
    data object UnsupportedCryptographicSuite :
        FetchCredentialByConfigError, FetchVerifiableCredentialError, CreateDidJwkError, FetchCredentialError
    data object InvalidCredentialOffer :
        FetchCredentialByConfigError, FetchVerifiableCredentialError, FetchCredentialError
    data object UnsupportedCredentialFormat : FetchCredentialByConfigError
    data object IntegrityCheckFailed : FetchCredentialByConfigError, FetchCredentialError
    data object UnknownIssuer : FetchCredentialByConfigError, FetchCredentialError
    data object NetworkInfoError :
        FetchIssuerCredentialInformationError,
        FetchCredentialByConfigError,
        FetchVerifiableCredentialError,
        FetchIssuerConfigurationError,
        FetchCredentialError
    data class Unexpected(val cause: Throwable?) :
        FetchIssuerCredentialInformationError,
        FetchCredentialByConfigError,
        CreateDidJwkError,
        FetchVerifiableCredentialError,
        FetchIssuerConfigurationError,
        FetchCredentialError
}

sealed interface FetchIssuerCredentialInformationError
sealed interface FetchCredentialByConfigError
internal sealed interface FetchCredentialError
internal sealed interface CreateDidJwkError
sealed interface FetchVerifiableCredentialError
sealed interface FetchIssuerConfigurationError

internal fun FetchCredentialError.toFetchCredentialByConfigError(): FetchCredentialByConfigError = when (this) {
    is CredentialOfferError.IntegrityCheckFailed -> this
    is CredentialOfferError.InvalidCredentialOffer -> this
    is CredentialOfferError.InvalidGrant -> this
    is CredentialOfferError.NetworkInfoError -> this
    is CredentialOfferError.Unexpected -> this
    is CredentialOfferError.UnsupportedCryptographicSuite -> this
    is CredentialOfferError.UnsupportedGrantType -> this
    is CredentialOfferError.UnsupportedProofType -> this
    is CredentialOfferError.UnknownIssuer -> this
}

internal fun FetchVerifiableCredentialError.toFetchCredentialError(): FetchCredentialError = when (this) {
    is CredentialOfferError.InvalidCredentialOffer -> this
    is CredentialOfferError.InvalidGrant -> this
    is CredentialOfferError.NetworkInfoError -> this
    is CredentialOfferError.Unexpected -> this
    is CredentialOfferError.UnsupportedCryptographicSuite -> this
    is CredentialOfferError.UnsupportedGrantType -> this
    is CredentialOfferError.UnsupportedProofType -> this
}

internal fun CreateJWSKeyPairError.toCredentialOfferError() = when (this) {
    is KeyPairError.Unexpected -> CredentialOfferError.Unexpected(throwable)
}

internal fun FetchIssuerCredentialInformationError.toFetchVerifiableCredentialError(): FetchVerifiableCredentialError = when (this) {
    is CredentialOfferError.NetworkInfoError -> this
    is CredentialOfferError.Unexpected -> this
}

internal fun FetchIssuerConfigurationError.toFetchVerifiableCredentialError(): FetchVerifiableCredentialError = when (this) {
    is CredentialOfferError.NetworkInfoError -> this
    is CredentialOfferError.Unexpected -> this
}

internal fun CreateDidJwkError.toFetchVerifiableCredentialError(): FetchVerifiableCredentialError = when (this) {
    is CredentialOfferError.UnsupportedCryptographicSuite -> this
    is CredentialOfferError.Unexpected -> this
}

internal fun VerifyJwtError.toFetchCredentialError(): FetchCredentialError = when (this) {
    VcSdJwtError.InvalidJwt,
    VcSdJwtError.DidDocumentDeactivated -> CredentialOfferError.IntegrityCheckFailed
    VcSdJwtError.NetworkError -> CredentialOfferError.NetworkInfoError
    VcSdJwtError.IssuerValidationFailed -> CredentialOfferError.UnknownIssuer
    is VcSdJwtError.Unexpected -> CredentialOfferError.Unexpected(cause)
}

internal fun Throwable.toFetchCredentialError(message: String): FetchCredentialError {
    Timber.e(t = this, message = message)
    return CredentialOfferError.Unexpected(this)
}

internal fun TypeMetadataRepositoryError.toFetchCredentialError(): FetchCredentialError = when (this) {
    is VcSdJwtError.NetworkError -> CredentialOfferError.NetworkInfoError
    is VcSdJwtError.Unexpected -> CredentialOfferError.Unexpected(cause)
}

internal fun JsonParsingError.toFetchCredentialError(): FetchCredentialError = when (this) {
    is JsonError.Unexpected -> CredentialOfferError.Unexpected(throwable)
}

@file:Suppress("TooManyFunctions")

package ch.admin.foitt.wallet.platform.actorMetadata.domain.model

import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorMetaDataError.Unexpected
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchTrustForIssuanceError
import ch.admin.foitt.wallet.platform.credential.domain.model.GetAllAnyCredentialsByCredentialIdError
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialIssuerDisplayRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.RawCredentialDataRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ProcessIdentityTrustStatementError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ProcessIdentityV1TrustStatementError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustRegistryError
import ch.admin.foitt.wallet.platform.utils.JsonError
import ch.admin.foitt.wallet.platform.utils.JsonParsingError

interface ActorMetaDataError {
    data object UnverifiedIssuer :
        FetchAndCacheIssuerDisplayDataError

    data class UnverifiedVerifier(val responseUri: String?) :
        FetchTrustForVerificationError,
        FetchAndCacheVerifierDisplayDataError

    data object UnauthorizedIssuance :
        FetchAndCacheIssuerDisplayDataError

    data class UnknownRegistry(val responseUri: String?) :
        FetchAndCacheIssuerDisplayDataError,
        FetchTrustForVerificationError,
        FetchAndCacheVerifierDisplayDataError

    data class Unexpected(val cause: Throwable?) :
        FetchAndCacheIssuerDisplayDataError,
        FetchAndCacheVerifierDisplayDataError,
        FetchTrustForVerificationError
}

sealed interface FetchTrustForVerificationError
sealed interface FetchAndCacheIssuerDisplayDataError
sealed interface FetchAndCacheVerifierDisplayDataError

fun ProcessIdentityTrustStatementError.toFetchTrustForVerificationError(
    responseUri: String?,
): FetchTrustForVerificationError = when (this) {
    is TrustRegistryError.UnknownRegistry -> ActorMetaDataError.UnknownRegistry(responseUri)
    is TrustRegistryError.Unexpected -> ActorMetaDataError.UnverifiedVerifier(responseUri)
}

fun ProcessIdentityV1TrustStatementError.toFetchTrustForVerificationError(
    responseUri: String?,
): FetchTrustForVerificationError = when (this) {
    is TrustRegistryError.InvalidTrustStatus,
    is TrustRegistryError.Unexpected -> ActorMetaDataError.UnverifiedVerifier(responseUri)
}

fun FetchTrustForVerificationError.toFetchAndCacheVerifierDisplayDataError(): FetchAndCacheVerifierDisplayDataError = when (this) {
    is ActorMetaDataError.UnverifiedVerifier -> this
    is ActorMetaDataError.UnknownRegistry -> this
    is Unexpected -> this
}

fun GetAllAnyCredentialsByCredentialIdError.toFetchAndCacheIssuerDisplayDataError(): FetchAndCacheIssuerDisplayDataError = when (this) {
    is CredentialError.Unexpected -> Unexpected(cause)
}

fun CredentialIssuerDisplayRepositoryError.toFetchAndCacheIssuerDisplayDataError(): FetchAndCacheIssuerDisplayDataError = when (this) {
    is SsiError.Unexpected -> Unexpected(cause)
}

fun RawCredentialDataRepositoryError.toFetchAndCacheIssuerDisplayDataError(): FetchAndCacheIssuerDisplayDataError = when (this) {
    is SsiError.Unexpected -> Unexpected(cause)
}

fun CredentialRepositoryError.toFetchAndCacheIssuerDisplayDataError(): FetchAndCacheIssuerDisplayDataError = when (this) {
    is SsiError.Unexpected -> Unexpected(cause)
}

fun ProcessIdentityTrustStatementError.toFetchAndCacheIssuerDisplayDataError(): FetchAndCacheIssuerDisplayDataError = when (this) {
    is TrustRegistryError.Unexpected -> ActorMetaDataError.UnverifiedIssuer
    is TrustRegistryError.UnknownRegistry -> ActorMetaDataError.UnknownRegistry(null)
}

fun JsonParsingError.toFetchAndCacheIssuerDisplayDataError(): FetchAndCacheIssuerDisplayDataError = when (this) {
    is JsonError.Unexpected -> Unexpected(throwable)
}

fun FetchTrustForIssuanceError.toFetchAndCacheIssuerDisplayDataError(): FetchAndCacheIssuerDisplayDataError = when (this) {
    is CredentialError.UnverifiedIssuer -> ActorMetaDataError.UnverifiedIssuer
    is CredentialError.UnauthorizedIssuance -> ActorMetaDataError.UnauthorizedIssuance
    is CredentialError.UnknownRegistry -> ActorMetaDataError.UnknownRegistry(null)
    is CredentialError.Unexpected -> Unexpected(cause)
}

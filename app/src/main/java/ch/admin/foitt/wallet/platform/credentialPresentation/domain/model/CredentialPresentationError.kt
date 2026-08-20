@file:Suppress("TooManyFunctions")

package ch.admin.foitt.wallet.platform.credentialPresentation.domain.model

import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VerifyRequestObjectSignatureError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CredentialPresentationError.Unexpected
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialWithKeyBindingRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.model.VerifiableCredentialRepositoryError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ProcessIdentityTrustStatementError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustRegistryError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ValidateVqPsError
import ch.admin.foitt.wallet.platform.utils.JsonError
import ch.admin.foitt.wallet.platform.utils.JsonParsingError
import timber.log.Timber

internal interface CredentialPresentationError {
    data object InvalidUri : ValidateRedirectUriError

    // https://openid.net/specs/openid-4-verifiable-presentations-1_0.html#section-8.5
    data class InvalidRequest(
        val responseUri: String?,
        val state: String?,
    ) : ProcessPresentationRequestError,
        ValidatePresentationRequestError

    data class InvalidClient(
        val responseUri: String,
        val state: String?,
    ) : ProcessPresentationRequestError,
        ValidatePresentationRequestError

    data class InvalidTransactionData(
        val responseUri: String?,
        val state: String?,
    ) : ProcessPresentationRequestError,
        ValidatePresentationRequestError

    data class EmptyWallet(
        val responseUri: String?,
        val state: String?,
    ) : ProcessPresentationRequestError
    data class NoCompatibleCredential(
        val responseUri: String?,
        val state: String?,
    ) : ProcessPresentationRequestError

    data class UnverifiedVerifier(
        val responseUri: String?,
        val state: String?,
    ) : ValidatePresentationRequestError
    data class UnknownRegistry(
        val responseUri: String?,
        val state: String?,
    ) : ValidatePresentationRequestError

    data object UnknownVerifier : ValidatePresentationRequestError, ProcessPresentationRequestError
    data object NetworkError : ValidatePresentationRequestError, ProcessPresentationRequestError
    data class Unexpected(val cause: Throwable?) :
        ProcessPresentationRequestError,
        GetCompatibleCredentialsError,
        GetPresentationPathsError,
        ValidatePresentationRequestError
}

sealed interface ProcessPresentationRequestError
sealed interface ValidatePresentationRequestError
sealed interface GetCompatibleCredentialsError
sealed interface GetPresentationPathsError
sealed interface ValidateRedirectUriError

internal fun ValidatePresentationRequestError.toProximityEngagementError(): ProximityEngagementError = when (this) {
    is CredentialPresentationError.NetworkError -> ProximityEngagementError.Disconnected
    is CredentialPresentationError.Unexpected -> ProximityEngagementError.Unexpected(cause)
    else -> ProximityEngagementError.Unexpected(null)
}

internal fun ProcessPresentationRequestError.toProximityEngagementError(): ProximityEngagementError = when (this) {
    is CredentialPresentationError.NetworkError -> ProximityEngagementError.Disconnected
    is CredentialPresentationError.Unexpected -> ProximityEngagementError.Unexpected(cause)
    is CredentialPresentationError.EmptyWallet,
    is CredentialPresentationError.NoCompatibleCredential -> ProximityEngagementError.NoCompatibleCredential

    else -> ProximityEngagementError.Unexpected(null)
}

internal fun VerifiableCredentialRepositoryError.toProcessPresentationRequestError(): ProcessPresentationRequestError = when (this) {
    is SsiError.Unexpected -> Unexpected(cause)
}

internal fun GetCompatibleCredentialsError.toProcessPresentationRequestError(): ProcessPresentationRequestError = when (this) {
    is Unexpected -> this
}

internal fun CredentialWithKeyBindingRepositoryError.toGetCompatibleCredentialsError(): GetCompatibleCredentialsError = when (this) {
    is SsiError.Unexpected -> Unexpected(cause)
}

internal fun Throwable.toValidatePresentationRequestError(
    responseUri: String?,
    state: String?,
    message: String
): ValidatePresentationRequestError {
    Timber.e(t = this, message = message)
    return CredentialPresentationError.InvalidRequest(responseUri, state)
}

internal fun VerifyRequestObjectSignatureError.toValidatePresentationRequestError(
    responseUri: String?,
    state: String?,
): ValidatePresentationRequestError = when (this) {
    is VcSdJwtError.InvalidDid,
    is VcSdJwtError.InvalidJwt,
    is VcSdJwtError.DidDocumentDeactivated,
    is VcSdJwtError.InvalidRequestObject,
    is VcSdJwtError.Unexpected -> CredentialPresentationError.InvalidRequest(responseUri, state)

    is VcSdJwtError.IssuerValidationFailed -> CredentialPresentationError.UnknownVerifier
    is VcSdJwtError.NetworkError -> CredentialPresentationError.NetworkError
}

internal fun JsonParsingError.toValidatePresentationRequestError(): ValidatePresentationRequestError = when (this) {
    is JsonError.Unexpected -> Unexpected(throwable)
}

internal fun ValidateVqPsError.toValidatePresentationRequestError(
    responseUri: String?,
    state: String?,
): ValidatePresentationRequestError = when (this) {
    is TrustRegistryError.UnknownRegistry -> CredentialPresentationError.UnknownRegistry(responseUri, state)
    is TrustRegistryError.InvalidTrustStatus -> CredentialPresentationError.InvalidRequest(responseUri, state)
    is TrustRegistryError.Unexpected -> Unexpected(cause)
}

internal fun ProcessIdentityTrustStatementError.toValidatePresentationRequestError(
    responseUri: String?,
    state: String?,
): ValidatePresentationRequestError = when (this) {
    is TrustRegistryError.UnknownRegistry -> CredentialPresentationError.UnknownRegistry(responseUri, state)
    is TrustRegistryError.Unexpected -> CredentialPresentationError.UnverifiedVerifier(responseUri, state)
}

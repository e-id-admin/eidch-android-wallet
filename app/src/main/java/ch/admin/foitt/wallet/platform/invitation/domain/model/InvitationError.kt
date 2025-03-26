package ch.admin.foitt.wallet.platform.invitation.domain.model

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.FetchPresentationRequestError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestError
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchCredentialError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CredentialPresentationError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.ProcessPresentationRequestError
import ch.admin.foitt.wallet.platform.utils.JsonError
import ch.admin.foitt.wallet.platform.utils.JsonParsingError
import timber.log.Timber

interface InvitationError {
    data object UnknownSchema : ValidateInvitationError
    data object InvalidUri : GetPresentationRequestError, ValidateInvitationError
    data object InvalidCredentialOffer : ProcessInvitationError
    data object NoCredentialsFound : GetCredentialOfferError, ValidateInvitationError
    data class UnsupportedGrantType(val message: String) : GetCredentialOfferError, ValidateInvitationError
    data object CredentialOfferDeserializationFailed : GetCredentialOfferError, ValidateInvitationError
    data object NetworkError : ProcessInvitationError, GetPresentationRequestError, ValidateInvitationError
    data object EmptyWallet : ProcessInvitationError
    data object NoCompatibleCredential : ProcessInvitationError
    data object InvalidInput : ProcessInvitationError
    data object InvalidPresentationRequest : GetPresentationRequestError, ValidateInvitationError, ProcessInvitationError
    data class InvalidPresentation(val responseUri: String) : ProcessInvitationError
    data object CredentialOfferExpired : ProcessInvitationError
    data object UnknownIssuer : ProcessInvitationError
    data object UnknownVerifier : ProcessInvitationError
    data object Unexpected : ProcessInvitationError, GetPresentationRequestError, ValidateInvitationError
}

sealed interface ProcessInvitationError : InvitationError
sealed interface GetCredentialOfferError : InvitationError
sealed interface GetPresentationRequestError : InvitationError
sealed interface ValidateInvitationError : InvitationError

//region Error to Error mappings
internal fun FetchPresentationRequestError.toGetPresentationRequestError(): GetPresentationRequestError = when (this) {
    PresentationRequestError.NetworkError -> InvitationError.NetworkError
    is PresentationRequestError.Unexpected -> InvitationError.InvalidPresentationRequest
}

internal fun GetPresentationRequestError.toValidateInvitationError(): ValidateInvitationError = when (this) {
    is InvitationError.InvalidUri -> this
    is InvitationError.NetworkError -> this
    is InvitationError.InvalidPresentationRequest -> this
    is InvitationError.Unexpected -> this
}

internal fun GetCredentialOfferError.toValidateInvitationError(): ValidateInvitationError = when (this) {
    is InvitationError.CredentialOfferDeserializationFailed -> this
    is InvitationError.NoCredentialsFound -> this
    is InvitationError.UnsupportedGrantType -> this
}

internal fun FetchCredentialError.toProcessInvitationError(): ProcessInvitationError = when (this) {
    CredentialError.InvalidGrant -> InvitationError.CredentialOfferExpired
    CredentialError.IntegrityCheckFailed,
    CredentialError.UnsupportedGrantType,
    CredentialError.InvalidCredentialOffer,
    CredentialError.UnsupportedCredentialFormat,
    CredentialError.UnsupportedCredentialIdentifier,
    CredentialError.UnsupportedProofType,
    CredentialError.UnsupportedCryptographicSuite,
    CredentialError.CredentialParsingError,
    CredentialError.InvalidMetadataClaims -> InvitationError.InvalidCredentialOffer
    CredentialError.NetworkError -> InvitationError.NetworkError
    CredentialError.DatabaseError,
    is CredentialError.Unexpected -> InvitationError.Unexpected
    CredentialError.UnknownIssuer -> InvitationError.UnknownIssuer
}

internal fun ValidateInvitationError.toProcessInvitationError(): ProcessInvitationError = when (this) {
    is InvitationError.InvalidUri,
    is InvitationError.UnknownSchema -> InvitationError.InvalidInput
    is InvitationError.InvalidPresentationRequest -> this
    is InvitationError.NetworkError -> this
    is InvitationError.UnsupportedGrantType,
    is InvitationError.CredentialOfferDeserializationFailed,
    is InvitationError.NoCredentialsFound -> InvitationError.InvalidCredentialOffer
    is InvitationError.Unexpected -> this
}

internal fun Throwable.toGetCredentialOfferError(message: String): GetCredentialOfferError {
    Timber.e(t = this, message = message)
    return InvitationError.CredentialOfferDeserializationFailed
}

internal fun JsonParsingError.toGetCredentialOfferError(): GetCredentialOfferError = when (this) {
    is JsonError.Unexpected -> InvitationError.CredentialOfferDeserializationFailed
}

internal fun ProcessPresentationRequestError.toProcessInvitationError(): ProcessInvitationError = when (this) {
    CredentialPresentationError.EmptyWallet -> InvitationError.EmptyWallet
    CredentialPresentationError.NoCompatibleCredential -> InvitationError.NoCompatibleCredential
    is CredentialPresentationError.InvalidPresentation -> InvitationError.InvalidPresentation(responseUri)
    is CredentialPresentationError.Unexpected -> InvitationError.Unexpected
    is CredentialPresentationError.UnknownVerifier -> InvitationError.UnknownVerifier
    CredentialPresentationError.NetworkError -> InvitationError.NetworkError
}
//endregion

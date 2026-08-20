@file:Suppress("TooManyFunctions")

package ch.admin.foitt.wallet.platform.invitation.domain.model

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.FetchPresentationRequestError
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestError
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchCredentialError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CredentialPresentationError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.ProcessPresentationRequestError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.ValidatePresentationRequestError
import ch.admin.foitt.wallet.platform.genericScreens.domain.model.GenericErrorScreenState
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.CredentialOfferDeserializationFailed
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.CredentialOfferExpired
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.CredentialRequestDenied
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.EmptyWallet
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.IncompatibleDeviceKeyStorage
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.InsufficientScope
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.InvalidClient
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.InvalidClientPresentation
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.InvalidCredentialOffer
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.InvalidCredentialRequest
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.InvalidEncryptionParameters
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.InvalidInput
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.InvalidNonce
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.InvalidPresentation
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.InvalidPresentationRequest
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.InvalidProof
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.InvalidRequest
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.InvalidRequestBearerToken
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.InvalidToken
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.InvalidTransactionData
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.InvalidUri
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.MetadataMisconfiguration
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.NetworkError
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.NoCompatibleCredential
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.NoCredentialsFound
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.UnauthorizedClient
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.UnauthorizedGrantType
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.Unexpected
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.UnknownCredentialConfiguration
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.UnknownCredentialIdentifier
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.UnknownIssuer
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.UnknownSchema
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.UnknownVerifier
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.UnsupportedGrantType
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError.UnsupportedKeyStorageSecurityLevel
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination.GenericErrorScreen
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination.InvitationFailureScreen
import ch.admin.foitt.wallet.platform.utils.JsonError
import ch.admin.foitt.wallet.platform.utils.JsonParsingError
import timber.log.Timber
import java.net.URI

interface InvitationError {
    data object UnknownSchema : ValidateInvitationError
    data object InvalidUri : GetPresentationRequestError, ValidateInvitationError
    data object InvalidCredentialOffer : ProcessInvitationError
    data object NoCredentialsFound : GetCredentialOfferError, ValidateInvitationError
    data class UnsupportedGrantType(val message: String) : GetCredentialOfferError, ValidateInvitationError
    data class CredentialOfferDeserializationFailed(val throwable: Throwable?) : GetCredentialOfferError, ValidateInvitationError
    data object NetworkError : ProcessInvitationError, GetPresentationRequestError, ValidateInvitationError
    data class EmptyWallet(
        val responseUri: String?,
        val state: String?,
    ) : ProcessInvitationError
    data class NoCompatibleCredential(
        val responseUri: String?,
        val state: String?,
    ) : ProcessInvitationError
    data object InvalidInput : ProcessInvitationError
    data object InvalidPresentationRequest : GetPresentationRequestError, ValidateInvitationError, ProcessInvitationError
    data class InvalidPresentation(
        val responseUri: String?,
        val state: String?,
    ) : ProcessInvitationError, GetPresentationRequestError, ValidateInvitationError
    data class InvalidClientPresentation(
        val responseUri: String?,
        val state: String?,
    ) : ProcessInvitationError,
        GetPresentationRequestError,
        ValidateInvitationError

    data class InvalidTransactionData(
        val responseUri: String?,
        val state: String?,
    ) : ProcessInvitationError,
        GetPresentationRequestError,
        ValidateInvitationError

    data object UnverifiedIssuer :
        ProcessInvitationError,
        ValidateInvitationError

    data class UnverifiedVerifier(
        val responseUri: String?,
        val state: String?,
    ) : ProcessInvitationError,
        GetPresentationRequestError,
        ValidateInvitationError

    data object UnauthorizedIssuance :
        ProcessInvitationError,
        ValidateInvitationError

    data class UnknownRegistry(
        val responseUri: String?,
        val state: String?,
    ) : ProcessInvitationError,
        ValidateInvitationError,
        GetPresentationRequestError

    data object CredentialOfferExpired : ProcessInvitationError
    data object UnknownIssuer : ProcessInvitationError
    data object UnknownVerifier : ProcessInvitationError, GetPresentationRequestError, ValidateInvitationError
    data object UnsupportedKeyStorageSecurityLevel : ProcessInvitationError
    data object IncompatibleDeviceKeyStorage : ProcessInvitationError
    data class MetadataMisconfiguration(val message: String) : ProcessInvitationError

    data object CredentialRequestDenied : ProcessInvitationError
    data object InsufficientScope : ProcessInvitationError
    data object InvalidClient : ProcessInvitationError
    data object InvalidCredentialRequest : ProcessInvitationError
    data object InvalidEncryptionParameters : ProcessInvitationError
    data object InvalidNonce : ProcessInvitationError
    data object InvalidProof : ProcessInvitationError
    data object InvalidRequest : ProcessInvitationError
    data object InvalidRequestBearerToken : ProcessInvitationError
    data object InvalidToken : ProcessInvitationError
    data object UnauthorizedClient : ProcessInvitationError
    data object UnauthorizedGrantType : ProcessInvitationError
    data object UnknownCredentialConfiguration : ProcessInvitationError
    data object UnknownCredentialIdentifier : ProcessInvitationError

    data object Unexpected :
        ProcessInvitationError,
        GetPresentationRequestError,
        GetProximityPresentationRequestError,
        ValidateInvitationError
}

sealed interface ProcessInvitationError : InvitationError
sealed interface GetCredentialOfferError : InvitationError
sealed interface GetPresentationRequestError : InvitationError
sealed interface GetProximityPresentationRequestError : InvitationError
sealed interface ValidateInvitationError : InvitationError

//region Error to Error mappings
internal fun FetchPresentationRequestError.toGetPresentationRequestError(): GetPresentationRequestError = when (this) {
    PresentationRequestError.NetworkError -> NetworkError
    is PresentationRequestError.Unexpected -> InvalidPresentationRequest
}

internal fun GetPresentationRequestError.toValidateInvitationError(): ValidateInvitationError = when (this) {
    is InvalidUri -> this
    is NetworkError -> this
    is InvalidPresentationRequest -> this
    is Unexpected -> this
    is InvalidPresentation -> this
    is InvalidClientPresentation -> this
    is UnknownVerifier -> this
    is InvalidTransactionData -> this
    is InvitationError.UnverifiedVerifier -> this
    is InvitationError.UnknownRegistry -> this
}

internal fun GetProximityPresentationRequestError.toValidateInvitationError(): ValidateInvitationError = when (this) {
    is Unexpected -> this
}

internal fun GetCredentialOfferError.toValidateInvitationError(): ValidateInvitationError = when (this) {
    is CredentialOfferDeserializationFailed -> this
    is NoCredentialsFound -> this
    is UnsupportedGrantType -> this
}

@Suppress("CyclomaticComplexMethod")
internal fun FetchCredentialError.toProcessInvitationError(): ProcessInvitationError = when (this) {
    CredentialError.InvalidGrant -> CredentialOfferExpired
    CredentialError.IntegrityCheckFailed,
    CredentialError.UnsupportedGrantType,
    CredentialError.InvalidCredentialOffer,
    CredentialError.UnsupportedCredentialFormat,
    CredentialError.UnsupportedCredentialIdentifier,
    CredentialError.UnsupportedProofType,
    CredentialError.UnsupportedCryptographicSuite,
    CredentialError.CredentialParsingError,
    CredentialError.InvalidJsonScheme,
    is CredentialError.InvalidSignedMetadata,
    CredentialError.InvalidIssuerCredentialInfo,
    CredentialError.UnsupportedImageFormat,
    CredentialError.InvalidGenerateMetadataClaims -> InvalidCredentialOffer

    CredentialError.NetworkError -> NetworkError
    CredentialError.DatabaseError,
    is CredentialError.Unexpected -> Unexpected

    CredentialError.UnknownIssuer -> UnknownIssuer
    CredentialError.UnsupportedKeyStorageSecurityLevel -> UnsupportedKeyStorageSecurityLevel
    CredentialError.IncompatibleDeviceKeyStorage -> IncompatibleDeviceKeyStorage
    is CredentialError.MetadataMisconfiguration -> MetadataMisconfiguration(message)
    CredentialError.CredentialRequestDenied -> CredentialRequestDenied
    CredentialError.InsufficientScope -> InsufficientScope
    CredentialError.InvalidClient -> InvalidClient
    CredentialError.InvalidCredentialRequest -> InvalidCredentialRequest
    CredentialError.InvalidEncryptionParameters -> InvalidEncryptionParameters
    CredentialError.InvalidNonce -> InvalidNonce
    CredentialError.InvalidProof -> InvalidProof
    CredentialError.InvalidRequest -> InvalidRequest
    CredentialError.InvalidRequestBearerToken -> InvalidRequestBearerToken
    CredentialError.InvalidToken -> InvalidToken
    CredentialError.UnauthorizedClient -> UnauthorizedClient
    CredentialError.UnauthorizedGrantType -> UnauthorizedGrantType
    CredentialError.UnknownCredentialConfiguration -> UnknownCredentialConfiguration
    CredentialError.UnknownCredentialIdentifier -> UnknownCredentialIdentifier

    CredentialError.UnverifiedIssuer -> InvitationError.UnverifiedIssuer
    CredentialError.UnauthorizedIssuance -> InvitationError.UnauthorizedIssuance
    CredentialError.UnknownRegistry -> InvitationError.UnknownRegistry(null, null) // no responseUri in issuance
}

internal fun ValidateInvitationError.toProcessInvitationError(): ProcessInvitationError = when (this) {
    is InvalidUri,
    is UnknownSchema -> InvalidInput

    is InvalidPresentationRequest -> this
    is NetworkError -> this
    is UnsupportedGrantType,
    is CredentialOfferDeserializationFailed,
    is NoCredentialsFound -> InvalidCredentialOffer

    is Unexpected -> this
    is InvalidPresentation -> this
    is InvalidClientPresentation -> this
    is UnknownVerifier -> this
    is InvalidTransactionData -> this

    is InvitationError.UnverifiedIssuer -> this
    is InvitationError.UnverifiedVerifier -> this
    is InvitationError.UnauthorizedIssuance -> this
    is InvitationError.UnknownRegistry -> this
}

internal fun Throwable.toGetCredentialOfferError(message: String): GetCredentialOfferError {
    Timber.e(t = this, message = message)
    return CredentialOfferDeserializationFailed(this)
}

internal fun JsonParsingError.toGetCredentialOfferError(): GetCredentialOfferError = when (this) {
    is JsonError.Unexpected -> CredentialOfferDeserializationFailed(throwable)
}

internal fun ProcessPresentationRequestError.toProcessInvitationError(): ProcessInvitationError = when (this) {
    is CredentialPresentationError.EmptyWallet -> EmptyWallet(responseUri, state)
    is CredentialPresentationError.NoCompatibleCredential -> NoCompatibleCredential(responseUri, state)
    is CredentialPresentationError.InvalidRequest -> InvalidPresentation(responseUri, state)
    is CredentialPresentationError.Unexpected -> Unexpected
    is CredentialPresentationError.UnknownVerifier -> UnknownVerifier
    CredentialPresentationError.NetworkError -> NetworkError
    is CredentialPresentationError.InvalidClient -> InvalidClientPresentation(responseUri, state)
    is CredentialPresentationError.InvalidTransactionData -> InvalidTransactionData(responseUri, state)
}

@Suppress("CyclomaticComplexMethod")
internal fun ProcessInvitationError.toErrorDestination(uri: String?, state: String?): Destination = when (this) {
    NetworkError -> InvitationFailureScreen(invitationError = InvitationErrorScreenState.NETWORK_ERROR, responseUri = uri, state = state)
    InvalidCredentialOffer,
    InvalidInput,
    is MetadataMisconfiguration,
    CredentialOfferExpired -> InvitationFailureScreen(
        invitationError = InvitationErrorScreenState.INVALID_CREDENTIAL,
        responseUri = uri,
        state = state
    )

    UnknownVerifier,
    Unexpected -> {
        Timber.w("Unexpected state on processing deeplink")
        InvitationFailureScreen(invitationError = InvitationErrorScreenState.UNEXPECTED, responseUri = uri, state = state)
    }

    UnknownIssuer -> InvitationFailureScreen(invitationError = InvitationErrorScreenState.UNKNOWN_ISSUER, responseUri = uri, state = state)
    UnsupportedKeyStorageSecurityLevel -> InvitationFailureScreen(
        invitationError = InvitationErrorScreenState.UNSUPPORTED_KEY_STORAGE,
        responseUri = uri,
        state = state
    )

    IncompatibleDeviceKeyStorage -> InvitationFailureScreen(
        invitationError = InvitationErrorScreenState.UNSUPPORTED_KEY_STORAGE_CAPABILITIES,
        responseUri = uri,
        state = state
    )

    InvalidPresentationRequest -> InvitationFailureScreen(
        invitationError = InvitationErrorScreenState.INVALID_PRESENTATION,
        responseUri = uri,
        state = state
    )
    is EmptyWallet -> InvitationFailureScreen(
        invitationError = InvitationErrorScreenState.EMPTY_WALLET,
        responseUri = this.responseUri,
        state = this.state
    )
    is NoCompatibleCredential -> InvitationFailureScreen(
        invitationError = InvitationErrorScreenState.NO_COMPATIBLE_CREDENTIAL,
        responseUri = this.responseUri,
        state = this.state
    )

    CredentialRequestDenied -> GenericErrorScreen(GenericErrorScreenState.Offer.credentialRequestDenied())
    InsufficientScope -> GenericErrorScreen(error = GenericErrorScreenState.Offer.insufficientScope())
    InvalidClient -> GenericErrorScreen(error = GenericErrorScreenState.Offer.invalidClient())
    InvalidCredentialRequest -> GenericErrorScreen(error = GenericErrorScreenState.Offer.invalidCredentialRequest())
    InvalidEncryptionParameters -> GenericErrorScreen(error = GenericErrorScreenState.Offer.invalidEncryptionParameters())

    InvalidNonce -> GenericErrorScreen(error = GenericErrorScreenState.Offer.invalidNonce())
    InvalidProof -> GenericErrorScreen(error = GenericErrorScreenState.Offer.invalidProof())
    InvalidRequest -> GenericErrorScreen(error = GenericErrorScreenState.Offer.invalidRequest())
    InvalidRequestBearerToken -> GenericErrorScreen(error = GenericErrorScreenState.Offer.invalidRequestBearerToken())
    InvalidToken -> GenericErrorScreen(error = GenericErrorScreenState.Offer.invalidToken())
    UnauthorizedClient -> GenericErrorScreen(error = GenericErrorScreenState.Offer.unauthorizedClient())
    UnauthorizedGrantType -> GenericErrorScreen(error = GenericErrorScreenState.Offer.unauthorizedGrantType())
    UnknownCredentialConfiguration -> GenericErrorScreen(error = GenericErrorScreenState.Offer.unknownCredentialConfiguration())
    UnknownCredentialIdentifier -> GenericErrorScreen(error = GenericErrorScreenState.Offer.unknownCredentialIdentifier())

    is InvalidPresentation -> GenericErrorScreen(error = GenericErrorScreenState.Presentation.invalidRequest()) // no need to decline again
    is InvalidClientPresentation -> GenericErrorScreen(error = GenericErrorScreenState.Presentation.invalidClient(responseUri, this.state))
    is InvalidTransactionData -> GenericErrorScreen(
        error = GenericErrorScreenState.Presentation.invalidTransactionData(responseUri, this.state)
    )

    is InvitationError.UnverifiedIssuer -> GenericErrorScreen(error = GenericErrorScreenState.Offer.unverifiedIssuer())
    is InvitationError.UnauthorizedIssuance -> GenericErrorScreen(error = GenericErrorScreenState.Offer.unauthorizedIssuance())
    is InvitationError.UnverifiedVerifier -> GenericErrorScreen(
        error = GenericErrorScreenState.Presentation.unverifiedVerifier(responseUri, this.state)
    )
    is InvitationError.UnknownRegistry -> GenericErrorScreen(
        error = GenericErrorScreenState.General.unknownRegistry(responseUri, this.state)
    )
}

internal fun Throwable.toGetPresentationRequestError(uri: URI): GetPresentationRequestError {
    // do not log this to dynatrace
    Timber.d("Invalid uri: $uri")
    return InvalidUri
}

internal fun ValidatePresentationRequestError.toGetPresentationRequestError(): GetPresentationRequestError = when (this) {
    is CredentialPresentationError.InvalidRequest -> InvalidPresentation(responseUri, state)
    is CredentialPresentationError.InvalidClient -> InvalidClientPresentation(responseUri, state)
    is CredentialPresentationError.InvalidTransactionData -> InvalidTransactionData(responseUri, state)
    is CredentialPresentationError.UnknownVerifier -> UnknownVerifier
    is CredentialPresentationError.UnverifiedVerifier -> InvitationError.UnverifiedVerifier(responseUri, state)
    is CredentialPresentationError.UnknownRegistry -> InvitationError.UnknownRegistry(responseUri, state)
    is CredentialPresentationError.NetworkError -> NetworkError
    is CredentialPresentationError.Unexpected -> Unexpected
}
//endregion

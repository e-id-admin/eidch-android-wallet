@file:Suppress("TooManyFunctions")

package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model

import ch.admin.foitt.openid4vc.domain.model.GetKeyPairError
import ch.admin.foitt.openid4vc.domain.model.KeyPairError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CreateDPoPProofJwtError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.AttestationError
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.GenerateProofOfPossessionError
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.RequestClientAttestationError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestError.InsufficientKeyStorageResistance
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestError.InvalidClientAttestation
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestError.InvalidDeferredCredentialOffer
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestError.InvalidKeyAttestation
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestError.NetworkError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestError.RequestInWrongState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestError.Unexpected
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError
import ch.admin.foitt.wallet.platform.invitation.domain.model.ProcessInvitationError
import ch.admin.foitt.wallet.platform.utils.JsonError
import ch.admin.foitt.wallet.platform.utils.JsonParsingError
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.getOrElse
import io.ktor.client.call.body
import io.ktor.client.plugins.ClientRequestException
import io.ktor.http.HttpStatusCode
import timber.log.Timber
import java.io.IOException

interface EIdRequestError {
    data object NetworkError :
        SIdRepositoryError,
        ValidateAttestationsError,
        ApplyRequestError,
        StateRequestError,
        GuardianVerificationError,
        StartOnlineSessionError,
        PairWalletError,
        PairCurrentWalletError,
        StartAutoVerificationError,
        AvRepositoryError,
        AvUploadFilesError,
        AvSubmitCaseError,
        WalletPairingStateError,
        SetEIdPeerPushIdError,
        AbortSIdProcessError,
        VerifyWalletPairingError,
        SIdAPIError

    data object InvalidClientAttestation :
        ValidateAttestationsError,
        ApplyRequestError,
        GuardianVerificationError,
        StateRequestError,
        PairWalletError,
        PairCurrentWalletError,
        StartAutoVerificationError,
        StartOnlineSessionError,
        WalletPairingStateError,
        SIdAPIError

    data object InvalidKeyAttestation : ValidateAttestationsError, SIdAPIError
    data object InsufficientKeyStorageResistance : ValidateAttestationsError, SIdAPIError
    data class DeclinedProcessData(val cause: String?) :
        AvRepositoryError,
        AvUploadFilesError,
        AvSubmitCaseError
    data class FileNotFound(val fileName: String) : AvUploadFilesError

    data object InvalidDeferredCredentialOffer : PairCurrentWalletError

    data object RequestInWrongState : SIdAPIError, PairWalletError, PairCurrentWalletError

    data object UnauthorizedPairing : VerifyWalletPairingError, StartAutoVerificationError

    data class Unexpected(val cause: Throwable?) :
        EIdRequestCaseRepositoryError,
        EIdRequestStateRepositoryError,
        EIdRequestCaseWithStateRepositoryError,
        SIdRepositoryError,
        ApplyRequestError,
        StateRequestError,
        GuardianVerificationError,
        ValidateAttestationsError,
        EIdRequestFileRepositoryError,
        StartOnlineSessionError,
        PairWalletError,
        PairCurrentWalletError,
        StartAutoVerificationError,
        AvRepositoryError,
        AvUploadFilesError,
        AvSubmitCaseError,
        WalletPairingStateError,
        SetEIdPeerPushIdError,
        AbortSIdProcessError,
        VerifyWalletPairingError,
        SIdAPIError,
        CreateAutoVerificationDPoPError
}

sealed interface EIdRequestCaseRepositoryError
sealed interface EIdRequestStateRepositoryError
sealed interface EIdRequestCaseWithStateRepositoryError
sealed interface ApplyRequestError
sealed interface StateRequestError
sealed interface GuardianVerificationError
sealed interface SIdRepositoryError
sealed interface ValidateAttestationsError
sealed interface EIdRequestFileRepositoryError
sealed interface StartOnlineSessionError
sealed interface PairWalletError
sealed interface PairCurrentWalletError
sealed interface StartAutoVerificationError
sealed interface AvRepositoryError
sealed interface AvUploadFilesError
sealed interface AvSubmitCaseError
sealed interface WalletPairingStateError
sealed interface SetEIdPeerPushIdError
sealed interface SIdAPIError
sealed interface CreateAutoVerificationDPoPError
sealed interface AbortSIdProcessError
sealed interface VerifyWalletPairingError : StartAutoVerificationError

internal fun SIdRepositoryError.toAbortSIdProcessError(): AbortSIdProcessError = when (this) {
    is NetworkError -> this
    is Unexpected -> this
}

internal fun RequestClientAttestationError.toAbortSIdProcessError(): AbortSIdProcessError = when (this) {
    is AttestationError.NetworkError -> NetworkError
    is AttestationError.SocketTimeoutError -> NetworkError
    is AttestationError.Unexpected -> Unexpected(throwable)
    is AttestationError.ValidationError -> Unexpected(Exception(message))
}

internal fun GenerateProofOfPossessionError.toAbortSIdProcessError(): AbortSIdProcessError = when (this) {
    is AttestationError.Unexpected -> Unexpected(throwable)
}

internal fun EIdRequestCaseRepositoryError.toVerifyWalletPairingError(): VerifyWalletPairingError = when (this) {
    is Unexpected -> this
}

internal fun SIdRepositoryError.toStartOnlineSessionError(): StartOnlineSessionError = when (this) {
    is Unexpected -> this
    is NetworkError -> this
}

internal fun RequestClientAttestationError.toStartOnlineSessionError(): StartOnlineSessionError = when (this) {
    is AttestationError.NetworkError -> NetworkError
    is AttestationError.ValidationError -> InvalidClientAttestation
    AttestationError.SocketTimeoutError -> Unexpected(null)
    is AttestationError.Unexpected -> Unexpected(throwable)
}

internal fun SIdRepositoryError.toApplyRequestError(): ApplyRequestError = when (this) {
    is Unexpected -> this
    is NetworkError -> this
}

internal fun SIdRepositoryError.toStartAutoVerificationError(): StartAutoVerificationError = when (this) {
    is Unexpected -> this
    is NetworkError -> this
}

internal fun RequestClientAttestationError.toStartAutoVerificationError(): StartAutoVerificationError = when (this) {
    is AttestationError.NetworkError -> NetworkError
    is AttestationError.ValidationError -> InvalidClientAttestation
    AttestationError.SocketTimeoutError -> Unexpected(null)
    is AttestationError.Unexpected -> Unexpected(throwable)
}

internal fun SIdRepositoryError.toPairWalletError(): PairWalletError = when (this) {
    is Unexpected -> this
    is NetworkError -> this
}

internal fun SIdRepositoryError.toWalletPairingStateError(): WalletPairingStateError = when (this) {
    is Unexpected -> this
    is NetworkError -> this
}

internal fun SIdRepositoryError.toSetEIdPeerPushIdError(): SetEIdPeerPushIdError = when (this) {
    NetworkError -> NetworkError
    is Unexpected -> Unexpected(cause)
}

internal fun RequestClientAttestationError.toSetEIdPeerPushIdError(): SetEIdPeerPushIdError = when (this) {
    AttestationError.NetworkError -> NetworkError
    is AttestationError.ValidationError -> Unexpected(null)
    AttestationError.SocketTimeoutError -> Unexpected(null)
    is AttestationError.Unexpected -> Unexpected(throwable)
}

internal fun GenerateProofOfPossessionError.toSetEIdPeerPushIdError(): SetEIdPeerPushIdError = when (this) {
    is AttestationError.Unexpected -> Unexpected(throwable)
}

internal fun JsonParsingError.toSetEIdPeerPushIdError(): SetEIdPeerPushIdError = when (this) {
    is JsonError.Unexpected -> Unexpected(throwable)
}

internal fun RequestClientAttestationError.toPairWalletError(): PairWalletError = when (this) {
    is AttestationError.NetworkError -> NetworkError
    is AttestationError.ValidationError -> InvalidClientAttestation
    AttestationError.SocketTimeoutError -> Unexpected(null)
    is AttestationError.Unexpected -> Unexpected(throwable)
}

internal fun RequestClientAttestationError.toWalletPairingStateError(): WalletPairingStateError = when (this) {
    is AttestationError.NetworkError -> NetworkError
    is AttestationError.ValidationError -> InvalidClientAttestation
    AttestationError.SocketTimeoutError -> Unexpected(null)
    is AttestationError.Unexpected -> Unexpected(throwable)
}

internal fun EIdRequestCaseRepositoryError.toPairCurrentWalletError(): PairCurrentWalletError = when (this) {
    is Unexpected -> this
}

internal fun PairWalletError.toPairCurrentWalletError(): PairCurrentWalletError = when (this) {
    is InvalidClientAttestation -> this
    is NetworkError -> this
    is RequestInWrongState -> this
    is Unexpected -> this
}

internal fun ProcessInvitationError.toPairCurrentWalletError(): PairCurrentWalletError = when (this) {
    InvitationError.NetworkError -> NetworkError
    InvitationError.CredentialOfferExpired,
    is InvitationError.EmptyWallet,
    InvitationError.IncompatibleDeviceKeyStorage,
    InvitationError.InvalidCredentialOffer,
    InvitationError.InvalidInput,
    is InvitationError.InvalidPresentation,
    is InvitationError.InvalidClientPresentation,
    is InvitationError.InvalidTransactionData,
    InvitationError.InvalidPresentationRequest,
    is InvitationError.NoCompatibleCredential,
    is InvitationError.Unexpected,
    InvitationError.UnknownIssuer,
    InvitationError.UnknownVerifier,
    is InvitationError.MetadataMisconfiguration,
    InvitationError.CredentialRequestDenied,
    InvitationError.InsufficientScope,
    InvitationError.InvalidCredentialOffer,
    InvitationError.InvalidCredentialRequest,
    InvitationError.InvalidEncryptionParameters,
    InvitationError.InvalidClient,
    InvitationError.InvalidNonce,
    InvitationError.InvalidProof,
    InvitationError.InvalidRequest,
    InvitationError.InvalidToken,
    InvitationError.InvalidRequestBearerToken,
    InvitationError.UnauthorizedClient,
    InvitationError.UnauthorizedGrantType,
    InvitationError.UnknownCredentialConfiguration,
    InvitationError.UnknownCredentialIdentifier,
    InvitationError.UnsupportedKeyStorageSecurityLevel -> InvalidDeferredCredentialOffer
    InvitationError.UnverifiedIssuer,
    is InvitationError.UnverifiedVerifier,
    InvitationError.UnauthorizedIssuance,
    is InvitationError.UnknownRegistry -> InvalidDeferredCredentialOffer
}

internal fun AvRepositoryError.toAvUploadFilesError(): AvUploadFilesError = when (this) {
    is Unexpected -> this
    is EIdRequestError.DeclinedProcessData -> this
    is NetworkError -> this
}

internal fun AvRepositoryError.toAvSubmitCaseError(): AvSubmitCaseError = when (this) {
    is Unexpected -> this
    is EIdRequestError.DeclinedProcessData -> this
    is NetworkError -> this
}

internal fun EIdRequestCaseRepositoryError.toAvSubmitCaseError(): AvSubmitCaseError = when (this) {
    is Unexpected -> this
}

internal fun EIdRequestFileRepositoryError.toAvSubmitCaseError(): AvSubmitCaseError = when (this) {
    is Unexpected -> this
}

internal fun JsonParsingError.toAvSubmitCaseError(): AvSubmitCaseError = when (this) {
    is JsonError.Unexpected -> Unexpected(throwable)
}

internal fun RequestClientAttestationError.toApplyRequestError(): ApplyRequestError = when (this) {
    is AttestationError.NetworkError -> NetworkError
    is AttestationError.ValidationError -> InvalidClientAttestation
    AttestationError.SocketTimeoutError -> Unexpected(null)
    is AttestationError.Unexpected -> Unexpected(throwable)
}

internal fun GenerateProofOfPossessionError.toApplyRequestError(): ApplyRequestError = when (this) {
    is AttestationError.Unexpected -> Unexpected(this.throwable)
}

internal fun GenerateProofOfPossessionError.toStartOnlineSessionError(): StartOnlineSessionError = when (this) {
    is AttestationError.Unexpected -> Unexpected(this.throwable)
}

internal fun GenerateProofOfPossessionError.toPairWalletError(): PairWalletError = when (this) {
    is AttestationError.Unexpected -> Unexpected(this.throwable)
}

internal fun GenerateProofOfPossessionError.toStartAutoVerificationError(): StartAutoVerificationError = when (this) {
    is AttestationError.Unexpected -> Unexpected(this.throwable)
}

internal fun RequestClientAttestationError.toStateRequestError(): StateRequestError = when (this) {
    is AttestationError.NetworkError -> NetworkError
    is AttestationError.ValidationError -> InvalidClientAttestation
    AttestationError.SocketTimeoutError -> Unexpected(null)
    is AttestationError.Unexpected -> Unexpected(throwable)
}

internal fun SIdRepositoryError.toStateRequestError(): StateRequestError = when (this) {
    is Unexpected -> this
    is NetworkError -> this
}

internal fun RequestClientAttestationError.toGuardianVerificationError(): GuardianVerificationError = when (this) {
    is AttestationError.NetworkError -> NetworkError
    is AttestationError.ValidationError -> InvalidClientAttestation
    AttestationError.SocketTimeoutError -> Unexpected(null)
    is AttestationError.Unexpected -> Unexpected(throwable)
}

internal fun SIdRepositoryError.toGuardianVerificationError(): GuardianVerificationError = when (this) {
    is Unexpected -> this
    is NetworkError -> this
}

internal fun EIdRequestStateRepositoryError.toUpdateEIdRequestStateError() = when (this) {
    is Unexpected -> this
}

internal fun JsonParsingError.toApplyRequestError(): ApplyRequestError = when (this) {
    is JsonError.Unexpected -> Unexpected(this.throwable)
}

internal fun Throwable.toEIdRequestCaseRepositoryError(message: String): EIdRequestCaseRepositoryError {
    Timber.e(t = this, message = message)
    return Unexpected(this)
}

internal fun Throwable.toEIdRequestStateRepositoryError(message: String): EIdRequestStateRepositoryError {
    Timber.e(t = this, message = message)
    return Unexpected(this)
}

internal fun Throwable.toEIdRequestCaseWithStateRepositoryError(message: String): EIdRequestCaseWithStateRepositoryError {
    Timber.e(t = this, message = message)
    return Unexpected(this)
}

internal fun Throwable.toEIdRequestFileError(message: String): EIdRequestFileRepositoryError {
    Timber.e(t = this, message = message)
    return Unexpected(this)
}

internal fun Throwable.toSIdRepositoryError(message: String): SIdRepositoryError {
    Timber.e(t = this, message = message)
    return when (this) {
        is IOException -> NetworkError
        else -> Unexpected(this)
    }
}

internal fun Throwable.toAvRepositoryError(message: String): AvRepositoryError {
    Timber.e(t = this, message = message)
    return when (this) {
        is IOException -> NetworkError
        else -> Unexpected(this)
    }
}

internal suspend fun Throwable.toSIdClientError(message: String): SIdAPIError {
    Timber.e(t = this, message = message)
    return when (this) {
        is ClientRequestException -> this.toSIdClientError()
        is IOException -> NetworkError
        else -> Unexpected(this)
    }
}

private suspend fun ClientRequestException.toSIdClientError(): SIdAPIError {
    val statusCode = this.response.status.value
    val errors = runSuspendCatching { this.response.body<SIdErrorResponse>() }
        .getOrElse {
            return Unexpected(this)
        }

    return when (statusCode) {
        HttpStatusCode.UnprocessableEntity.value -> {
            when {
                errors.contains(SIdError.INSUFFICIENT_KEY_STORAGE_RESISTANCE) -> InsufficientKeyStorageResistance
                errors.contains(SIdError.INVALID_KEY_ATTESTATION) -> InvalidKeyAttestation
                errors.contains(SIdError.INVALID_CLIENT_ATTESTATION) -> InvalidClientAttestation
                else -> Unexpected(this)
            }
        }
        HttpStatusCode.BadRequest.value -> {
            when {
                errors.contains(SIdError.REQUEST_IN_WRONG_STATE) -> RequestInWrongState
                else -> Unexpected(this)
            }
        }
        HttpStatusCode.Unauthorized.value -> {
            when {
                errors.contains(SIdError.INVALID_KEY_ATTESTATION) -> InvalidKeyAttestation
                errors.contains(SIdError.INVALID_CLIENT_ATTESTATION) -> InvalidClientAttestation
                else -> Unexpected(this)
            }
        }
        else -> Unexpected(this)
    }
}

suspend fun Throwable.toValidateAttestationsError(message: String): ValidateAttestationsError = when (
    val apiError = this.toSIdClientError(message)
) {
    is InsufficientKeyStorageResistance -> apiError
    is InvalidClientAttestation -> apiError
    is InvalidKeyAttestation -> apiError
    is RequestInWrongState -> Unexpected(IllegalStateException("Request in wrong state"))
    is Unexpected -> apiError
    is NetworkError -> apiError
}

suspend fun Throwable.toPairWalletError(): PairWalletError = when (
    val apiError = this.toSIdClientError("pairWallet error")
) {
    is InsufficientKeyStorageResistance -> Unexpected(IllegalStateException("Insufficient key storage resistance"))
    is InvalidClientAttestation -> apiError
    is InvalidKeyAttestation -> Unexpected(IllegalStateException("Invalid key attestation"))
    is RequestInWrongState -> apiError
    is NetworkError -> apiError
    is Unexpected -> apiError
}

internal fun CreateAutoVerificationDPoPError.toAvRepositoryError(): AvRepositoryError = when (this) {
    is Unexpected -> this
}

internal fun CreateAutoVerificationDPoPError.toAvSubmitCaseError(): AvSubmitCaseError = when (this) {
    is Unexpected -> this
}

internal fun GetKeyPairError.toCreateAutoVerificationDPoPError(): CreateAutoVerificationDPoPError = when (this) {
    is KeyPairError.NotFound -> {
        val throwable = IllegalStateException("DPoP: get key pair failed with not found")
        Timber.e(t = throwable)
        Unexpected(throwable)
    }
    is KeyPairError.Unexpected -> Unexpected(cause = throwable)
}

internal fun CreateDPoPProofJwtError.toCreateAutoVerificationDPoPError(): CreateAutoVerificationDPoPError = when (this) {
    is CredentialOfferError.Unexpected -> Unexpected(cause)
    is CredentialOfferError.UnsupportedCryptographicSuite -> {
        val throwable = Exception("DPoP: unsupported cryptographic suite")
        Timber.e(t = throwable)
        Unexpected(throwable)
    }
}

private fun SIdErrorResponse.contains(errorCode: String) = errors.any { error -> error.code == errorCode }

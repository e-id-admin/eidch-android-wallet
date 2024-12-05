package ch.admin.foitt.wallet.platform.credentialPresentation.domain.model

import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtError
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VerifyJwtError
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.GetAnyCredentialsError
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import timber.log.Timber

internal interface CredentialPresentationError {
    data object EmptyWallet : ProcessPresentationRequestError
    data object NoCompatibleCredential : ProcessPresentationRequestError
    data class InvalidPresentation(val responseUri: String) :
        ProcessPresentationRequestError,
        ValidatePresentationRequestError
    data class Unexpected(val cause: Throwable?) :
        ProcessPresentationRequestError,
        GetCompatibleCredentialsError,
        GetRequestedFieldsError
}

sealed interface ProcessPresentationRequestError
sealed interface ValidatePresentationRequestError
sealed interface GetCompatibleCredentialsError
sealed interface GetRequestedFieldsError

internal fun ValidatePresentationRequestError.toProcessPresentationRequestError(): ProcessPresentationRequestError = when (this) {
    is CredentialPresentationError.InvalidPresentation -> this
}

internal fun CredentialRepositoryError.toProcessPresentationRequestError(): ProcessPresentationRequestError = when (this) {
    is SsiError.Unexpected -> CredentialPresentationError.Unexpected(cause)
}

internal fun GetCompatibleCredentialsError.toProcessPresentationRequestError(): ProcessPresentationRequestError = when (this) {
    is CredentialPresentationError.Unexpected -> this
}

internal fun GetRequestedFieldsError.toGetCompatibleCredentialsError(): GetCompatibleCredentialsError = when (this) {
    is CredentialPresentationError.Unexpected -> this
}

internal fun GetAnyCredentialsError.toGetCompatibleCredentialsError(): GetCompatibleCredentialsError = when (this) {
    is CredentialError.Unexpected -> CredentialPresentationError.Unexpected(cause)
}

internal fun Throwable.toGetCompatibleCredentialsError(): GetCompatibleCredentialsError {
    Timber.e(this)
    return CredentialPresentationError.Unexpected(this)
}

internal fun Throwable.toGetRequestedFieldsError(): GetRequestedFieldsError {
    Timber.e(this)
    return CredentialPresentationError.Unexpected(this)
}

internal fun VerifyJwtError.toValidatePresentationRequestError(responseUri: String): ValidatePresentationRequestError = when (this) {
    VcSdJwtError.NetworkError,
    VcSdJwtError.InvalidJwt,
    is VcSdJwtError.Unexpected -> CredentialPresentationError.InvalidPresentation(responseUri)
}
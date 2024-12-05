@file:Suppress("TooManyFunctions")

package ch.admin.foitt.wallet.feature.presentationRequest.domain.model

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.SubmitAnyCredentialPresentationError
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.GetAnyCredentialError
import ch.admin.foitt.wallet.platform.ssi.domain.model.GetCredentialClaimDataError
import ch.admin.foitt.wallet.platform.ssi.domain.model.GetCredentialClaimsError
import ch.admin.foitt.wallet.platform.ssi.domain.model.MapToCredentialClaimDataError
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import timber.log.Timber
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestError as OpenIdPresentationRequestError

interface PresentationRequestError {
    data object RawSdJwtParsingError : GeneratePresentationMetadataError, SubmitPresentationError
    data object ValidationError : SubmitPresentationError
    data object InvalidUrl : SubmitPresentationError
    data object NetworkError : SubmitPresentationError
    data class Unexpected(val throwable: Throwable?) :
        GeneratePresentationMetadataError,
        SubmitPresentationError,
        PresentationRequestRepositoryError,
        GetPresentationRequestFlowError,
        GetPresentationRequestCredentialListFlowError
}

sealed interface GeneratePresentationMetadataError
sealed interface SubmitPresentationError
sealed interface PresentationRequestRepositoryError
sealed interface GetPresentationRequestFlowError
sealed interface GetPresentationRequestCredentialListFlowError

fun GetAnyCredentialError.toSubmitPresentationError(): SubmitPresentationError = when (this) {
    is CredentialError.Unexpected -> PresentationRequestError.Unexpected(cause)
}

fun SubmitAnyCredentialPresentationError.toSubmitPresentationError(): SubmitPresentationError = when (this) {
    OpenIdPresentationRequestError.NetworkError -> PresentationRequestError.NetworkError
    OpenIdPresentationRequestError.ValidationError -> PresentationRequestError.ValidationError
    is OpenIdPresentationRequestError.Unexpected -> PresentationRequestError.Unexpected(throwable)
}

fun Throwable.toPresentationRequestRepositoryError(): PresentationRequestRepositoryError {
    Timber.e(this)
    return PresentationRequestError.Unexpected(this)
}

fun GetCredentialClaimsError.toGeneratePresentationMetadataError(): GeneratePresentationMetadataError = when (this) {
    is SsiError.Unexpected -> PresentationRequestError.Unexpected(cause)
}

fun GetCredentialClaimDataError.toGeneratePresentationMetadataError(): GeneratePresentationMetadataError = when (this) {
    is SsiError.Unexpected -> PresentationRequestError.Unexpected(cause)
}

fun PresentationRequestRepositoryError.toGetPresentationRequestFlowError(): GetPresentationRequestFlowError = when (this) {
    is PresentationRequestError.Unexpected -> this
}

fun MapToCredentialClaimDataError.toGetPresentationRequestFlowError(): GetPresentationRequestFlowError = when (this) {
    is SsiError.Unexpected -> PresentationRequestError.Unexpected(cause)
}

fun PresentationRequestRepositoryError.toGetPresentationRequestCredentialListFlowError(): GetPresentationRequestCredentialListFlowError =
    when (this) {
        is PresentationRequestError.Unexpected -> this
    }

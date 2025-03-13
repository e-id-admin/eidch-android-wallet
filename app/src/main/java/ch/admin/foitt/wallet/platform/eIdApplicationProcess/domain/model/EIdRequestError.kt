package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model

import timber.log.Timber

interface EIdRequestError {
    data class Unexpected(val cause: Throwable?) :
        SaveEIdRequestCaseError,
        SaveEIdRequestStateError,
        EIdRequestCaseRepositoryError,
        EIdRequestStateRepositoryError,
        EIdRequestCaseWithStateRepositoryError,
        GetEIdRequestsError,
        StateRequestError,
        ApplyRequestError
}

sealed interface SaveEIdRequestCaseError
sealed interface SaveEIdRequestStateError
sealed interface EIdRequestCaseRepositoryError
sealed interface EIdRequestStateRepositoryError
sealed interface EIdRequestCaseWithStateRepositoryError
sealed interface GetEIdRequestsError
sealed interface StateRequestError
sealed interface ApplyRequestError

internal fun EIdRequestCaseRepositoryError.toSaveEIdRequestCaseError() = when (this) {
    is EIdRequestError.Unexpected -> this
}

internal fun EIdRequestStateRepositoryError.toSaveEIdRequestStateError() = when (this) {
    is EIdRequestError.Unexpected -> this
}

internal fun EIdRequestStateRepositoryError.toUpdateEIdRequestStateError() = when (this) {
    is EIdRequestError.Unexpected -> this
}

internal fun Throwable.toEIdRequestCaseRepositoryError(): EIdRequestCaseRepositoryError {
    Timber.e(this)
    return EIdRequestError.Unexpected(this)
}

internal fun Throwable.toEIdRequestStateRepositoryError(): EIdRequestStateRepositoryError {
    Timber.e(this)
    return EIdRequestError.Unexpected(this)
}

internal fun Throwable.toEIdRequestCaseWithStateRepositoryError(): EIdRequestCaseWithStateRepositoryError {
    Timber.e(this)
    return EIdRequestError.Unexpected(this)
}

internal fun EIdRequestCaseWithStateRepositoryError.toGetEIdRequestsError() = when (this) {
    is EIdRequestError.Unexpected -> this
}

internal fun Throwable.toFetchSIdStateError(): StateRequestError {
    Timber.e(this)
    return EIdRequestError.Unexpected(this)
}

internal fun Throwable.toFetchSIdCaseError(): ApplyRequestError {
    Timber.e(this)
    return EIdRequestError.Unexpected(this)
}

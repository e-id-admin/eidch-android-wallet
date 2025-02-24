package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model

import timber.log.Timber

interface EIdRequestError {
    data class Unexpected(val cause: Throwable?) :
        SaveEIdRequestCaseError,
        SaveEIdRequestStateError,
        EIdRequestCaseRepositoryError,
        EIdRequestStateRepositoryError
}

sealed interface SaveEIdRequestCaseError
sealed interface SaveEIdRequestStateError
sealed interface EIdRequestCaseRepositoryError
sealed interface EIdRequestStateRepositoryError

internal fun EIdRequestCaseRepositoryError.toSaveEIdRequestCaseError() = when (this) {
    is EIdRequestError.Unexpected -> this
}

internal fun EIdRequestStateRepositoryError.toSaveEIdRequestStateError() = when (this) {
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

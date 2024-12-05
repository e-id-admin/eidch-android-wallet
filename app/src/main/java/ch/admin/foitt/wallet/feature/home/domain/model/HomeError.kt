package ch.admin.foitt.wallet.feature.home.domain.model

import timber.log.Timber

internal interface HomeError {
    data class Unexpected(val throwable: Throwable?) :
        HomeRepositoryError,
        GetHomeDataFlowError
}
sealed interface GetHomeDataFlowError
sealed interface HomeRepositoryError

internal fun Throwable.toHomeRepositoryError(): HomeRepositoryError {
    Timber.e(this)
    return HomeError.Unexpected(this)
}

internal fun HomeRepositoryError.toGetHomeDataFlowError(): GetHomeDataFlowError = when (this) {
    is HomeError.Unexpected -> this
}

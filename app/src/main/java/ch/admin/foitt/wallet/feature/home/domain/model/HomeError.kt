package ch.admin.foitt.wallet.feature.home.domain.model

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestCaseRepositoryError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestCaseWithStateRepositoryError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestError

internal interface HomeError {
    data class Unexpected(val throwable: Throwable?) :
        GetEIdRequestsFlowError,
        DeleteEIdRequestCaseError
}

sealed interface GetEIdRequestsFlowError
sealed interface DeleteEIdRequestCaseError

internal fun EIdRequestCaseWithStateRepositoryError.toGetEIdRequestsFlowError() = when (this) {
    is EIdRequestError.Unexpected -> HomeError.Unexpected(cause)
}

internal fun EIdRequestCaseRepositoryError.toDeleteEIdRequestCaseError(): DeleteEIdRequestCaseError = when (this) {
    is EIdRequestError.Unexpected -> HomeError.Unexpected(cause)
}

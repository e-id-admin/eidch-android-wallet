package ch.admin.foitt.wallet.feature.credentialDetail.domain.model

import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.MapToCredentialClaimDataError
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError

internal interface CredentialDetailError {
    data class Unexpected(val throwable: Throwable?) :
        GetCredentialDetailFlowError
}

sealed interface GetCredentialDetailFlowError

internal fun CredentialRepositoryError.toGetCredentialDetailFlowError(): GetCredentialDetailFlowError = when (this) {
    is SsiError.Unexpected -> CredentialDetailError.Unexpected(cause)
}

fun MapToCredentialClaimDataError.toGetCredentialDetailFlowError(): GetCredentialDetailFlowError = when (this) {
    is SsiError.Unexpected -> CredentialDetailError.Unexpected(cause)
}

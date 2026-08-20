package ch.admin.foitt.wallet.feature.credentialDetail.domain.model

import ch.admin.foitt.wallet.platform.credentialRefresh.domain.model.CredentialRefreshDataError
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialIssuerDisplayRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError

internal interface CredentialDetailError {
    data class Unexpected(val throwable: Throwable?) :
        GetCredentialIssuerDisplaysFlowError,
        GetCredentialIssuanceTypeError
}

sealed interface GetCredentialIssuerDisplaysFlowError
sealed interface GetCredentialIssuanceTypeError

fun CredentialIssuerDisplayRepositoryError.toGetCredentialIssuerDisplaysFlowError(): GetCredentialIssuerDisplaysFlowError = when (this) {
    is SsiError.Unexpected -> CredentialDetailError.Unexpected(cause)
}

fun CredentialRefreshDataError.toGetCredentialIssuanceTypeError(): GetCredentialIssuanceTypeError = when (this) {
    is CredentialRefreshDataError.Unexpected -> CredentialDetailError.Unexpected(cause)
}

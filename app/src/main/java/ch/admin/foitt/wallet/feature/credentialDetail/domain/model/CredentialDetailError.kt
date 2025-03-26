package ch.admin.foitt.wallet.feature.credentialDetail.domain.model

import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialIssuerDisplayRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError

internal interface CredentialDetailError {
    data class Unexpected(val throwable: Throwable?) :
        GetCredentialIssuerDisplaysFlowError
}

sealed interface GetCredentialIssuerDisplaysFlowError

fun CredentialIssuerDisplayRepositoryError.toGetCredentialIssuerDisplaysFlowError(): GetCredentialIssuerDisplaysFlowError = when (this) {
    is SsiError.Unexpected -> CredentialDetailError.Unexpected(cause)
}

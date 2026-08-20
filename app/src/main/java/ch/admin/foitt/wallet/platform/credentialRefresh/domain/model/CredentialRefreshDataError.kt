package ch.admin.foitt.wallet.platform.credentialRefresh.domain.model

import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError

sealed interface CredentialRefreshDataError {
    data class Unexpected(val cause: Throwable?) : CredentialRefreshDataError

    fun toFetchCredentialError() = when (this) {
        is Unexpected -> CredentialError.Unexpected(cause)
    }
}

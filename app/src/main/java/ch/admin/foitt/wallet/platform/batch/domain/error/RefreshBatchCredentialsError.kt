package ch.admin.foitt.wallet.platform.batch.domain.error

import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchCredentialError
import ch.admin.foitt.wallet.platform.credentialRefresh.domain.model.CredentialRefreshDataError
import ch.admin.foitt.wallet.platform.ssi.domain.model.BundleItemRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError

sealed interface RefreshBatchCredentialsError {
    class Unexpected(val cause: Throwable?) : RefreshBatchCredentialsError
}

fun CredentialRefreshDataError.toRefreshBatchCredentialsError(): RefreshBatchCredentialsError = when (this) {
    is CredentialRefreshDataError.Unexpected -> RefreshBatchCredentialsError.Unexpected(cause)
}

fun FetchCredentialError.toRefreshBatchCredentialsError(): RefreshBatchCredentialsError = when (this) {
    is CredentialError.Unexpected -> RefreshBatchCredentialsError.Unexpected(cause)
    else -> RefreshBatchCredentialsError.Unexpected(
        IllegalStateException("Batch credential refresh failed: $this")
    )
}

fun BundleItemRepositoryError.toRefreshBatchCredentialsError(): RefreshBatchCredentialsError = when (this) {
    is SsiError.Unexpected -> RefreshBatchCredentialsError.Unexpected(cause)
}

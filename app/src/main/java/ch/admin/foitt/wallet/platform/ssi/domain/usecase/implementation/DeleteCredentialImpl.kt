package ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation

import ch.admin.foitt.openid4vc.utils.Constants.ANDROID_KEY_STORE
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.DeleteCredentialError
import ch.admin.foitt.wallet.platform.ssi.domain.model.toDeleteCredentialError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialRepo
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.DeleteCredential
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.mapError
import timber.log.Timber
import java.security.KeyStore
import javax.inject.Inject

class DeleteCredentialImpl @Inject constructor(
    private val credentialRepo: CredentialRepo
) : DeleteCredential {
    override suspend fun invoke(credentialId: Long): Result<Unit, DeleteCredentialError> = coroutineBinding {
        val credential = credentialRepo.getById(credentialId)
            .mapError(CredentialRepositoryError::toDeleteCredentialError)
            .bind()

        if (credential == null) {
            Timber.w("Try to delete non-existing credential")
            return@coroutineBinding
        }

        if (credential.keyBindingIdentifier != null) {
            deleteKeyStoreEntry(credential.keyBindingIdentifier)
        }

        credentialRepo.deleteById(credentialId)
            .mapError(CredentialRepositoryError::toDeleteCredentialError).bind()
    }

    private fun deleteKeyStoreEntry(keyIdentifier: String) = runSuspendCatching {
        val keyStore = KeyStore.getInstance(ANDROID_KEY_STORE)
        keyStore.load(null)
        keyStore.deleteEntry(keyIdentifier)
    }.getOrElse { throwable ->
        Timber.e(t = throwable, message = "Could not delete key store entry for credential")
    }
}

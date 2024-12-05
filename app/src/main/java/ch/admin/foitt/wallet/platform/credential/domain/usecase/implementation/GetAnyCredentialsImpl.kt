package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.wallet.platform.credential.domain.model.GetAnyCredentialsError
import ch.admin.foitt.wallet.platform.credential.domain.model.toAnyCredential
import ch.admin.foitt.wallet.platform.credential.domain.model.toGetAnyCredentialsError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GetAnyCredentials
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialRepo
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.get
import com.github.michaelbull.result.mapError
import timber.log.Timber
import javax.inject.Inject

internal class GetAnyCredentialsImpl @Inject constructor(
    private val credentialRepository: CredentialRepo,
) : GetAnyCredentials {
    override suspend fun invoke(): Result<List<AnyCredential>, GetAnyCredentialsError> = coroutineBinding {
        val credentials = credentialRepository.getAll()
            .mapError(CredentialRepositoryError::toGetAnyCredentialsError)
            .bind()
        credentials.mapNotNull { credential ->
            runSuspendCatching {
                credential.toAnyCredential()
            }.get() ?: run {
                Timber.e("Could not create AnyCredential from database entry")
                null
            }
        }
    }
}

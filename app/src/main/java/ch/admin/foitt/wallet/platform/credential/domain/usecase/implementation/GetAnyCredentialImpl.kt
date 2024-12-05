package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.wallet.platform.credential.domain.model.GetAnyCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.toAnyCredential
import ch.admin.foitt.wallet.platform.credential.domain.model.toGetAnyCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GetAnyCredential
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialRepo
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class GetAnyCredentialImpl @Inject constructor(
    private val credentialRepository: CredentialRepo,
) : GetAnyCredential {
    override suspend fun invoke(credentialId: Long): Result<AnyCredential?, GetAnyCredentialError> =
        credentialRepository.getById(credentialId)
            .mapError(CredentialRepositoryError::toGetAnyCredentialError)
            .andThen { credential ->
                credential?.let {
                    runSuspendCatching {
                        credential.toAnyCredential()
                    }.mapError(Throwable::toGetAnyCredentialError)
                } ?: Ok(null)
            }
}

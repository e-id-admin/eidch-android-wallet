package ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.anycredential.CredentialValidity
import ch.admin.foitt.wallet.platform.credential.domain.model.GetAnyCredentialError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GetAnyCredential
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialStatusProperties
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.FetchCredentialStatusError
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.UpdateCredentialStatusError
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.toUpdateCredentialStatusError
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.FetchCredentialStatus
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.UpdateCredentialStatus
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import ch.admin.foitt.wallet.platform.di.IoDispatcher
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialRepo
import ch.admin.foitt.wallet.platform.utils.SafeJson
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.get
import com.github.michaelbull.result.mapError
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class UpdateCredentialStatusImpl @Inject constructor(
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val credentialRepository: CredentialRepo,
    private val getAnyCredential: GetAnyCredential,
    private val fetchCredentialStatus: FetchCredentialStatus,
    private val safeJson: SafeJson,
) : UpdateCredentialStatus {

    override suspend fun invoke(credentialId: Long): Result<Unit, UpdateCredentialStatusError> = withContext(ioDispatcher) {
        coroutineBinding {
            val anyCredential = getAnyCredential(credentialId)
                .mapError(GetAnyCredentialError::toUpdateCredentialStatusError)
                .bind()

            if (anyCredential != null) {
                when (anyCredential.validity) {
                    CredentialValidity.EXPIRED -> updateCredentialStatus(credentialId, CredentialStatus.EXPIRED).bind()
                    CredentialValidity.NOT_YET_VALID -> updateCredentialStatus(credentialId, CredentialStatus.UNKNOWN).bind()
                    CredentialValidity.VALID -> checkStatusList(credentialId, anyCredential).bind()
                }
            } else {
                Timber.w("Try to update status of non-existing credential")
            }
        }
    }

    private suspend fun checkStatusList(
        credentialId: Long,
        anyCredential: AnyCredential
    ): Result<Unit, UpdateCredentialStatusError> = coroutineBinding {
        val properties = anyCredential.parseStatusProperties()
        if (properties != null) {
            val status = fetchCredentialStatus(anyCredential, properties)
                .mapError(FetchCredentialStatusError::toUpdateCredentialStatusError)
                .bind()
            if (status != CredentialStatus.UNKNOWN) {
                updateCredentialStatus(credentialId, status).bind()
            }
        } else {
            Timber.w("Credential does not have any status to check")
        }
    }

    private suspend fun updateCredentialStatus(
        credentialId: Long,
        newStatus: CredentialStatus
    ): Result<Int, UpdateCredentialStatusError> =
        credentialRepository.updateStatusByCredentialId(credentialId, newStatus)
            .mapError(CredentialRepositoryError::toUpdateCredentialStatusError)

    private fun AnyCredential.parseStatusProperties() =
        safeJson.safeDecodeStringTo<CredentialStatusProperties>(string = json.toString()).get()
}

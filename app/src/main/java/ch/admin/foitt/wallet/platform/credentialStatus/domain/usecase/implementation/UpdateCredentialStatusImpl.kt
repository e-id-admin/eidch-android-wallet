package ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.anycredential.CredentialValidity
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
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
            val anyCredential: AnyCredential? = getAnyCredential(credentialId)
                .mapError(GetAnyCredentialError::toUpdateCredentialStatusError)
                .bind()

            if (anyCredential == null) {
                Timber.w("Cannot update status of non-existing credential")
                return@coroutineBinding
            }

            if (anyCredential.validity != CredentialValidity.Valid) {
                // No point in getting the status, local validity has precedence for now.
                Timber.d("Try to update status of invalid credential")
                return@coroutineBinding
            }
            checkStatusList(credentialId, anyCredential).bind()
        }
    }

    private suspend fun checkStatusList(
        credentialId: Long,
        anyCredential: AnyCredential
    ): Result<Unit, UpdateCredentialStatusError> = coroutineBinding {
        val issuer = anyCredential.issuer
        val properties = anyCredential.parseStatusProperties()
        if (issuer.isNullOrBlank() || properties == null) {
            Timber.w("Credential does not have an issuer and/or any status to check")
            return@coroutineBinding
        }

        val status = fetchCredentialStatus(issuer, properties)
            .mapError(FetchCredentialStatusError::toUpdateCredentialStatusError)
            .bind()
        if (status != CredentialStatus.UNKNOWN) {
            updateCredentialStatus(credentialId, status).bind()
        }
    }

    private suspend fun updateCredentialStatus(
        credentialId: Long,
        newStatus: CredentialStatus
    ): Result<Int, UpdateCredentialStatusError> =
        credentialRepository.updateStatusByCredentialId(credentialId, newStatus)
            .mapError(CredentialRepositoryError::toUpdateCredentialStatusError)

    private fun AnyCredential.parseStatusProperties() = when (this.format) {
        CredentialFormat.VC_SD_JWT -> {
            (this as VcSdJwtCredential).status?.let {
                safeJson.safeDecodeElementTo<CredentialStatusProperties>(it).get()
            }
        }

        else -> null
    }
}

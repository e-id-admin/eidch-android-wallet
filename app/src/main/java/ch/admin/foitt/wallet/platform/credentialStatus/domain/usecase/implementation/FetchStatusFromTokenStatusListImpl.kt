package ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.FetchStatusFromTokenStatusListError
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.ParseTokenStatusStatusListError
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.TokenStatusListProperties
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.ValidateTokenStatusStatusListError
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.toFetchStatusFromTokenStatusListError
import ch.admin.foitt.wallet.platform.credentialStatus.domain.repository.CredentialStatusRepository
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.FetchStatusFromTokenStatusList
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.ParseTokenStatusList
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.ValidateTokenStatusList
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class FetchStatusFromTokenStatusListImpl @Inject constructor(
    private val credentialStatusRepository: CredentialStatusRepository,
    private val validateTokenStatusList: ValidateTokenStatusList,
    private val parseTokenStatusList: ParseTokenStatusList,
) : FetchStatusFromTokenStatusList {

    override suspend fun invoke(
        anyCredential: AnyCredential,
        statusProperties: TokenStatusListProperties,
    ): Result<CredentialStatus, FetchStatusFromTokenStatusListError> = coroutineBinding {
        val status = statusProperties.status ?: return@coroutineBinding CredentialStatus.UNKNOWN

        val jwt = credentialStatusRepository.fetchTokenStatusListJwt(status.statusList.uri).bind()

        val response = validateTokenStatusList(anyCredential, jwt, status.statusList.uri)
            .mapError(ValidateTokenStatusStatusListError::toFetchStatusFromTokenStatusListError)
            .bind()

        val value = parseTokenStatusList(statusList = response.statusList, index = status.statusList.index)
            .mapError(ParseTokenStatusStatusListError::toFetchStatusFromTokenStatusListError)
            .bind()

        mapStatus(value)
    }

    private fun mapStatus(status: Int): CredentialStatus {
        return when (status) {
            0 -> CredentialStatus.VALID
            1 -> CredentialStatus.REVOKED
            2 -> CredentialStatus.SUSPENDED
            else -> CredentialStatus.UNSUPPORTED
        }
    }
}

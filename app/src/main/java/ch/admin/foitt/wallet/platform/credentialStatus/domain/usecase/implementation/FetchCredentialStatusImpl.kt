package ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialStatusProperties
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.FetchCredentialStatusError
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.FetchStatusFromTokenStatusListError
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.TokenStatusListProperties
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.toFetchCredentialStatusError
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.FetchCredentialStatus
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.FetchStatusFromTokenStatusList
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class FetchCredentialStatusImpl @Inject constructor(
    private val fetchStatusFromTokenStatusList: FetchStatusFromTokenStatusList,
) : FetchCredentialStatus {
    override suspend fun invoke(
        credentialIssuer: String,
        properties: CredentialStatusProperties,
    ): Result<CredentialStatus, FetchCredentialStatusError> = when (properties) {
        is TokenStatusListProperties -> fetchStatusFromTokenStatusList(credentialIssuer, properties)
            .mapError(FetchStatusFromTokenStatusListError::toFetchCredentialStatusError)
    }
}

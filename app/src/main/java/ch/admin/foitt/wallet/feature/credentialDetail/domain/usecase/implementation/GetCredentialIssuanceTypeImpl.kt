package ch.admin.foitt.wallet.feature.credentialDetail.domain.usecase.implementation

import ch.admin.foitt.wallet.feature.credentialDetail.domain.model.GetCredentialIssuanceTypeError
import ch.admin.foitt.wallet.feature.credentialDetail.domain.model.IssuanceType
import ch.admin.foitt.wallet.feature.credentialDetail.domain.model.toGetCredentialIssuanceTypeError
import ch.admin.foitt.wallet.feature.credentialDetail.domain.usecase.GetCredentialIssuanceType
import ch.admin.foitt.wallet.platform.credentialRefresh.domain.model.CredentialRefreshDataError
import ch.admin.foitt.wallet.platform.credentialRefresh.domain.repository.CredentialRefreshDataRepository
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class GetCredentialIssuanceTypeImpl @Inject constructor(
    private val credentialRefreshDataRepository: CredentialRefreshDataRepository,
) : GetCredentialIssuanceType {
    override suspend fun invoke(credentialId: Long): Result<IssuanceType, GetCredentialIssuanceTypeError> =
        credentialRefreshDataRepository.getBatchRefreshDataById(credentialId)
            .mapError(CredentialRefreshDataError::toGetCredentialIssuanceTypeError)
            .map { batchRefreshData ->
                val batchSize = batchRefreshData?.batchSize
                if (batchSize != null && batchSize > 1) {
                    IssuanceType.BATCH
                } else {
                    IssuanceType.STANDARD
                }
            }
}

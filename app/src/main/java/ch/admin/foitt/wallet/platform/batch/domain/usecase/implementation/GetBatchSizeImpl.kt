package ch.admin.foitt.wallet.platform.batch.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.BatchSize
import ch.admin.foitt.wallet.platform.batch.domain.usecase.GetBatchSize
import ch.admin.foitt.wallet.platform.credentialRefresh.domain.repository.CredentialRefreshDataRepository
import com.github.michaelbull.result.get
import javax.inject.Inject

class GetBatchSizeImpl @Inject constructor(
    private val credentialRefreshDataRepository: CredentialRefreshDataRepository,
) : GetBatchSize {
    override suspend fun invoke(credentialId: Long): BatchSize =
        credentialRefreshDataRepository.getBatchRefreshDataById(credentialId).get()?.batchSize ?: 0
}

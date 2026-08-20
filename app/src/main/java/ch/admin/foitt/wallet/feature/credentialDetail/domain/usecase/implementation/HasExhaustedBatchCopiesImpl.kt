package ch.admin.foitt.wallet.feature.credentialDetail.domain.usecase.implementation

import ch.admin.foitt.wallet.feature.credentialDetail.domain.model.IssuanceType
import ch.admin.foitt.wallet.feature.credentialDetail.domain.usecase.GetCredentialIssuanceType
import ch.admin.foitt.wallet.feature.credentialDetail.domain.usecase.HasExhaustedBatchCopies
import ch.admin.foitt.wallet.platform.ssi.domain.repository.BundleItemRepository
import com.github.michaelbull.result.get
import javax.inject.Inject

class HasExhaustedBatchCopiesImpl @Inject constructor(
    private val getCredentialIssuanceType: GetCredentialIssuanceType,
    private val bundleItemRepository: BundleItemRepository,
) : HasExhaustedBatchCopies {
    override suspend fun invoke(credentialId: Long): Boolean =
        getCredentialIssuanceType(credentialId).get() == IssuanceType.BATCH &&
            bundleItemRepository.getNeverPresentedCount(credentialId).get() == 0
}

package ch.admin.foitt.wallet.platform.credentialRefresh.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.credentialRefresh.domain.repository.CredentialRefreshDataRepository
import ch.admin.foitt.wallet.platform.credentialRefresh.domain.usecase.IsCredentialRefreshable
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import ch.admin.foitt.wallet.platform.ssi.domain.repository.BundleItemRepository
import com.github.michaelbull.result.get
import javax.inject.Inject

internal class IsCredentialRefreshableImpl @Inject constructor(
    private val credentialRefreshDataRepository: CredentialRefreshDataRepository,
    private val bundleItemRepository: BundleItemRepository,
) : IsCredentialRefreshable {
    override suspend fun invoke(credentialId: Long): Boolean {
        val hasRefreshToken = credentialRefreshDataRepository
            .getCredentialAuthenticationById(credentialId)
            .get()?.refreshToken != null

        return hasRefreshToken && !isRevoked(credentialId)
    }

    private suspend fun isRevoked(credentialId: Long): Boolean = bundleItemRepository
        .getAllByCredentialId(credentialId)
        .get()
        ?.firstOrNull()?.status == CredentialStatus.REVOKED
}

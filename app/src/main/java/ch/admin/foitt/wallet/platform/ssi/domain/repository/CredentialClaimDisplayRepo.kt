package ch.admin.foitt.wallet.platform.ssi.domain.repository

import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimDisplay
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimDisplayRepositoryError
import com.github.michaelbull.result.Result

interface CredentialClaimDisplayRepo {
    suspend fun insertAll(
        credentialClaimDisplays: Collection<CredentialClaimDisplay>
    ): Result<Unit, CredentialClaimDisplayRepositoryError>

    suspend fun getByClaimId(
        claimId: Long,
    ): Result<List<CredentialClaimDisplay>, CredentialClaimDisplayRepositoryError>
}

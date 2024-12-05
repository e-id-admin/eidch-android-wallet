package ch.admin.foitt.wallet.platform.ssi.domain.usecase

import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimDisplay
import ch.admin.foitt.wallet.platform.ssi.domain.model.GetCredentialClaimDisplayError
import com.github.michaelbull.result.Result

interface GetCredentialClaimDisplays {
    suspend operator fun invoke(claimId: Long): Result<List<CredentialClaimDisplay>, GetCredentialClaimDisplayError>
}

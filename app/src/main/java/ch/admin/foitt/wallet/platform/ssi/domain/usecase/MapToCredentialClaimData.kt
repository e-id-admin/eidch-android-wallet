package ch.admin.foitt.wallet.platform.ssi.domain.usecase

import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimWithDisplays
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimData
import ch.admin.foitt.wallet.platform.ssi.domain.model.MapToCredentialClaimDataError
import com.github.michaelbull.result.Result

interface MapToCredentialClaimData {
    suspend operator fun invoke(
        claimWithDisplays: CredentialClaimWithDisplays,
    ): Result<CredentialClaimData, MapToCredentialClaimDataError>
}

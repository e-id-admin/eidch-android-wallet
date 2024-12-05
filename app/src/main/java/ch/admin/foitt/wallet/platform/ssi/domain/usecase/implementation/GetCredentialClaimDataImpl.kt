package ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimData
import ch.admin.foitt.wallet.platform.ssi.domain.model.GetCredentialClaimDataError
import ch.admin.foitt.wallet.platform.ssi.domain.model.GetCredentialClaimDisplayError
import ch.admin.foitt.wallet.platform.ssi.domain.model.MapToCredentialClaimDataError
import ch.admin.foitt.wallet.platform.ssi.domain.model.toGetCredentialClaimDataError
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.GetCredentialClaimData
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.GetCredentialClaimDisplays
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.MapToCredentialClaimData
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class GetCredentialClaimDataImpl @Inject constructor(
    private val getCredentialClaimDisplays: GetCredentialClaimDisplays,
    private val mapToCredentialClaimData: MapToCredentialClaimData,
) : GetCredentialClaimData {
    override suspend fun invoke(credentialClaim: CredentialClaim): Result<CredentialClaimData, GetCredentialClaimDataError> =
        getCredentialClaimDisplays(credentialClaim.id)
            .mapError(GetCredentialClaimDisplayError::toGetCredentialClaimDataError)
            .andThen { displays ->
                mapToCredentialClaimData(credentialClaim, displays)
            }.mapError(MapToCredentialClaimDataError::toGetCredentialClaimDataError)
}

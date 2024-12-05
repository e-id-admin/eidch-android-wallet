package ch.admin.foitt.wallet.platform.ssi.domain.usecase

import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimData
import ch.admin.foitt.wallet.platform.ssi.domain.model.GetCredentialClaimDataError
import com.github.michaelbull.result.Result

fun interface GetCredentialClaimData {
    suspend operator fun invoke(credentialClaim: CredentialClaim): Result<CredentialClaimData, GetCredentialClaimDataError>
}

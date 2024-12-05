package ch.admin.foitt.wallet.platform.ssi.domain.usecase

import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim
import ch.admin.foitt.wallet.platform.ssi.domain.model.GetCredentialClaimsError
import com.github.michaelbull.result.Result

interface GetCredentialClaims {
    suspend operator fun invoke(credentialId: Long): Result<List<CredentialClaim>, GetCredentialClaimsError>
}

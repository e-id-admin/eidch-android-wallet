package ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim
import ch.admin.foitt.wallet.platform.ssi.domain.model.GetCredentialClaimsError
import ch.admin.foitt.wallet.platform.ssi.domain.model.toGetCredentialClaimsError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialClaimRepo
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.GetCredentialClaims
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import javax.inject.Inject

class GetCredentialClaimsImpl @Inject constructor(
    private val credentialClaimRepo: CredentialClaimRepo,
) : GetCredentialClaims {
    override suspend fun invoke(credentialId: Long): Result<List<CredentialClaim>, GetCredentialClaimsError> = coroutineBinding {
        val claims = credentialClaimRepo.getByCredentialId(credentialId)
            .mapError { error ->
                error.toGetCredentialClaimsError()
            }.bind()
        claims
    }
}

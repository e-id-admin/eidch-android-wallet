package ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimDisplay
import ch.admin.foitt.wallet.platform.ssi.domain.model.GetCredentialClaimDisplayError
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.model.toGetCredentialClaimDisplayError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialClaimDisplayRepo
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.GetCredentialClaimDisplays
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.toErrorIfNull
import javax.inject.Inject

class GetCredentialClaimDisplaysImpl @Inject constructor(
    private val credentialClaimDisplayRepo: CredentialClaimDisplayRepo,
) : GetCredentialClaimDisplays {
    override suspend fun invoke(claimId: Long): Result<List<CredentialClaimDisplay>, GetCredentialClaimDisplayError> = coroutineBinding {
        val claims = credentialClaimDisplayRepo.getByClaimId(claimId)
            .mapError { error ->
                error.toGetCredentialClaimDisplayError()
            }.toErrorIfNull {
                SsiError.Unexpected(null)
            }.bind()
        claims
    }
}

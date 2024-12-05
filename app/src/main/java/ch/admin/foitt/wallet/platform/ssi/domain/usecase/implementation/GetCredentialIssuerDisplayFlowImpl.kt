package ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.database.domain.model.CredentialIssuerDisplay
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedDisplay
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialIssuerDisplayRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.GetCredentialIssuerDisplayFlowError
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.model.toGetCredentialIssuerDisplayFlowError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialIssuerDisplayRepo
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.GetCredentialIssuerDisplayFlow
import ch.admin.foitt.wallet.platform.utils.andThen
import ch.admin.foitt.wallet.platform.utils.mapError
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCredentialIssuerDisplayFlowImpl @Inject constructor(
    private val credentialIssuerDisplayRepo: CredentialIssuerDisplayRepo,
    private val getLocalizedDisplay: GetLocalizedDisplay,
) : GetCredentialIssuerDisplayFlow {
    override fun invoke(credentialId: Long): Flow<Result<CredentialIssuerDisplay?, GetCredentialIssuerDisplayFlowError>> =
        credentialIssuerDisplayRepo.getIssuerDisplays(credentialId)
            .mapError(CredentialIssuerDisplayRepositoryError::toGetCredentialIssuerDisplayFlowError)
            .andThen { credentialIssuerDisplays ->
                coroutineBinding {
                    if (credentialIssuerDisplays.isEmpty()) return@coroutineBinding null
                    getDisplay(credentialIssuerDisplays).bind()
                }
            }

    private fun getDisplay(displays: List<CredentialIssuerDisplay>): Result<CredentialIssuerDisplay, GetCredentialIssuerDisplayFlowError> =
        getLocalizedDisplay(displays)?.let { Ok(it) }
            ?: Err(SsiError.Unexpected(IllegalStateException("No localized display found")))
}

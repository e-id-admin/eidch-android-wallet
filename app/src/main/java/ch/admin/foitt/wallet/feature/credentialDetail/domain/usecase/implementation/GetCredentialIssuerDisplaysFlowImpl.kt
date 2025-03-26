package ch.admin.foitt.wallet.feature.credentialDetail.domain.usecase.implementation

import ch.admin.foitt.wallet.feature.credentialDetail.domain.model.CredentialDetailError
import ch.admin.foitt.wallet.feature.credentialDetail.domain.model.GetCredentialIssuerDisplaysFlowError
import ch.admin.foitt.wallet.feature.credentialDetail.domain.model.IssuerDisplay
import ch.admin.foitt.wallet.feature.credentialDetail.domain.model.toGetCredentialIssuerDisplaysFlowError
import ch.admin.foitt.wallet.feature.credentialDetail.domain.model.toIssuerDisplay
import ch.admin.foitt.wallet.feature.credentialDetail.domain.usecase.GetCredentialIssuerDisplaysFlow
import ch.admin.foitt.wallet.platform.database.domain.model.LocalizedDisplay
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedDisplay
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialIssuerDisplayRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialIssuerDisplayRepo
import ch.admin.foitt.wallet.platform.utils.andThen
import ch.admin.foitt.wallet.platform.utils.mapError
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCredentialIssuerDisplaysFlowImpl @Inject constructor(
    private val credentialIssuerDisplayRepo: CredentialIssuerDisplayRepo,
    private val getLocalizedDisplay: GetLocalizedDisplay,
) : GetCredentialIssuerDisplaysFlow {
    override fun invoke(credentialId: Long): Flow<Result<IssuerDisplay, GetCredentialIssuerDisplaysFlowError>> =
        credentialIssuerDisplayRepo.getIssuerDisplaysFlow(credentialId)
            .mapError(CredentialIssuerDisplayRepositoryError::toGetCredentialIssuerDisplaysFlowError)
            .andThen { credentialIssuerDisplays ->
                coroutineBinding {
                    val display = getDisplay(credentialIssuerDisplays).bind()
                    display.toIssuerDisplay()
                }
            }

    private fun <T : LocalizedDisplay> getDisplay(displays: List<T>): Result<T, GetCredentialIssuerDisplaysFlowError> =
        getLocalizedDisplay(displays)?.let { Ok(it) }
            ?: Err(CredentialDetailError.Unexpected(IllegalStateException("No localized display found")))
}

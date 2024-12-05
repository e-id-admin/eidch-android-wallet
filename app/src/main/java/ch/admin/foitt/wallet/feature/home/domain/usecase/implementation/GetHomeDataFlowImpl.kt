package ch.admin.foitt.wallet.feature.home.domain.usecase.implementation

import ch.admin.foitt.wallet.feature.home.domain.model.GetHomeDataFlowError
import ch.admin.foitt.wallet.feature.home.domain.model.HomeError
import ch.admin.foitt.wallet.feature.home.domain.model.HomeRepositoryError
import ch.admin.foitt.wallet.feature.home.domain.model.toGetHomeDataFlowError
import ch.admin.foitt.wallet.feature.home.domain.repository.HomeRepository
import ch.admin.foitt.wallet.feature.home.domain.usecase.GetHomeDataFlow
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialPreview
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.IsCredentialFromBetaIssuerImpl
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialWithIssuerAndDisplays
import ch.admin.foitt.wallet.platform.database.domain.model.LocalizedDisplay
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedDisplay
import ch.admin.foitt.wallet.platform.utils.andThen
import ch.admin.foitt.wallet.platform.utils.mapError
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetHomeDataFlowImpl @Inject constructor(
    private val homeRepository: HomeRepository,
    private val getLocalizedDisplay: GetLocalizedDisplay,
    private val isCredentialFromBetaIssuer: IsCredentialFromBetaIssuerImpl,
) : GetHomeDataFlow {
    override fun invoke(): Flow<Result<List<CredentialPreview>, GetHomeDataFlowError>> =
        homeRepository.getHomeData()
            .mapError(HomeRepositoryError::toGetHomeDataFlowError)
            .andThen { credentials ->
                coroutineBinding {
                    createCredentialPreviews(
                        credentials = credentials,
                    ).bind()
                }
            }

    private suspend fun createCredentialPreviews(
        credentials: List<CredentialWithIssuerAndDisplays>
    ): Result<List<CredentialPreview>, GetHomeDataFlowError> = coroutineBinding {
        credentials.map { credentialWithIssuerAndDisplays ->
            val credential = credentialWithIssuerAndDisplays.credential
            val display = getDisplay(credentialWithIssuerAndDisplays.credentialDisplays).bind()

            CredentialPreview(
                credential = credential,
                credentialDisplay = display,
                isCredentialFromBetaIssuer = isCredentialFromBetaIssuer(credential.id)
            )
        }
    }

    private fun <T : LocalizedDisplay> getDisplay(displays: List<T>): Result<T, GetHomeDataFlowError> =
        getLocalizedDisplay(displays)?.let { Ok(it) }
            ?: Err(HomeError.Unexpected(IllegalStateException("No localized display found")))
}

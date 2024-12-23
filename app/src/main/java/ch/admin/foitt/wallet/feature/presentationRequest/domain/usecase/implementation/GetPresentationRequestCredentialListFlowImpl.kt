package ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase.implementation

import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.GetPresentationRequestCredentialListFlowError
import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.PresentationCredentialDisplayData
import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.PresentationRequestError
import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.PresentationRequestRepositoryError
import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.toGetPresentationRequestCredentialListFlowError
import ch.admin.foitt.wallet.feature.presentationRequest.domain.repository.PresentationRequestRepository
import ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase.GetPresentationRequestCredentialListFlow
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialPreview
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.IsCredentialFromBetaIssuerImpl
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CompatibleCredential
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialDisplay
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedDisplay
import ch.admin.foitt.wallet.platform.utils.andThen
import ch.admin.foitt.wallet.platform.utils.mapError
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPresentationRequestCredentialListFlowImpl @Inject constructor(
    private val presentationRequestRepository: PresentationRequestRepository,
    private val getLocalizedDisplay: GetLocalizedDisplay,
    private val isCredentialFromBetaIssuer: IsCredentialFromBetaIssuerImpl,
) : GetPresentationRequestCredentialListFlow {
    override fun invoke(
        compatibleCredentials: Array<CompatibleCredential>,
    ): Flow<Result<PresentationCredentialDisplayData, GetPresentationRequestCredentialListFlowError>> =
        presentationRequestRepository.getPresentationCredentialListFlow()
            .mapError(PresentationRequestRepositoryError::toGetPresentationRequestCredentialListFlowError)
            .andThen { credentialsWithDisplays ->
                coroutineBinding {
                    val credentialList: List<CredentialPreview> = let {
                        val compatibleCredentialIds = compatibleCredentials.map { it.credentialId }
                        credentialsWithDisplays.map { credentialWithDisplays ->
                            val credential = credentialWithDisplays.credential
                            val display = getDisplay(credentialWithDisplays.displays).bind()

                            CredentialPreview(
                                credential = credential,
                                credentialDisplay = display,
                                isCredentialFromBetaIssuer = isCredentialFromBetaIssuer(credential.id)
                            )
                        }.filter { it.credentialId in compatibleCredentialIds }
                    }
                    PresentationCredentialDisplayData(
                        credentials = credentialList
                    )
                }
            }

    private fun getDisplay(displays: List<CredentialDisplay>): Result<CredentialDisplay, GetPresentationRequestCredentialListFlowError> =
        getLocalizedDisplay(displays)?.let { Ok(it) }
            ?: Err(PresentationRequestError.Unexpected(IllegalStateException("No localized display found")))
}

package ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.shouldFetchTrustStatements
import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.GetPresentationRequestCredentialListFlowError
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
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.FetchTrustStatementFromDid
import ch.admin.foitt.wallet.platform.utils.andThen
import ch.admin.foitt.wallet.platform.utils.mapError
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.get
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject

class GetPresentationRequestCredentialListFlowImpl @Inject constructor(
    private val presentationRequestRepository: PresentationRequestRepository,
    private val getLocalizedDisplay: GetLocalizedDisplay,
    private val isCredentialFromBetaIssuer: IsCredentialFromBetaIssuerImpl,
    private val fetchTrustStatementFromDid: FetchTrustStatementFromDid,
) : GetPresentationRequestCredentialListFlow {
    override fun invoke(
        compatibleCredentials: Array<CompatibleCredential>,
        presentationRequest: PresentationRequest,
    ): Flow<Result<List<CredentialPreview>, GetPresentationRequestCredentialListFlowError>> =
        presentationRequestRepository.getPresentationCredentialListFlow()
            .mapError(PresentationRequestRepositoryError::toGetPresentationRequestCredentialListFlowError)
            .andThen { credentialsWithDisplays ->
                coroutineBinding {
                    val compatibleCredentialIds = compatibleCredentials.map { it.credentialId }
                    credentialsWithDisplays.map { credentialWithDisplays ->
                        val credential = credentialWithDisplays.credential
                        val display = getDisplay(credentialWithDisplays.displays).bind()

                        val trustStatement = if (presentationRequest.shouldFetchTrustStatements()) {
                            fetchTrustStatementFromDid(presentationRequest.clientId).get()
                        } else {
                            null
                        }
                        Timber.d("${trustStatement ?: "truststatement not evaluated or failed"}")

                        CredentialPreview(
                            credential = credential,
                            credentialDisplay = display,
                            isCredentialFromBetaIssuer = isCredentialFromBetaIssuer(credential.id)
                        )
                    }.filter { it.credentialId in compatibleCredentialIds }
                }
            }

    private fun getDisplay(displays: List<CredentialDisplay>): Result<CredentialDisplay, GetPresentationRequestCredentialListFlowError> =
        getLocalizedDisplay(displays)?.let { Ok(it) }
            ?: Err(PresentationRequestError.Unexpected(IllegalStateException("No localized display found")))
}

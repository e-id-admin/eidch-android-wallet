package ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.shouldFetchTrustStatements
import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.GetPresentationRequestFlowError
import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.PresentationRequestError
import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.PresentationRequestRepositoryError
import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.PresentationRequestUi
import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.toGetPresentationRequestFlowError
import ch.admin.foitt.wallet.feature.presentationRequest.domain.repository.PresentationRequestRepository
import ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase.GetPresentationRequestFlow
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialPreview
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.IsCredentialFromBetaIssuerImpl
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.PresentationRequestField
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimWithDisplays
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialDisplay
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedDisplay
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimData
import ch.admin.foitt.wallet.platform.ssi.domain.model.MapToCredentialClaimDataError
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.MapToCredentialClaimData
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.FetchTrustStatementFromDid
import ch.admin.foitt.wallet.platform.utils.andThen
import ch.admin.foitt.wallet.platform.utils.mapError
import ch.admin.foitt.wallet.platform.utils.sortByOrder
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.get
import com.github.michaelbull.result.mapError
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject

class GetPresentationRequestFlowImpl @Inject constructor(
    private val presentationRequestRepository: PresentationRequestRepository,
    private val getLocalizedDisplay: GetLocalizedDisplay,
    private val mapToCredentialClaimData: MapToCredentialClaimData,
    private val isCredentialFromBetaIssuer: IsCredentialFromBetaIssuerImpl,
    private val fetchTrustStatementFromDid: FetchTrustStatementFromDid,
) : GetPresentationRequestFlow {
    override fun invoke(
        id: Long,
        requestedFields: List<PresentationRequestField>,
        presentationRequest: PresentationRequest,
    ): Flow<Result<PresentationRequestUi, GetPresentationRequestFlowError>> =
        presentationRequestRepository.getPresentationCredentialFlow(id)
            .mapError(PresentationRequestRepositoryError::toGetPresentationRequestFlowError)
            .andThen { presentationCredentialEntity ->
                coroutineBinding {
                    val credential = presentationCredentialEntity.credential
                    val credentialDisplay = getDisplay(presentationCredentialEntity.credentialDisplays).bind()
                    val requestedClaims = getCredentialClaimData(
                        claims = presentationCredentialEntity.claims,
                        requestedFields = requestedFields,
                    ).bind()
                    val credentialPreview = CredentialPreview(
                        credential = credential,
                        credentialDisplay = credentialDisplay,
                        isCredentialFromBetaIssuer = isCredentialFromBetaIssuer(credential.id)
                    )

                    val trustStatement = if (presentationRequest.shouldFetchTrustStatements()) {
                        fetchTrustStatementFromDid(presentationRequest.clientId).get()
                    } else {
                        null
                    }
                    Timber.d("${trustStatement ?: "truststatement not evaluated or failed"}")

                    PresentationRequestUi(
                        credential = credentialPreview,
                        requestedClaims = requestedClaims,
                    )
                }
            }

    private fun getDisplay(displays: List<CredentialDisplay>): Result<CredentialDisplay, GetPresentationRequestFlowError> =
        getLocalizedDisplay(displays)?.let { Ok(it) }
            ?: Err(PresentationRequestError.Unexpected(IllegalStateException("No localized display found")))

    private suspend fun getCredentialClaimData(
        claims: List<CredentialClaimWithDisplays>,
        requestedFields: List<PresentationRequestField>,
    ): Result<List<CredentialClaimData>, GetPresentationRequestFlowError> = coroutineBinding {
        val requiredClaims = filterClaims(
            claims = claims,
            fieldList = requestedFields,
        ).sortByOrder()

        requiredClaims.map { claimWithDisplays ->
            val claim = claimWithDisplays.claim
            mapToCredentialClaimData(
                claim,
                claimWithDisplays.displays
            ).mapError(MapToCredentialClaimDataError::toGetPresentationRequestFlowError).bind()
        }
    }

    private fun filterClaims(claims: List<CredentialClaimWithDisplays>, fieldList: List<PresentationRequestField>) =
        claims.filter { claim -> fieldList.any { field -> field.key == claim.claim.key } }
}

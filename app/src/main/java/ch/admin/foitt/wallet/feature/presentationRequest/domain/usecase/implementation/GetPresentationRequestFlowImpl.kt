package ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase.implementation

import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.GetPresentationRequestFlowError
import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.PresentationRequestDisplayData
import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.toGetPresentationRequestFlowError
import ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase.GetPresentationRequestFlow
import ch.admin.foitt.wallet.platform.credential.domain.model.MapToCredentialDisplayDataError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.MapToCredentialDisplayData
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.PresentationRequestField
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimWithDisplays
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimData
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialWithDisplaysAndClaimsRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.MapToCredentialClaimDataError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialWithDisplaysAndClaimsRepository
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.MapToCredentialClaimData
import ch.admin.foitt.wallet.platform.utils.andThen
import ch.admin.foitt.wallet.platform.utils.mapError
import ch.admin.foitt.wallet.platform.utils.sortByOrder
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPresentationRequestFlowImpl @Inject constructor(
    private val credentialWithDisplaysAndClaimsRepository: CredentialWithDisplaysAndClaimsRepository,
    private val mapToCredentialDisplayData: MapToCredentialDisplayData,
    private val mapToCredentialClaimData: MapToCredentialClaimData,
) : GetPresentationRequestFlow {
    override fun invoke(
        id: Long,
        requestedFields: List<PresentationRequestField>,
    ): Flow<Result<PresentationRequestDisplayData, GetPresentationRequestFlowError>> =
        credentialWithDisplaysAndClaimsRepository.getCredentialWithDisplaysAndClaimsFlowById(id)
            .mapError(CredentialWithDisplaysAndClaimsRepositoryError::toGetPresentationRequestFlowError)
            .andThen { credentialWithDisplaysAndClaims ->
                coroutineBinding {
                    val credentialDisplayData = mapToCredentialDisplayData(
                        credential = credentialWithDisplaysAndClaims.credential,
                        credentialDisplays = credentialWithDisplaysAndClaims.credentialDisplays,
                    ).mapError(MapToCredentialDisplayDataError::toGetPresentationRequestFlowError)
                        .bind()

                    val requestedClaims = getCredentialClaimData(
                        claims = credentialWithDisplaysAndClaims.claims,
                        requestedFields = requestedFields,
                    ).bind()

                    PresentationRequestDisplayData(
                        credential = credentialDisplayData,
                        requestedClaims = requestedClaims,
                    )
                }
            }

    private suspend fun getCredentialClaimData(
        claims: List<CredentialClaimWithDisplays>,
        requestedFields: List<PresentationRequestField>,
    ): Result<List<CredentialClaimData>, GetPresentationRequestFlowError> = coroutineBinding {
        val requiredClaims = filterClaims(
            claims = claims,
            fieldList = requestedFields,
        ).sortByOrder()

        requiredClaims.map { claimWithDisplays ->
            mapToCredentialClaimData(
                claimWithDisplays
            ).mapError(MapToCredentialClaimDataError::toGetPresentationRequestFlowError).bind()
        }
    }

    private fun filterClaims(claims: List<CredentialClaimWithDisplays>, fieldList: List<PresentationRequestField>) =
        claims.filter { claim -> fieldList.any { field -> field.key == claim.claim.key } }
}

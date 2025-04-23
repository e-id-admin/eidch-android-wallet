package ch.admin.foitt.wallet.feature.credentialOffer.domain.usecase.implementation

import ch.admin.foitt.wallet.feature.credentialOffer.domain.model.CredentialOffer
import ch.admin.foitt.wallet.feature.credentialOffer.domain.model.GetCredentialOfferFlowError
import ch.admin.foitt.wallet.feature.credentialOffer.domain.model.toGetCredentialOfferFlowError
import ch.admin.foitt.wallet.feature.credentialOffer.domain.usecase.GetCredentialOfferFlow
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.FetchAndCacheIssuerDisplayData
import ch.admin.foitt.wallet.platform.credential.domain.model.MapToCredentialDisplayDataError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.MapToCredentialDisplayData
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

class GetCredentialOfferFlowImpl @Inject constructor(
    private val credentialWithDisplaysAndClaimsRepository: CredentialWithDisplaysAndClaimsRepository,
    private val mapToCredentialDisplayData: MapToCredentialDisplayData,
    private val mapToCredentialClaimData: MapToCredentialClaimData,
    private val fetchAndCacheIssuerDisplayData: FetchAndCacheIssuerDisplayData,
) : GetCredentialOfferFlow {
    override fun invoke(credentialId: Long): Flow<Result<CredentialOffer?, GetCredentialOfferFlowError>> =
        credentialWithDisplaysAndClaimsRepository.getNullableCredentialWithDisplaysAndClaimsFlowById(credentialId)
            .mapError(CredentialWithDisplaysAndClaimsRepositoryError::toGetCredentialOfferFlowError)
            .andThen { credentialWithDisplaysAndClaims ->
                coroutineBinding {
                    credentialWithDisplaysAndClaims?.let {
                        val credential = credentialWithDisplaysAndClaims.credential

                        val credentialDisplayData = mapToCredentialDisplayData(
                            credential = credential,
                            credentialDisplays = credentialWithDisplaysAndClaims.credentialDisplays,
                        ).mapError(MapToCredentialDisplayDataError::toGetCredentialOfferFlowError)
                            .bind()
                        fetchAndCacheIssuerDisplayData(credentialId, credential.issuer)

                        val claims = getCredentialClaimData(credentialWithDisplaysAndClaims.claims.sortByOrder()).bind()
                        CredentialOffer(
                            credential = credentialDisplayData,
                            claims = claims
                        )
                    }
                }
            }

    private suspend fun getCredentialClaimData(
        claims: List<CredentialClaimWithDisplays>
    ): Result<List<CredentialClaimData>, GetCredentialOfferFlowError> = coroutineBinding {
        claims.map { claimWithDisplays ->
            mapToCredentialClaimData(
                claimWithDisplays
            ).mapError(MapToCredentialClaimDataError::toGetCredentialOfferFlowError).bind()
        }
    }
}

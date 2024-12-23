package ch.admin.foitt.wallet.feature.credentialOffer.domain.usecase.implementation

import ch.admin.foitt.wallet.feature.credentialOffer.domain.model.CredentialOffer
import ch.admin.foitt.wallet.feature.credentialOffer.domain.model.CredentialOfferError
import ch.admin.foitt.wallet.feature.credentialOffer.domain.model.GetCredentialOfferFlowError
import ch.admin.foitt.wallet.feature.credentialOffer.domain.model.toGetCredentialOfferFlowError
import ch.admin.foitt.wallet.feature.credentialOffer.domain.usecase.GetCredentialOffer
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.FetchIssuerDisplayData
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialPreview
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.IsCredentialFromBetaIssuerImpl
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimWithDisplays
import ch.admin.foitt.wallet.platform.database.domain.model.LocalizedDisplay
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedDisplay
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimData
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialOfferRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.MapToCredentialClaimDataError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialOfferRepository
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.MapToCredentialClaimData
import ch.admin.foitt.wallet.platform.utils.andThen
import ch.admin.foitt.wallet.platform.utils.mapError
import ch.admin.foitt.wallet.platform.utils.sortByOrder
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCredentialOfferImpl @Inject constructor(
    private val credentialOfferRepository: CredentialOfferRepository,
    private val getLocalizedDisplay: GetLocalizedDisplay,
    private val mapToCredentialClaimData: MapToCredentialClaimData,
    private val isCredentialFromBetaIssuer: IsCredentialFromBetaIssuerImpl,
    private val fetchIssuerDisplayData: FetchIssuerDisplayData,
) : GetCredentialOffer {
    override fun invoke(credentialId: Long): Flow<Result<CredentialOffer?, GetCredentialOfferFlowError>> =
        credentialOfferRepository.getCredentialOfferByIdFlow(credentialId)
            .mapError(CredentialOfferRepositoryError::toGetCredentialOfferFlowError)
            .andThen { credentialOfferEntity ->
                coroutineBinding {
                    if (credentialOfferEntity == null) return@coroutineBinding null

                    val credential = credentialOfferEntity.credential
                    val credentialDisplay = getDisplay(credentialOfferEntity.credentialDisplays).bind()
                    val claims = getCredentialClaimData(credentialOfferEntity.claims.sortByOrder()).bind()
                    val credentialPreview =
                        CredentialPreview(
                            credential = credential,
                            credentialDisplay = credentialDisplay,
                            isCredentialFromBetaIssuer = isCredentialFromBetaIssuer(credential.id)
                        )

                    val issuerMetaData = fetchIssuerDisplayData(credentialId)

                    CredentialOffer(
                        issuerDisplayData = issuerMetaData,
                        credential = credentialPreview,
                        claims = claims
                    )
                }
            }

    private fun <T : LocalizedDisplay> getDisplay(
        displays: List<T>,
    ): Result<T, GetCredentialOfferFlowError> =
        getLocalizedDisplay(
            displays = displays,
        )?.let { Ok(it) }
            ?: Err(CredentialOfferError.Unexpected(IllegalStateException("No localized display found")))

    private suspend fun getCredentialClaimData(
        claims: List<CredentialClaimWithDisplays>
    ): Result<List<CredentialClaimData>, GetCredentialOfferFlowError> = coroutineBinding {
        claims.map { claimWithDisplays ->
            val claim = claimWithDisplays.claim
            mapToCredentialClaimData(
                claim,
                claimWithDisplays.displays
            ).mapError(MapToCredentialClaimDataError::toGetCredentialOfferFlowError).bind()
        }
    }
}

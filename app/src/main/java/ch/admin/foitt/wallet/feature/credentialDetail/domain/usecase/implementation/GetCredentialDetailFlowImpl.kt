package ch.admin.foitt.wallet.feature.credentialDetail.domain.usecase.implementation

import ch.admin.foitt.wallet.feature.credentialDetail.domain.model.CredentialDetail
import ch.admin.foitt.wallet.feature.credentialDetail.domain.model.CredentialDetailError
import ch.admin.foitt.wallet.feature.credentialDetail.domain.model.GetCredentialDetailFlowError
import ch.admin.foitt.wallet.feature.credentialDetail.domain.model.toGetCredentialDetailFlowError
import ch.admin.foitt.wallet.feature.credentialDetail.domain.repository.CredentialDetailRepository
import ch.admin.foitt.wallet.feature.credentialDetail.domain.usecase.GetCredentialDetailFlow
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialPreview
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.IsCredentialFromBetaIssuerImpl
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimWithDisplays
import ch.admin.foitt.wallet.platform.database.domain.model.LocalizedDisplay
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedDisplay
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimData
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.MapToCredentialClaimDataError
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

class GetCredentialDetailFlowImpl @Inject constructor(
    private val credentialDetailRepository: CredentialDetailRepository,
    private val getLocalizedDisplay: GetLocalizedDisplay,
    private val mapToCredentialClaimData: MapToCredentialClaimData,
    private val isCredentialFromBetaIssuer: IsCredentialFromBetaIssuerImpl,
) : GetCredentialDetailFlow {
    override fun invoke(credentialId: Long): Flow<Result<CredentialDetail?, GetCredentialDetailFlowError>> =
        credentialDetailRepository.getCredentialDetailByIdFlow(credentialId)
            .mapError(CredentialRepositoryError::toGetCredentialDetailFlowError)
            .andThen { credentialDetail ->
                coroutineBinding {
                    credentialDetail?.let {
                        val credential = credentialDetail.credential
                        val credentialDisplay = getDisplay(credentialDetail.credentialDisplays).bind()
                        val claims = getCredentialClaimData(credentialDetail.claims.sortByOrder()).bind()

                        val credentialPreview = CredentialPreview(
                            credential = credential,
                            credentialDisplay = credentialDisplay,
                            isCredentialFromBetaIssuer = isCredentialFromBetaIssuer(credential.id)
                        )
                        val issuerDisplay = getDisplay(credentialDetail.issuerDisplays).bind()

                        CredentialDetail(
                            credential = credentialPreview,
                            claims = claims,
                            issuer = issuerDisplay,
                        )
                    }
                }
            }

    private fun <T : LocalizedDisplay> getDisplay(displays: List<T>): Result<T, GetCredentialDetailFlowError> =
        getLocalizedDisplay(displays)?.let { Ok(it) }
            ?: Err(CredentialDetailError.Unexpected(IllegalStateException("No localized display found")))

    private suspend fun getCredentialClaimData(
        claims: List<CredentialClaimWithDisplays>
    ): Result<List<CredentialClaimData>, GetCredentialDetailFlowError> = coroutineBinding {
        claims.map { claimWithDisplays ->
            val claim = claimWithDisplays.claim
            mapToCredentialClaimData(
                claim,
                claimWithDisplays.displays
            ).mapError(MapToCredentialClaimDataError::toGetCredentialDetailFlowError).bind()
        }
    }
}

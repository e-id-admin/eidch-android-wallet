package ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.credential.domain.model.MapToCredentialDisplayDataError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.MapToCredentialDisplayData
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimWithDisplays
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimData
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialDetail
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialWithDisplaysAndClaimsRepositoryError
import ch.admin.foitt.wallet.platform.ssi.domain.model.GetCredentialDetailFlowError
import ch.admin.foitt.wallet.platform.ssi.domain.model.MapToCredentialClaimDataError
import ch.admin.foitt.wallet.platform.ssi.domain.model.toGetCredentialDetailFlowError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialWithDisplaysAndClaimsRepository
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.GetCredentialDetailFlow
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.MapToCredentialClaimData
import ch.admin.foitt.wallet.platform.utils.andThen
import ch.admin.foitt.wallet.platform.utils.mapError
import ch.admin.foitt.wallet.platform.utils.sortByOrder
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCredentialDetailFlowImpl @Inject constructor(
    private val credentialWithDisplaysAndClaimsRepository: CredentialWithDisplaysAndClaimsRepository,
    private val mapToCredentialDisplayData: MapToCredentialDisplayData,
    private val mapToCredentialClaimData: MapToCredentialClaimData,
) : GetCredentialDetailFlow {
    override fun invoke(credentialId: Long): Flow<Result<CredentialDetail?, GetCredentialDetailFlowError>> =
        credentialWithDisplaysAndClaimsRepository.getNullableCredentialWithDisplaysAndClaimsFlowById(credentialId)
            .mapError(CredentialWithDisplaysAndClaimsRepositoryError::toGetCredentialDetailFlowError)
            .andThen { credentialDetail ->
                coroutineBinding {
                    credentialDetail?.let {
                        val credentialDisplayData = mapToCredentialDisplayData(
                            credential = credentialDetail.credential,
                            credentialDisplays = credentialDetail.credentialDisplays,
                        ).mapError(MapToCredentialDisplayDataError::toGetCredentialDetailFlowError)
                            .bind()

                        val claims = getCredentialClaimData(credentialDetail.claims.sortByOrder()).bind()

                        CredentialDetail(
                            credential = credentialDisplayData,
                            claims = claims,
                        )
                    }
                }
            }

    private suspend fun getCredentialClaimData(
        claims: List<CredentialClaimWithDisplays>
    ): Result<List<CredentialClaimData>, GetCredentialDetailFlowError> = coroutineBinding {
        claims.map { claimWithDisplays ->
            mapToCredentialClaimData(
                claimWithDisplays
            ).mapError(MapToCredentialClaimDataError::toGetCredentialDetailFlowError).bind()
        }
    }
}

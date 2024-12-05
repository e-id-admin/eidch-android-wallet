package ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase.implementation

import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.GeneratePresentationMetadataError
import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.PresentationMetadata
import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.toGeneratePresentationMetadataError
import ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase.GeneratePresentationMetadata
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CompatibleCredential
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.PresentationRequestField
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim
import ch.admin.foitt.wallet.platform.ssi.domain.model.GetCredentialClaimDataError
import ch.admin.foitt.wallet.platform.ssi.domain.model.GetCredentialClaimsError
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.GetCredentialClaimData
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.GetCredentialClaims
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.coroutineBinding
import com.github.michaelbull.result.mapError
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import javax.inject.Inject

class GeneratePresentationMetadataImpl @Inject constructor(
    private val getCredentialClaims: GetCredentialClaims,
    private val getCredentialClaimData: GetCredentialClaimData,
) : GeneratePresentationMetadata {
    override suspend fun invoke(
        compatibleCredential: CompatibleCredential
    ): Result<PresentationMetadata, GeneratePresentationMetadataError> = coroutineBinding {
        val credentialClaims = getCredentialClaims(compatibleCredential.credentialId)
            .mapError(GetCredentialClaimsError::toGeneratePresentationMetadataError)
            .bind()

        val requiredClaims = filterClaims(credentialClaims, compatibleCredential.requestedFields)

        val claimsData = requiredClaims.map { credentialClaim ->
            async {
                getCredentialClaimData(credentialClaim)
                    .mapError(GetCredentialClaimDataError::toGeneratePresentationMetadataError)
                    .bind()
            }
        }.awaitAll()
        PresentationMetadata(claimsData)
    }

    private fun filterClaims(claims: List<CredentialClaim>, fieldList: List<PresentationRequestField>) =
        claims.filter { claim -> fieldList.any { field -> field.key == claim.key } }
}

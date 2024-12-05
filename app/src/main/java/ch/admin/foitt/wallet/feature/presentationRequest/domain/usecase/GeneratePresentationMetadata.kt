package ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase

import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.GeneratePresentationMetadataError
import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.PresentationMetadata
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CompatibleCredential
import com.github.michaelbull.result.Result

interface GeneratePresentationMetadata {
    suspend operator fun invoke(
        compatibleCredential: CompatibleCredential
    ): Result<PresentationMetadata, GeneratePresentationMetadataError>
}

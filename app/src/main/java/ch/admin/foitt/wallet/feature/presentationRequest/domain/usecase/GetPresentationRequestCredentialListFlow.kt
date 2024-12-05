package ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequest
import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.GetPresentationRequestCredentialListFlowError
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialPreview
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CompatibleCredential
import com.github.michaelbull.result.Result
import kotlinx.coroutines.flow.Flow

interface GetPresentationRequestCredentialListFlow {
    operator fun invoke(
        compatibleCredentials: Array<CompatibleCredential>,
        presentationRequest: PresentationRequest,
    ): Flow<Result<List<CredentialPreview>, GetPresentationRequestCredentialListFlowError>>
}

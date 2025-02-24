package ch.admin.foitt.wallet.platform.navArgs.domain.model

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequest
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CompatibleCredential

data class PresentationRequestNavArg(
    val compatibleCredential: CompatibleCredential,
    val presentationRequest: PresentationRequest,
    val shouldFetchTrustStatement: Boolean,
)

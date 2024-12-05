package ch.admin.foitt.wallet.platform.navArgs.domain.model

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequest
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.ClientNameDisplay
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CompatibleCredential
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.LogoUriDisplay

data class PresentationRequestNavArg(
    val compatibleCredential: CompatibleCredential,
    val presentationRequest: PresentationRequest,
    val clientNameDisplay: Array<ClientNameDisplay>,
    val uriDisplay: Array<LogoUriDisplay>
)

package ch.admin.foitt.wallet.platform.credentialPresentation.domain.model

import ch.admin.foitt.openid4vc.domain.model.Invitation
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationFlowContext
import kotlinx.serialization.Serializable
import uniffi.heidi_dcql_rust.DcqlQuery

@Serializable
data class PresentationRequestWithRaw(
    val authorizationRequest: AuthorizationRequest,
    val rawPresentationRequest: String,
    val verificationProcessType: VerificationProcessType,
    val presentationContext: PresentationFlowContext = PresentationFlowContext(),
    val verifierAttestationTrusted: Boolean? = null,
    val hasVerifiedQuery: Boolean = false,
    val dcqlQuery: DcqlQuery,
) : Invitation

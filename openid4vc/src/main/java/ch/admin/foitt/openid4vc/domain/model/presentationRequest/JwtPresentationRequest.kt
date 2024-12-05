package ch.admin.foitt.openid4vc.domain.model.presentationRequest

import com.nimbusds.jwt.SignedJWT

data class JwtPresentationRequest(
    private val presentationRequest: PresentationRequest,
    val signedJWT: SignedJWT,
) : PresentationRequest(
    clientId = presentationRequest.clientId,
    clientIdScheme = presentationRequest.clientIdScheme,
    responseType = presentationRequest.responseType,
    responseMode = presentationRequest.responseMode,
    responseUri = presentationRequest.responseUri,
    nonce = presentationRequest.nonce,
    presentationDefinition = presentationRequest.presentationDefinition,
    clientMetaData = presentationRequest.clientMetaData
)

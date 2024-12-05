package ch.admin.foitt.openid4vc.domain.model.presentationRequest

data class JsonPresentationRequest(
    private val presentationRequest: PresentationRequest,
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

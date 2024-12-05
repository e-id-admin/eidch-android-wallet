package ch.admin.foitt.wallet.platform.credentialPresentation.mock

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.ClientMetaData
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.ClientName
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.LogoUri
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationDefinition
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequest

object MockPresentationRequest {
    val presentationRequest = PresentationRequest(
        nonce = "iusto",
        presentationDefinition = PresentationDefinition(
            id = "diam",
            inputDescriptors = listOf(),
            purpose = "purpose",
            name = "name",
        ),
        responseUri = "tincidunt",
        responseMode = "suscipit",
        clientId = "clientId",
        clientIdScheme = "clientIdScheme",
        responseType = "responseType",
        clientMetaData = null
    )

    val presentationRequestWithDisplays = PresentationRequest(
        nonce = "iusto",
        presentationDefinition = PresentationDefinition(
            id = "diam",
            inputDescriptors = listOf(),
            purpose = "purpose",
            name = "name",
        ),
        responseUri = "tincidunt",
        responseMode = "suscipit",
        clientId = "clientId",
        clientIdScheme = "clientIdScheme",
        responseType = "responseType",
        clientMetaData = ClientMetaData(
            clientNameList = listOf(
                ClientName(
                    clientName = "firstClientName",
                    locale = "en"
                ),
                ClientName(
                    clientName = "secondClientName",
                    locale = "fr"
                ),
                ClientName(
                    clientName = "clientName",
                    locale = "fallback"
                )
            ),
            logoUriList = listOf(
                LogoUri(
                    logoUri = "firstLogoUri",
                    locale = "en"
                ),
                LogoUri(
                    logoUri = "secondLogoUri",
                    locale = "de"
                ),
                LogoUri(
                    logoUri = "logoUri",
                    locale = "fallback"
                )
            )
        )
    )
}

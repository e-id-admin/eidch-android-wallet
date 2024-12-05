package ch.admin.foitt.openid4vc.domain.model.mock

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.ClientMetaData
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.ClientName
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.LogoUri

object ClientMetaDataMock {
    val clientMetaData = ClientMetaData(
        clientNameList = listOf(
            ClientName(
                clientName = "クライアント名",
                locale = "ja-Jpan-JP"
            ),
            ClientName(
                clientName = "Mon example",
                locale = "fr"
            ),
            ClientName(
                clientName = "mein Beispiel",
                locale = "de-ch"
            ),
            ClientName(
                clientName = "My Example",
                locale = "fallback"
            )
        ),
        logoUriList = listOf(
            LogoUri(
                logoUri = "someURI",
                locale = "fallback"
            )
        )
    )

    var clientMetaDataWithUri = clientMetaData.copy(
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
                logoUri = "someURI",
                locale = "fallback"
            )
        )
    )
}

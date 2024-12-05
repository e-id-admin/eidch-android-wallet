package ch.admin.foitt.openid4vc.domain.model.presentationRequest

import ch.admin.foitt.openid4vc.domain.model.mock.ClientMetaDataMock
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ClientMetaDataSerializerTest {

    val fallbackClientJson = """"client_name":"My Example""""
    val fallbackUriJson = """"logo_uri":"someURI""""
    val clientJson = """"client_name#ja-Jpan-JP":"クライアント名","client_name#fr":"Mon example","client_name#de-ch":"mein Beispiel""""
    val uriJson = """"logo_uri#en":"firstLogoUri","logo_uri#de":"secondLogoUri""""

    val payload = "{$clientJson,$fallbackClientJson,$fallbackUriJson}"
    val payloadWithUri = "{$clientJson,$fallbackClientJson,$uriJson,$fallbackUriJson}"
    val payloadOnlyWithFallbacks = "{$fallbackClientJson,$fallbackUriJson}"

    @Test
    fun `decode a json string to a ClientMetaData correctly`() = runTest {
        var result = Json.decodeFromString<ClientMetaData>(payload)

        assertEquals(result.clientNameList.size, 4)
        assertEquals(result.logoUriList.size, 1)
        assertEquals(result.clientNameList[3].clientName, "My Example")
        assertEquals(result.clientNameList[3].locale, "fallback")
        assertEquals(result.logoUriList[0].logoUri, "someURI")
        assertEquals(result.logoUriList[0].locale, "fallback")
    }

    @Test
    fun `decode a json string only with fallback elements to a ClientMetaData correctly`() = runTest {
        var result = Json.decodeFromString<ClientMetaData>(payloadOnlyWithFallbacks)

        assertEquals(result.clientNameList.size, 1)
        assertEquals(result.logoUriList.size, 1)
        assertEquals("My Example", result.clientNameList[0].clientName)
        assertEquals("fallback", result.clientNameList[0].locale)
        assertEquals("someURI", result.logoUriList[0].logoUri)
        assertEquals("fallback", result.logoUriList[0].locale)
    }

    @Test
    fun `decode a json string with uri elements to a ClientMetaData correctly`() = runTest {
        var result = Json.decodeFromString<ClientMetaData>(payloadWithUri)

        assertEquals(result.clientNameList.size, 4)
        assertEquals(result.logoUriList.size, 3)
        assertEquals(result.clientNameList[3].clientName, "My Example")
        assertEquals(result.clientNameList[3].locale, "fallback")
        assertEquals(result.logoUriList[0].logoUri, "firstLogoUri")
        assertEquals(result.logoUriList[0].locale, "en")
    }

    @Test
    fun `encode a ClientMetaData to a json string correctly`() = runTest {
        val mockClientMetaData = ClientMetaDataMock.clientMetaData

        val result = Json.encodeToString(ClientMetaData.serializer(), mockClientMetaData)

        assertEquals(payload, result)
    }

    @Test
    fun `encode a ClientMetaData with logoUri to a json string correctly`() = runTest {
        val mockClientMetaData = ClientMetaDataMock.clientMetaDataWithUri

        val result = Json.encodeToString(ClientMetaData.serializer(), mockClientMetaData)

        assertEquals(payloadWithUri, result)
    }
}

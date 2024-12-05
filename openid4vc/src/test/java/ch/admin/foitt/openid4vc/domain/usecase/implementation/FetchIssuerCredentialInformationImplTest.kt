package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.Logo
import ch.admin.foitt.openid4vc.util.SafeJsonTestInstance.safeJson
import ch.admin.foitt.openid4vc.util.assertOk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FetchIssuerCredentialInformationImplTest {

    @Test
    fun `Issuer credential information logo data uri is parsed correctly`() = runTest {
        val logo = safeJson.safeDecodeStringTo<Logo>(DATA_LOGO_JSON).assertOk()

        assertEquals(LOGO_DATA_URI, logo.uri)
    }

    @Test
    fun `Issuer credential information logo https uri is parsed correctly`() = runTest {
        val logo = safeJson.safeDecodeStringTo<Logo>(URL_LOGO_JSON).assertOk()

        assertEquals(LOGO_HTTPS_URI, logo.uri)
    }

    companion object {
        private const val LOGO_DATA_URI =
            "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAABwAAAAcCAYAAAByDd+UAAAACXBIWXMAAAsTAAALEwEAmpwYAAAAAXNSR0IArs4c6QAAAARnQU1BAACxjwv8YQUAAACPSURBVHgB7ZbbCYAwDEVvxEHcREdzlI7gCHUDN9AtfEJM0S9BUWwrSA5cWujHgdA2oWFcWhAyxKGjYVpYNpaZawSEiHJZCjhhP84lAuMczpUgMir8Tii32PBGiRdoSVWoQhX+UJjiOfnJf9pIV68QQFjsOWIkXoVGYi/OO9zgtlDKZeEBfRbeiT4IU+xRfwVePD+H6WV/zQAAAABJRU5ErkJggg=="
        private const val LOGO_HTTPS_URI = "https://example.org/example.png"

        private const val DATA_LOGO_JSON =
            """
            {
              "uri":"$LOGO_DATA_URI"
            }
            """

        private const val URL_LOGO_JSON =
            """
            {
              "uri":"$LOGO_HTTPS_URI"
            }
            """
    }
}

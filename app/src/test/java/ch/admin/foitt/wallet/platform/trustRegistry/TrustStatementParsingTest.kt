package ch.admin.foitt.wallet.platform.trustRegistry

import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IdentityV1TrustStatement
import ch.admin.foitt.wallet.util.SafeJsonTestInstance
import ch.admin.foitt.wallet.util.assertOk
import io.mockk.MockKAnnotations
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class TrustStatementParsingTest {

    private val safeJson = SafeJsonTestInstance.safeJson

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `parse identity trust statement correctly`() = runTest {
        val trustStatement = safeJson.safeDecodeStringTo<IdentityV1TrustStatement>(identityStatement)
        trustStatement.assertOk()
    }

    private companion object {
        val identityStatement = """
            {
                "vct": "TrustStatementIdentityV1",
                "iss": "did:example:issuer",
                "sub": "did:example:subject",
                "iat": 1690360968,
                "nbf": 1721896968,
                "exp": 1753432968,
                "status":  {
                    "status_list": {
                        "idx": 0,
                        "uri": "https://example.com/statuslist"
                    }
                },
                "entityName": {
                  "en": "John Smith's Smithery",
                  "de-CH": "John Smith's Schmiderei"
                },
                "registryIds": [
                  {
                    "type": "UID",
                    "value": "CHE-000.000.000"
                  },
                  {
                    "type": "LEI",
                    "value": "0A1B2C3D4E5F6G7H8J9I"
                  }
                ],
                "isStateActor": true
            }
        """.trimIndent()
    }
}

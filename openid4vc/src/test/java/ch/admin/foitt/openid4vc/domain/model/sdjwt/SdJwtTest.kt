package ch.admin.foitt.openid4vc.domain.model.sdjwt

import ch.admin.foitt.openid4vc.domain.model.sdjwt.mock.ComplexSdJwt
import ch.admin.foitt.openid4vc.domain.model.sdjwt.mock.FlatDisclosures
import ch.admin.foitt.openid4vc.domain.model.sdjwt.mock.FlatSdJwt
import ch.admin.foitt.openid4vc.domain.model.sdjwt.mock.JwtTimestamps
import ch.admin.foitt.openid4vc.domain.model.sdjwt.mock.KeyBindingJwt
import ch.admin.foitt.openid4vc.domain.model.sdjwt.mock.RecursiveSdJwt
import ch.admin.foitt.openid4vc.domain.model.sdjwt.mock.SdJwtSeparator
import ch.admin.foitt.openid4vc.domain.model.sdjwt.mock.StructuredSdJwt
import ch.admin.foitt.openid4vc.domain.model.sdjwt.mock.UndisclosedJwt
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class SdJwtTest {

    @Test
    fun `parsing a valid undisclosed JWT should return the JWT and the JSON with actual values`() = runTest {
        val sdJwt = SdJwt(UndisclosedJwt.JWT)

        assertEquals(UndisclosedJwt.JWT, sdJwt.signedJWT.parsedString)
        assertJsonEquals(UndisclosedJwt.JSON, sdJwt.json)
    }

    @Test
    fun `parsing a valid flat SD-JWT should return the JWT and the JSON with actual values`() = runTest {
        val sdJwt = SdJwt(FlatSdJwt.JWT + FlatDisclosures)

        assertEquals(FlatSdJwt.JWT, sdJwt.signedJWT.parsedString)
        assertJsonEquals(FlatSdJwt.JSON, sdJwt.json)
    }

    @Test
    fun `parsing a valid structured SD-JWT should return the JWT and JSON with actual values`() = runTest {
        val sdJwt = SdJwt(StructuredSdJwt.JWT + FlatDisclosures)

        assertEquals(StructuredSdJwt.JWT, sdJwt.signedJWT.parsedString)
        assertJsonEquals(StructuredSdJwt.JSON, sdJwt.json)
    }

    @Test
    fun `parsing a valid structured SD-JWT with KeyBindingJWT should return the JWT and the JSON with actual values`() =
        runTest {
            val sdJwt = SdJwt(StructuredSdJwt.JWT + FlatDisclosures + KeyBindingJwt)

            assertEquals(StructuredSdJwt.JWT, sdJwt.signedJWT.parsedString)
            assertJsonEquals(StructuredSdJwt.JSON, sdJwt.json)
        }

    @Test
    fun `parsing a valid recursive SD-JWT should return the JWT and the JSON with actual values`() = runTest {
        val sdJwt = SdJwt(RecursiveSdJwt.SD_JWT)

        assertEquals(RecursiveSdJwt.JWT, sdJwt.signedJWT.parsedString)
        assertJsonEquals(RecursiveSdJwt.JSON, sdJwt.json)
    }

    @Test
    fun `parsing a valid complex SD-JWT should return the JWT and the JSON with actual values`() = runTest {
        val sdJwt = SdJwt(ComplexSdJwt.SD_JWT)

        assertEquals(ComplexSdJwt.JWT, sdJwt.signedJWT.parsedString)
        assertJsonEquals(ComplexSdJwt.JSON, sdJwt.json)
    }

    @Test
    fun `parsing a valid SD-JWT with timestamps should return all timestamps`() = runTest {
        val sdJwt = SdJwt(JwtTimestamps.VALID_JWT)

        assertEquals(JwtTimestamps.VALID_TIMESTAMP, sdJwt.expiredAt)
        assertEquals(JwtTimestamps.VALID_TIMESTAMP, sdJwt.issuedAt)
        assertEquals(JwtTimestamps.VALID_TIMESTAMP, sdJwt.activatedAt)
    }

    @Test
    fun `parsing a valid SD-JWT with no timestamps should return null timestamps`() = runTest {
        val sdJwt = SdJwt(JwtTimestamps.JWT_WITHOUT_TIMESTAMPS)

        assertEquals(null, sdJwt.expiredAt)
        assertEquals(null, sdJwt.issuedAt)
        assertEquals(null, sdJwt.activatedAt)
    }

    @Test
    fun `parsing a valid SD-JWT with no expiredAt should return other timestamps`() = runTest {
        val sdJwt = SdJwt(JwtTimestamps.NO_EXPIRED_AT_JWT)

        assertEquals(null, sdJwt.expiredAt)
        assertEquals(JwtTimestamps.VALID_TIMESTAMP, sdJwt.issuedAt)
        assertEquals(JwtTimestamps.VALID_TIMESTAMP, sdJwt.activatedAt)
    }

    @Test
    fun `parsing a valid SD-JWT with no issuedAt should return other timestamps`() = runTest {
        val sdJwt = SdJwt(JwtTimestamps.NO_ISSUED_AT_JWT)

        assertEquals(JwtTimestamps.VALID_TIMESTAMP, sdJwt.expiredAt)
        assertEquals(null, sdJwt.issuedAt)
        assertEquals(JwtTimestamps.VALID_TIMESTAMP, sdJwt.activatedAt)
    }

    @Test
    fun `parsing a valid SD-JWT with no activatedAt should return other timestamps`() = runTest {
        val sdJwt = SdJwt(JwtTimestamps.NO_ACTIVATED_AT_JWT)

        assertEquals(JwtTimestamps.VALID_TIMESTAMP, sdJwt.expiredAt)
        assertEquals(JwtTimestamps.VALID_TIMESTAMP, sdJwt.issuedAt)
        assertEquals(null, sdJwt.activatedAt)
    }

    @Test
    fun `parsing an SD-JWT with only two JWT parts should throw an exception`() = runTest {
        val invalidSdJwt = "test.test${SdJwtSeparator}disclosures$SdJwtSeparator"
        val sdJwt = SdJwt(invalidSdJwt)

        assertThrows<IllegalStateException> {
            sdJwt.signedJWT
        }
    }

    @Test
    fun `parsing an SD-JWT with only one JWT part should throw an exception`() = runTest {
        val invalidSdJwt = "test${SdJwtSeparator}disclosures$SdJwtSeparator"
        val sdJwt = SdJwt(invalidSdJwt)

        assertThrows<IllegalStateException> {
            sdJwt.signedJWT
        }
    }

    @Test
    fun `parsing an SD-JWT with a disclosure with two elements should throw an exception`() = runTest {
        // ["test_salt_1", "test_key_1"]
        val invalidSdJwt = FlatSdJwt.JWT + "${SdJwtSeparator}WyJ0ZXN0X3NhbHRfMSIsICJ0ZXN0X2tleV8xIl0$SdJwtSeparator"
        val sdJwt = SdJwt(invalidSdJwt)

        assertThrows<IllegalStateException> {
            sdJwt.json
        }
    }

    @Test
    fun `parsing an SD-JWT with a disclosure with one element should throw an exception`() = runTest {
        // ["test_salt_1"]
        val invalidSdJwt = FlatSdJwt.JWT + "${SdJwtSeparator}WyJ0ZXN0X3NhbHRfMSJd$SdJwtSeparator"
        val sdJwt = SdJwt(invalidSdJwt)

        assertThrows<IllegalStateException> {
            sdJwt.json
        }
    }

    @Test
    fun `parsing an SD-JWT with a disclosure with too many elements should  throw an exception`() = runTest {
        // ["test_salt_1", "test_key_1", "test_value_1", "test"]
        val invalidSdJwt = FlatSdJwt.JWT +
            "${SdJwtSeparator}WyJ0ZXN0X3NhbHRfMSIsICJ0ZXN0X2tleV8xIiwgInRlc3RfdmFsdWVfMSIsICJ0ZXN0Il0$SdJwtSeparator"
        val sdJwt = SdJwt(invalidSdJwt)

        assertThrows<IllegalStateException> {
            sdJwt.json
        }
    }

    @Test
    fun `parsing a random string should throw an exception`() = runTest {
        val invalidSdJwt = "foobar"
        val sdJwt = SdJwt(invalidSdJwt)

        assertThrows<IllegalStateException> {
            sdJwt.signedJWT
        }
    }

    @Test
    fun `parsing an empty string should throw an exception`() = runTest {
        val invalidSdJwt = ""
        val sdJwt = SdJwt(invalidSdJwt)

        assertThrows<IllegalStateException> {
            sdJwt.signedJWT
        }
    }

    @Test
    fun `parsing an SD-JWT where an _sd key references a json object should throw an exception`() =
        runTest {
            /*
            {
               "test":{
                  "_sd":{
                     "not_good":"true"
                  }
               },
               "_sd_alg":"sha-256"
            }
             */
            val jwt =
                "eyJhbGciOiJFUzUxMiIsInR5cCI6IkpXVCJ9.eyJ0ZXN0Ijp7Il9zZCI6eyJub3RfZ29vZCI6InRydWUifX0sIl9zZF9hbGciOiJzaGEtMjU2IiwiaWF0IjoxNjk3ODA5NzMxfQ.AaQdx2Pwj0jPE2Z8dCa9Jiam8tyzkOJb_5HCEZumuLRlh3nFtvmAxLGWBqYO54zotDOgGMH5WBdPuad5sJzdbWfHAbpFN6APM9FSNk3uk4C2qvb1osGeehE2REtJ1EjPOqFldgO36zqmMG8jSHm5YH9p1Xw4oYkeehXJpLL2qRsZPdZU"
            val sdJwt = SdJwt(jwt + FlatDisclosures)

            assertThrows<IllegalStateException> {
                sdJwt.json
            }
        }

    @Test
    fun `parsing an SD-JWT where an _sd key references a json primitive should throw an exception`() = runTest {
        /*
        {
           "test":{
              "_sd":"not good"
           },
           "_sd_alg":"sha-256"
        }
         */
        val jwt =
            "eyJhbGciOiJFUzUxMiIsInR5cCI6IkpXVCJ9.eyJ0ZXN0Ijp7Il9zZCI6Im5vdCBnb29kIn0sIl9zZF9hbGciOiJzaGEtMjU2IiwiaWF0IjoxNjk3ODEwODkzfQ.AZmwJlPifMvTWxUJfTrbnq4-lqzKPsrqi2CDjuIaDwSIeyouTcCEO5SNHfYQwlFEiMq5qpM5qUo6opVGbTOsge2HAH81GBymZR4n5cKPvMmIVe6rQ-fcdV-rfbV4RfEuXSla_qZGl6NR8CX9slVc3YRBr_UK7rgl_bGh_EH2sJAP19-N"
        val sdJwt = SdJwt(jwt + FlatDisclosures)

        assertThrows<IllegalStateException> {
            sdJwt.json
        }
    }

    private fun assertJsonEquals(expected: String, actualJson: JsonElement) {
        val expectedJson = Json.parseToJsonElement(expected)
        val filteredJson = actualJson.jsonObject.filterKeys { key ->
            key != ISSUED_AT_KEY && key != SD_ALGORITHM_KEY
        }
        assertEquals(expectedJson, filteredJson)
    }

    companion object {
        private const val ISSUED_AT_KEY = "iat"
        private const val SD_ALGORITHM_KEY = "_sd_alg"
    }
}

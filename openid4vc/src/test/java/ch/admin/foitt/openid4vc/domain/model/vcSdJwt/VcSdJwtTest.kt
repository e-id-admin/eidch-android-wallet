package ch.admin.foitt.openid4vc.domain.model.vcSdJwt

import ch.admin.foitt.openid4vc.util.SafeJsonTestInstance
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class VcSdJwtTest {
    @Test
    fun `Creating a VcSdJwt with a valid vcSdJwt succeeds`() = runTest {
        val vcSdJwt = VcSdJwt(VALID_VC_SD_JWT)

        assertEquals(KID, vcSdJwt.kid)
        assertEquals(ISS, vcSdJwt.iss)
        assertEquals(VCT, vcSdJwt.vct)
        assertEquals(SafeJsonTestInstance.json.parseToJsonElement(CNF), vcSdJwt.cnf)
        assertEquals(SafeJsonTestInstance.json.parseToJsonElement(STATUS), vcSdJwt.status)
    }

    @Test
    fun `Creating a VcSdJwt without the kid claim throws an exception`() = runTest {
        assertThrows<IllegalStateException> {
            VcSdJwt(VC_SD_JWT_MISSING_KID)
        }
    }

    @Test
    fun `Creating a VcSdJwt without the iss claim throws an exception`() = runTest {
        assertThrows<IllegalStateException> {
            VcSdJwt(VC_SD_JWT_MISSING_ISS)
        }
    }

    @Test
    fun `Creating a VcSdJwt without the vct claim throws an exception`() = runTest {
        assertThrows<IllegalStateException> {
            VcSdJwt(VC_SD_JWT_MISSING_VCT)
        }
    }

    @Test
    fun `Creating a VcSdJwt without the cnf claim succeeds but has a null field`() = runTest {
        val vcSdJwt = VcSdJwt(VC_SD_JWT_WITHOUT_CNF)
        assertNull(vcSdJwt.cnf)
    }

    @Test
    fun `Creating a VcSdJwt without the status claim succeeds but has a null field`() = runTest {
        val vcSdJwt = VcSdJwt(
            VC_SD_JWT_WITHOUT_STATUS
        )
        assertNull(vcSdJwt.status)
    }

    private companion object {
        const val KID = "keyId"
        const val ISS = "issuer"
        const val VCT = "vct"

        val CNF = """
          {
            "kty": "EC",
            "crv": "P-256",
            "x": "xValue",
            "y": "yValue"
          }
        """.trimIndent()

        val STATUS = """
          {
            "status_list": {
              "uri": "example.com",
              "idx": 1
            }
          }
        """.trimIndent()

        /*
        header:
        {
          "alg":"ES256",
          "typ":"type",
          "kid":"keyId"
        }
        payload:
        {
          "iss":"issuer",
          "vct":"vct",
          "cnf": {
            "kty": "EC",
            "crv": "P-256",
            "x": "xValue",
            "y": "yValue"
          },
          "status": {
            "status_list": {
              "uri": "example.com",
              "idx": 1
            }
          }
        }
         */
        const val VALID_VC_SD_JWT = "ewogICJhbGciOiJFUzI1NiIsCiAgInR5cCI6InR5cGUiLAogICJraWQiOiJrZXlJZCIKfQ.ewogICJpc3MiOiJpc3N1ZXIiLAogICJ2Y3QiOiJ2Y3QiLAogICJjbmYiOiB7CiAgICAia3R5IjogIkVDIiwKICAgICJjcnYiOiAiUC0yNTYiLAogICAgIngiOiAieFZhbHVlIiwKICAgICJ5IjogInlWYWx1ZSIKICB9LCAgCiAgInN0YXR1cyI6IHsKICAgICJzdGF0dXNfbGlzdCI6IHsKICAgICAgInVyaSI6ICJleGFtcGxlLmNvbSIsCiAgICAgICJpZHgiOiAxCiAgICB9CiAgfQp9.ZXdvZ0lDSmhiR2NpT2lKRlV6STFOaUlzQ2lBZ0luUjVjQ0k2SW5SNWNHVWlMQW9nSUNKcmFXUWlPaUpyWlhsSlpDSUtmUS4uUmx6cXJYa0xXMlhHWTN1V3lEeFJwQlZEWm9wNnhjcC1mV0lXd2tSWEtuOWRRa1lueDZSODRLX3B0MV9uR2kwaTQzekhRTXJ4aXZPX3ltOFNpeU44LWc"
        const val VC_SD_JWT_MISSING_ISS = "ewogICJhbGciOiJFUzI1NiIsCiAgInR5cCI6InR5cGUiLAogICJraWQiOiJrZXlJZCIKfQ.ewogICJ2Y3QiOiJ2Y3QiLAogICJjbmYiOiB7CiAgICAia3R5IjogIkVDIiwKICAgICJjcnYiOiAiUC0yNTYiLAogICAgIngiOiAieFZhbHVlIiwKICAgICJ5IjogInlWYWx1ZSIKICB9LCAgCiAgInN0YXR1cyI6IHsKICAgICJzdGF0dXNfbGlzdCI6IHsKICAgICAgInVyaSI6ICJleGFtcGxlLmNvbSIsCiAgICAgICJpZHgiOiAxCiAgICB9CiAgfQp9.ZXdvZ0lDSmhiR2NpT2lKRlV6STFOaUlzQ2lBZ0luUjVjQ0k2SW5SNWNHVWlMQW9nSUNKcmFXUWlPaUpyWlhsSlpDSUtmUS4uZ2lmbVd6NkZzMmdPbU9uNVNoLTI4M29wMEdXaW01Zm9FbmRLZkptRXZaQkNFd0h5STB6Q20tT1NqZG9MdmJzSTFuQ2wwYUVtSjN2b24tdGpDdHVBVUE"
        const val VC_SD_JWT_MISSING_KID = "ewogICJhbGciOiJFUzI1NiIsCiAgInR5cCI6InR5cGUiCn0.ewogICJpc3MiOiJpc3N1ZXIiLAogICJ2Y3QiOiJ2Y3QiLAogICJjbmYiOiB7CiAgICAia3R5IjogIkVDIiwKICAgICJjcnYiOiAiUC0yNTYiLAogICAgIngiOiAieFZhbHVlIiwKICAgICJ5IjogInlWYWx1ZSIKICB9LCAgCiAgInN0YXR1cyI6IHsKICAgICJzdGF0dXNfbGlzdCI6IHsKICAgICAgInVyaSI6ICJleGFtcGxlLmNvbSIsCiAgICAgICJpZHgiOiAxCiAgICB9CiAgfQp9.ZXdvZ0lDSmhiR2NpT2lKRlV6STFOaUlzQ2lBZ0luUjVjQ0k2SW5SNWNHVWlDbjAuLkJ3RDM5UzdJT0hvT1VhcDNJWmxCWlJOWFBPZGJIRkVTR2xwWG01RzEycHhOWnQ4VV9sbGZCYlcyMXloNFFlTEk2NXZLSlZYM1Y1QlFXcGk3dUtfMTNn"
        const val VC_SD_JWT_MISSING_VCT = "ewogICJhbGciOiJFUzI1NiIsCiAgInR5cCI6InR5cGUiLAogICJraWQiOiJrZXlJZCIKfQ.ewogICJpc3MiOiJpc3N1ZXIiLAogICJjbmYiOiB7CiAgICAia3R5IjogIkVDIiwKICAgICJjcnYiOiAiUC0yNTYiLAogICAgIngiOiAieFZhbHVlIiwKICAgICJ5IjogInlWYWx1ZSIKICB9LCAgCiAgInN0YXR1cyI6IHsKICAgICJzdGF0dXNfbGlzdCI6IHsKICAgICAgInVyaSI6ICJleGFtcGxlLmNvbSIsCiAgICAgICJpZHgiOiAxCiAgICB9CiAgfQp9.ZXdvZ0lDSmhiR2NpT2lKRlV6STFOaUlzQ2lBZ0luUjVjQ0k2SW5SNWNHVWlMQW9nSUNKcmFXUWlPaUpyWlhsSlpDSUtmUS4uYV9wMzZ3WHd5dFI2Z25UYUpYS2FOTDRQbi1sazZlcFZFLWwyRG9nVWNhSXIwbkZxYVk4T25vTWNTNDEzQkJHalhMQ1hmMzZWQXVSNDJwdjVIdGhMdlE"
        const val VC_SD_JWT_WITHOUT_CNF = "ewogICJhbGciOiJFUzI1NiIsCiAgInR5cCI6InR5cGUiLAogICJraWQiOiJrZXlJZCIKfQ.ewogICJpc3MiOiJpc3N1ZXIiLAogICJ2Y3QiOiJ2Y3QiLAogICJzdGF0dXMiOiB7CiAgICAic3RhdHVzX2xpc3QiOiB7CiAgICAgICJ1cmkiOiAiZXhhbXBsZS5jb20iLAogICAgICAiaWR4IjogMQogICAgfQogIH0KfQ.ZXdvZ0lDSmhiR2NpT2lKRlV6STFOaUlzQ2lBZ0luUjVjQ0k2SW5SNWNHVWlMQW9nSUNKcmFXUWlPaUpyWlhsSlpDSUtmUS4udzJval9aVEx1RUNueElwcG94cVBZSTE0U2wybklqeE9nbzdRcWFwYjZ3MVo3Y2U0cFJBQXdJczBUSnNocDhha2NuX05QRldBc28xVF9UU24zUTRKRnc"
        const val VC_SD_JWT_WITHOUT_STATUS = "ewogICJhbGciOiJFUzI1NiIsCiAgInR5cCI6InR5cGUiLAogICJraWQiOiJrZXlJZCIKfQ.ewogICJpc3MiOiJpc3N1ZXIiLAogICJ2Y3QiOiJ2Y3QiLAogICJjbmYiOiB7CiAgICAia3R5IjogIkVDIiwKICAgICJjcnYiOiAiUC0yNTYiLAogICAgIngiOiAieFZhbHVlIiwKICAgICJ5IjogInlWYWx1ZSIKICB9Cn0.ZXdvZ0lDSmhiR2NpT2lKRlV6STFOaUlzQ2lBZ0luUjVjQ0k2SW5SNWNHVWlMQW9nSUNKcmFXUWlPaUpyWlhsSlpDSUtmUS4uX1hWbE5jb292alMzUnZfaXBNZDdCaEdQR3RPSkpSNGRqOUJnOFNScFZnd3NrMWZPTXZkLVhzemwwUGtFZmpneFpyYkJIMkFSN2ZHbE9qZjFPUWNXSXc"
    }
}

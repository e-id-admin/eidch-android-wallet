package ch.admin.foitt.wallet.platform.trustRegistry

import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtError
import ch.admin.foitt.openid4vc.domain.usecase.VerifyJwtSignature
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialStatusError
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.FetchCredentialStatus
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateTrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation.ValidateTrustStatementImpl
import ch.admin.foitt.wallet.util.SafeJsonTestInstance
import ch.admin.foitt.wallet.util.assertErr
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class ValidateTrustStatementImplTest {

    @MockK
    private lateinit var mockEnvironmentSetup: EnvironmentSetupRepository

    @MockK
    private lateinit var mockVerifyJwtSignature: VerifyJwtSignature

    @MockK
    private lateinit var mockFetchCredentialStatus: FetchCredentialStatus

    private val testSafeJson = SafeJsonTestInstance.safeJson

    private lateinit var useCase: ValidateTrustStatement

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        useCase = ValidateTrustStatementImpl(
            environmentSetupRepo = mockEnvironmentSetup,
            verifyJwtSignature = mockVerifyJwtSignature,
            safeJson = testSafeJson,
            fetchCredentialStatus = mockFetchCredentialStatus
        )

        coEvery {
            mockEnvironmentSetup.trustedDids
        } returns trustedDids

        coEvery { mockVerifyJwtSignature.invoke(did = any(), kid = any(), jwt = any()) } returns Ok(Unit)
        coEvery {
            mockFetchCredentialStatus.invoke(credentialIssuer = any(), properties = any())
        } returns Ok(CredentialStatus.VALID)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `A valid trust statement pass validation`(): Unit = runTest {
        val result = useCase(validTrustStatement)
        result.assertOk()

        coVerify(exactly = 1) {
            mockEnvironmentSetup.trustedDids
            mockVerifyJwtSignature.invoke(did = any(), kid = any(), jwt = any())
        }
    }

    @Test
    fun `A trust statement not whitelisted fails validation`(): Unit = runTest {
        coEvery { mockEnvironmentSetup.trustedDids } returns listOf("did:twd:bbb")

        val result = useCase(validTrustStatement)
        result.assertErr()

        coVerify(exactly = 1) {
            mockEnvironmentSetup.trustedDids
        }
        coVerify(exactly = 0) {
            mockVerifyJwtSignature.invoke(did = any(), kid = any(), jwt = any())
        }
    }

    @Test
    fun `A trust statement declaring the wrong type fails validation`(): Unit = runTest {
        val result = useCase(wrongTypeTrustStatement)
        result.assertErr()
    }

    @Test
    fun `A trust statement declaring the wrong algorithm fails validation`(): Unit = runTest {
        val result = useCase(wrongAlgoTrustStatement)
        result.assertErr()
    }

    @Test
    fun `A trust statement with an invalid signature fails validation`(): Unit = runTest {
        coEvery {
            mockVerifyJwtSignature.invoke(did = any(), kid = any(), jwt = any())
        } returns Err(VcSdJwtError.InvalidJwt)

        val result = useCase(validTrustStatement)
        result.assertErr()

        coVerify(exactly = 1) {
            mockEnvironmentSetup.trustedDids
            mockVerifyJwtSignature.invoke(did = any(), kid = any(), jwt = any())
        }
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            missingIat,
            missingExp,
            missingNbf,
            missingIss
        ]
    )
    fun `A trust statement missing a mandatory reserved claim fails validation`(sdJwt: String): Unit = runTest {
        val result = useCase(sdJwt)
        result.assertErr()
    }

    @Test
    fun `A trust statement with an invalid validity fails validation`(): Unit = runTest {
        val result = useCase(expired)
        result.assertErr()
    }

    @Test
    fun `A trust statement with an unsupported vct claim value fails validation`(): Unit = runTest {
        val result = useCase(wrongVctValue)
        result.assertErr()
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            missingLogoUri,
            missingOrgName,
            missingPrefLang,
        ]
    )
    fun `A trust statement missing a mandatory claim fails validation`(sdJwt: String): Unit = runTest {
        val result = useCase(sdJwt)
        result.assertErr()
    }

    @Test
    fun `A trust statement with an unexpected type fails validation`(): Unit = runTest {
        val result = useCase(wrongFieldTypes)
        result.assertErr()
    }

    @Test
    fun `A trust statement missing status properties fails validation`(): Unit = runTest {
        val result = useCase(missingStatusProperties)
        result.assertErr()
        coVerify(exactly = 0) {
            mockFetchCredentialStatus.invoke(any(), any())
        }
    }

    @Test
    fun `A failed status list call fails validation`(): Unit = runTest {
        coEvery { mockFetchCredentialStatus.invoke(any(), any()) } returns Err(CredentialStatusError.NetworkError)
        val result = useCase(validTrustStatement)
        result.assertErr()
        coVerify(exactly = 1) {
            mockFetchCredentialStatus.invoke(any(), any())
        }
    }

    @Test
    fun `A trust statement with any other status than valid fails validation`(): Unit = runTest {
        coEvery { mockFetchCredentialStatus.invoke(any(), any()) } returns Ok(CredentialStatus.SUSPENDED)
        val result = useCase(validTrustStatement)
        result.assertErr()
        coVerify(exactly = 1) {
            mockFetchCredentialStatus.invoke(any(), any())
        }
    }

    private val trustedDids = listOf(
        "did:tdw:aaa",
        "did:tdw:abc",
    )

    companion object {
        private const val validTrustStatement =
            "eyJhbGciOiJFUzI1NiIsInR5cCI6InZjK3NkLWp3dCIsImtpZCI6ImRpZDp0ZHc6YWJjI2tleTAxIn0.eyJpc3MiOiJkaWQ6dGR3OmFiYyIsIm5iZiI6MCwiZXhwIjo5OTk5OTk5OTk5LCJpYXQiOjAsIl9zZF9hbGciOiJzaGEtMjU2Iiwic3ViIjoiZGlkOnRkdzphYmNkIiwib3JnTmFtZSI6eyJlbiI6Im9yZ05hbWUgRW4iLCJkZS1DSCI6Im9yZ05hbWUgRGUifSwicHJlZkxhbmciOiJkZSIsInZjdCI6IlRydXN0U3RhdGVtZW50TWV0YWRhdGFWMSIsImxvZ29VcmkiOnsiZW4iOiJsb2dvVXJpRW4iLCJkZSI6ImxvZ29VcmlEZSJ9LCJzdGF0dXMiOnsic3RhdHVzX2xpc3QiOnsiaWR4IjowLCJ1cmkiOiJ1cmkifX19.vKbMQpbERKJEyDGbsxJ_X0Y92ye_oxP9l0l_uZiwO9Uy2rQZe0F9EN8SFnTDQ8Qdd3Bk1aQ6eiYVGfngDcN9Ag"
        private const val wrongTypeTrustStatement =
            "eyJhbGciOiJFUzI1NiIsInR5cCI6InNkLWp3dCIsImtpZCI6ImRpZDp0ZHc6YWJjI2tleTAxIn0.eyJpc3MiOiJkaWQ6dGR3OmFiYyIsIm5iZiI6MCwiZXhwIjo5OTk5OTk5OTk5LCJpYXQiOjAsIl9zZF9hbGciOiJzaGEtMjU2Iiwic3ViIjoiZGlkOnRkdzphYmNkIiwib3JnTmFtZSI6eyJlbiI6Im9yZ05hbWUgRW4iLCJkZS1DSCI6Im9yZ05hbWUgRGUifSwicHJlZkxhbmciOiJkZSIsInZjdCI6IlRydXN0U3RhdGVtZW50TWV0YWRhdGFWMSIsImxvZ29VcmkiOnsiZW4iOiJsb2dvVXJpRW4iLCJkZSI6ImxvZ29VcmlEZSJ9fQ.7QSBHnU2q1YEqdazQjhWND0rX_5JyUzXQ9mhW4DjLjRGreRG_F-24s49UgqPsJqQ3pjw2aJriJfG9BZJ6JJkSQ"
        private const val wrongAlgoTrustStatement =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6InZjK3NkLWp3dCIsImtpZCI6ImRpZDp0ZHc6YWJjI2tleTAxIn0.eyJpc3MiOiJkaWQ6dGR3OmFiYyIsIm5iZiI6MCwiZXhwIjo5OTk5OTk5OTk5LCJpYXQiOjAsIl9zZF9hbGciOiJzaGEtMjU2Iiwic3ViIjoiZGlkOnRkdzphYmNkIiwib3JnTmFtZSI6eyJlbiI6Im9yZ05hbWUgRW4iLCJkZS1DSCI6Im9yZ05hbWUgRGUifSwicHJlZkxhbmciOiJkZSIsInZjdCI6IlRydXN0U3RhdGVtZW50TWV0YWRhdGFWMSIsImxvZ29VcmkiOnsiZW4iOiJsb2dvVXJpRW4iLCJkZSI6ImxvZ29VcmlEZSJ9fQ.kSvO__9qdqXb4hSc8y6nFMZ8CxuQAW3seMl4vIDtcjI"
        private const val missingIat =
            "eyJhbGciOiJFUzI1NiIsInR5cCI6InZjK3NkLWp3dCIsImtpZCI6ImRpZDp0ZHc6YWJjI2tleTAxIn0.eyJpc3MiOiJkaWQ6dGR3OmFiYyIsIm5iZiI6MCwiZXhwIjo5OTk5OTk5OTk5LCJfc2RfYWxnIjoic2hhLTI1NiIsInN1YiI6ImRpZDp0ZHc6YWJjZCIsIm9yZ05hbWUiOnsiZW4iOiJvcmdOYW1lIEVuIiwiZGUtQ0giOiJvcmdOYW1lIERlIn0sInByZWZMYW5nIjoiZGUiLCJ2Y3QiOiJUcnVzdFN0YXRlbWVudE1ldGFkYXRhVjEiLCJsb2dvVXJpIjp7ImVuIjoibG9nb1VyaUVuIiwiZGUiOiJsb2dvVXJpRGUifX0.NiDzA3tUhPAPp7qw5mCTTt4muAFv4lgTBND0jfI_V-KkeIW1qslFDBuMIUGx4m6nNyjGjjZ2LCILPrUbO1fgWQ"
        private const val missingNbf =
            "eyJhbGciOiJFUzI1NiIsInR5cCI6InZjK3NkLWp3dCIsImtpZCI6ImRpZDp0ZHc6YWJjI2tleTAxIn0.eyJpc3MiOiJkaWQ6dGR3OmFiYyIsImV4cCI6OTk5OTk5OTk5OSwiaWF0IjowLCJfc2RfYWxnIjoic2hhLTI1NiIsInN1YiI6ImRpZDp0ZHc6YWJjZCIsIm9yZ05hbWUiOnsiZW4iOiJvcmdOYW1lIEVuIiwiZGUtQ0giOiJvcmdOYW1lIERlIn0sInByZWZMYW5nIjoiZGUiLCJ2Y3QiOiJUcnVzdFN0YXRlbWVudE1ldGFkYXRhVjEiLCJsb2dvVXJpIjp7ImVuIjoibG9nb1VyaUVuIiwiZGUiOiJsb2dvVXJpRGUifX0.HBzmUa9ISihmMXTBd6Npd3_iVAuwQIZpB6dokfbsbPRCEMNRdXMLZzpZQf-8oVVrSTc4QPJtNjOkVxWlJo1v2Q"
        private const val missingExp =
            "eyJhbGciOiJFUzI1NiIsInR5cCI6InZjK3NkLWp3dCIsImtpZCI6ImRpZDp0ZHc6YWJjI2tleTAxIn0.eyJpc3MiOiJkaWQ6dGR3OmFiYyIsIm5iZiI6MCwiaWF0IjowLCJfc2RfYWxnIjoic2hhLTI1NiIsInN1YiI6ImRpZDp0ZHc6YWJjZCIsIm9yZ05hbWUiOnsiZW4iOiJvcmdOYW1lIEVuIiwiZGUtQ0giOiJvcmdOYW1lIERlIn0sInByZWZMYW5nIjoiZGUiLCJ2Y3QiOiJUcnVzdFN0YXRlbWVudE1ldGFkYXRhVjEiLCJsb2dvVXJpIjp7ImVuIjoibG9nb1VyaUVuIiwiZGUiOiJsb2dvVXJpRGUifX0.fRh-6Rur8iM3v9Wzc-ua6qe2whqR9AEf50BHXWWr72qX4p7Y5fHbOVTZUm9vfH92bPD7j7AqkvzK3IPvK6FnZA"
        private const val missingIss =
            "eyJhbGciOiJFUzI1NiIsInR5cCI6InZjK3NkLWp3dCIsImtpZCI6ImRpZDp0ZHc6YWJjI2tleTAxIn0.eyJuYmYiOjAsImV4cCI6OTk5OTk5OTk5OSwiaWF0IjowLCJfc2RfYWxnIjoic2hhLTI1NiIsInN1YiI6ImRpZDp0ZHc6YWJjZCIsIm9yZ05hbWUiOnsiZW4iOiJvcmdOYW1lIEVuIiwiZGUtQ0giOiJvcmdOYW1lIERlIn0sInByZWZMYW5nIjoiZGUiLCJ2Y3QiOiJUcnVzdFN0YXRlbWVudE1ldGFkYXRhVjEiLCJsb2dvVXJpIjp7ImVuIjoibG9nb1VyaUVuIiwiZGUiOiJsb2dvVXJpRGUifX0.sKESDlYr_gYfVjHUL715GUbvQvbDXfqPgcUpl-ps_ktEvBujaOrYiDSxSI6jldv7fNx5zVtPP6vUQiIpOxnUtQ"
        private const val expired =
            "eyJhbGciOiJFUzI1NiIsInR5cCI6InZjK3NkLWp3dCIsImtpZCI6ImRpZDp0ZHc6YWJjI2tleTAxIn0.eyJpc3MiOiJkaWQ6dGR3OmFiYyIsIm5iZiI6MCwiZXhwIjoxLCJpYXQiOjAsIl9zZF9hbGciOiJzaGEtMjU2Iiwic3ViIjoiZGlkOnRkdzphYmNkIiwib3JnTmFtZSI6eyJlbiI6Im9yZ05hbWUgRW4iLCJkZS1DSCI6Im9yZ05hbWUgRGUifSwicHJlZkxhbmciOiJkZSIsInZjdCI6IlRydXN0U3RhdGVtZW50TWV0YWRhdGFWMSIsImxvZ29VcmkiOnsiZW4iOiJsb2dvVXJpRW4iLCJkZSI6ImxvZ29VcmlEZSJ9fQ.4rSXYbpAxBpXaGfIky_fCtU1PweXURI4-Cvea6EDLv8Kf3CBrmpoZb7pDTyhb0VEedu14rp45k-wF5R2Gzjpzw"
        private const val wrongVctValue =
            "eyJhbGciOiJFUzI1NiIsInR5cCI6InZjK3NkLWp3dCIsImtpZCI6ImRpZDp0ZHc6YWJjI2tleTAxIn0.eyJpc3MiOiJkaWQ6dGR3OmFiYyIsIm5iZiI6MCwiZXhwIjo5OTk5OTk5OTk5LCJpYXQiOjAsIl9zZF9hbGciOiJzaGEtMjU2Iiwic3ViIjoiZGlkOnRkdzphYmNkIiwib3JnTmFtZSI6eyJlbiI6Im9yZ05hbWUgRW4iLCJkZS1DSCI6Im9yZ05hbWUgRGUifSwicHJlZkxhbmciOiJkZSIsInZjdCI6IlNvbWVUeXBlIiwibG9nb1VyaSI6eyJlbiI6ImxvZ29VcmlFbiIsImRlIjoibG9nb1VyaURlIn19.WQauSJEOIljNcojD59vhRwDKQiR2nes7PIDXqI14wVJ_njt6jD8mNsqoJko_rammXxwwaL_Zo1QOlfnkXIoWew"
        private const val missingOrgName =
            "eyJhbGciOiJFUzI1NiIsInR5cCI6InZjK3NkLWp3dCIsImtpZCI6ImRpZDp0ZHc6YWJjI2tleTAxIn0.eyJpc3MiOiJkaWQ6dGR3OmFiYyIsIm5iZiI6MCwiZXhwIjo5OTk5OTk5OTk5LCJpYXQiOjAsIl9zZF9hbGciOiJzaGEtMjU2Iiwic3ViIjoiZGlkOnRkdzphYmNkIiwib3JnTmFtZSI6eyJlbiI6Im9yZ05hbWUgRW4iLCJkZS1DSCI6Im9yZ05hbWUgRGUifSwicHJlZkxhbmciOiJkZSIsInZjdCI6IlRydXN0U3RhdGVtZW50TWV0YWRhdGFWMSJ9.sLm7z9FSmaeTBXA9pgmkaa62TOMs6kHpjH4QEdyhHv7bqpi-O82ngyvb_R7yDgI6Gv_sRhH-h4E1EwSEO3lYIA"
        private const val missingLogoUri =
            "eyJhbGciOiJFUzI1NiIsInR5cCI6InZjK3NkLWp3dCIsImtpZCI6ImRpZDp0ZHc6YWJjI2tleTAxIn0.eyJpc3MiOiJkaWQ6dGR3OmFiYyIsIm5iZiI6MCwiZXhwIjo5OTk5OTk5OTk5LCJpYXQiOjAsIl9zZF9hbGciOiJzaGEtMjU2Iiwic3ViIjoiZGlkOnRkdzphYmNkIiwicHJlZkxhbmciOiJkZSIsInZjdCI6IlRydXN0U3RhdGVtZW50TWV0YWRhdGFWMSIsImxvZ29VcmkiOnsiZW4iOiJsb2dvVXJpRW4iLCJkZSI6ImxvZ29VcmlEZSJ9fQ.acxmQSU39zIhvlT0DNrzW79KHpRCl5t_i0uWRtmpfLf5aXswFN-vT2m4eHitS9f3zpZeZ_MMlNPilTbmlk1_0A"
        private const val missingPrefLang =
            "eyJhbGciOiJFUzI1NiIsInR5cCI6InZjK3NkLWp3dCIsImtpZCI6ImRpZDp0ZHc6YWJjI2tleTAxIn0.eyJpc3MiOiJkaWQ6dGR3OmFiYyIsIm5iZiI6MCwiZXhwIjo5OTk5OTk5OTk5LCJpYXQiOjAsIl9zZF9hbGciOiJzaGEtMjU2Iiwic3ViIjoiZGlkOnRkdzphYmNkIiwib3JnTmFtZSI6eyJlbiI6Im9yZ05hbWUgRW4iLCJkZS1DSCI6Im9yZ05hbWUgRGUifSwidmN0IjoiVHJ1c3RTdGF0ZW1lbnRNZXRhZGF0YVYxIiwibG9nb1VyaSI6eyJlbiI6ImxvZ29VcmlFbiIsImRlIjoibG9nb1VyaURlIn19.NRMDJN5yR_MaL0Ca27TYeRY_1th-fiZu3COI36oXSRjWmZWqXH0_ru9M2c3SvrefwLGxaDPTO7ciLAVVTuY1ag"
        private const val wrongFieldTypes =
            "eyJhbGciOiJFUzI1NiIsInR5cCI6InZjK3NkLWp3dCIsImtpZCI6ImRpZDp0ZHc6YWJjI2tleTAxIn0.eyJpc3MiOiJkaWQ6dGR3OmFiYyIsIm5iZiI6MCwiZXhwIjo5OTk5OTk5OTk5LCJpYXQiOjAsIl9zZF9hbGciOiJzaGEtMjU2Iiwic3ViIjoiZGlkOnRkdzphYmNkIiwib3JnTmFtZSI6Im15IG9yZyBuYW1lIiwicHJlZkxhbmciOiJkZSIsInZjdCI6IlRydXN0U3RhdGVtZW50TWV0YWRhdGFWMSIsImxvZ29VcmkiOiJteSBsb2dvIFVyaSJ9.gOkip7Y-9KK8PtdO9-mIG6t1h0Yhe1g3Uo4XgJtyQeCGfzcQnRiC79JbNC1iMNIqcdaPNWAqp5ZnbzdXUeXs3Q"
        private const val missingStatusProperties =
            "eyJhbGciOiJFUzI1NiIsInR5cCI6InZjK3NkLWp3dCIsImtpZCI6ImRpZDp0ZHc6YWJjI2tleTAxIn0.eyJpc3MiOiJkaWQ6dGR3OmFiYyIsIm5iZiI6MCwiZXhwIjo5OTk5OTk5OTk5LCJpYXQiOjAsIl9zZF9hbGciOiJzaGEtMjU2Iiwic3ViIjoiZGlkOnRkdzphYmNkIiwib3JnTmFtZSI6eyJlbiI6Im9yZ05hbWUgRW4iLCJkZS1DSCI6Im9yZ05hbWUgRGUifSwicHJlZkxhbmciOiJkZSIsInZjdCI6IlRydXN0U3RhdGVtZW50TWV0YWRhdGFWMSIsImxvZ29VcmkiOnsiZW4iOiJsb2dvVXJpRW4iLCJkZSI6ImxvZ29VcmlEZSJ9fQ.TLMEnH5lq5cXyrwetzd2wNoIkapb-cIWMG-2zvKFfRneMo_llk7wwKV-7opQ96OGtx9degPq_wD9t0aLRZQI0g"
        //region Trust statement source

/* Trust statement content
header:

{
  "alg": "ES256",
  "typ": "vc+sd-jwt",
  "kid": "did:tdw:abc#key01"
}

payload
{
  "iss": "did:tdw:abc",
  "nbf": 0,
  "exp": 9999999999,
  "iat": 0,
  "_sd_alg": "sha-256",
  "sub": "did:tdw:abcd",
  "orgName": {
    "en": "orgName En",
    "de-CH": "orgName De"
  },
  "prefLang": "de",
  "vct": "TrustStatementMetadataV1",
  "logoUri": {
    "en": "logoUriEn",
    "de": "logoUriDe"
  },
  "status":{
      "status_list":{
         "idx":0,
         "uri":"uri"
      }
   }
}

-----BEGIN PUBLIC KEY-----
MFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEEVs/o5+uQbTjL3chynL4wXgUg2R9
q9UU8I5mEovUf86QZ7kOBIjJwqnzD1omageEHWwHdBO6B+dFabmdT9POxg==
-----END PUBLIC KEY-----
-----BEGIN PRIVATE KEY-----
MIGHAgEAMBMGByqGSM49AgEGCCqGSM49AwEHBG0wawIBAQQgevZzL1gdAFr88hb2
OF/2NxApJCzGCEDdfSp6VQO30hyhRANCAAQRWz+jn65BtOMvdyHKcvjBeBSDZH2r
1RTwjmYSi9R/zpBnuQ4EiMnCqfMPWiZqB4QdbAd0E7oH50VpuZ1P087G
-----END PRIVATE KEY-----
 */
        //endregion
    }
}

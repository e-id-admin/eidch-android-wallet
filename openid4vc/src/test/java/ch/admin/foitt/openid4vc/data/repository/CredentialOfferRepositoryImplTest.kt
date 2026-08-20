package ch.admin.foitt.openid4vc.data.repository

import android.annotation.SuppressLint
import android.webkit.URLUtil
import ch.admin.foitt.openid4vc.data.CredentialOfferRepositoryImpl
import ch.admin.foitt.openid4vc.data.repository.mock.CredentialOfferRepoMocks.mockSoftwareKeyPair
import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.TokenType
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialResponse
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.JWSKeyPair
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.TokenResponse
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBindingType
import ch.admin.foitt.openid4vc.domain.model.payloadEncryption.PayloadEncryption
import ch.admin.foitt.openid4vc.domain.model.payloadEncryption.PayloadEncryptionKeyPair
import ch.admin.foitt.openid4vc.domain.repository.CredentialOfferRepository
import ch.admin.foitt.openid4vc.domain.usecase.jwe.DecryptJWE
import ch.admin.foitt.openid4vc.util.SafeJsonTestInstance
import ch.admin.foitt.openid4vc.util.assertErrorType
import ch.admin.foitt.openid4vc.util.assertOk
import ch.admin.foitt.openid4vc.util.assertTrue
import ch.admin.foitt.openid4vc.utils.ContentType.applicationJwt
import ch.admin.foitt.openid4vc.utils.content
import com.github.michaelbull.result.Ok
import com.nimbusds.jose.CompressionAlgorithm
import com.nimbusds.jose.EncryptionMethod
import com.nimbusds.jose.JWEAlgorithm
import com.nimbusds.jose.JWEHeader
import com.nimbusds.jose.JWEObject
import com.nimbusds.jose.Payload
import com.nimbusds.jose.crypto.ECDHEncrypter
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.net.URI
import java.net.URL
import java.security.KeyPair
import java.security.interfaces.ECPublicKey

class CredentialOfferRepositoryImplTest {

    private lateinit var handler: (suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData)

    @MockK
    private lateinit var mockDecryptJWE: DecryptJWE

    private val json = SafeJsonTestInstance.safeJson

    private val mockHttpClient by lazy {

        HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    handler(request)
                }
            }
        }
    }

    private lateinit var repo: CredentialOfferRepository

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        mockkStatic(URLUtil::class)
        every { URLUtil.isHttpsUrl(any()) } returns true

        repo = CredentialOfferRepositoryImpl(
            httpClient = mockHttpClient,
            safeJson = json,
            decryptJWE = mockDecryptJWE,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Fetching issuer metadata correctly sets Accept-Language header`() = runTest {
        handler = { request ->
            when {
                request.isAcceptLanguageIssuerResponse() -> respond(
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, applicationJwt.content),
                    content = ISSUER_METADATA_JWT_ACCEPT_LANGUAGE
                )

                else -> error("Unhandled request: ${request.url} -> add in mockHttpClient")
            }
        }

        val url = URI.create("$BASE_URL$ISSUER_ACCEPT_LANGUAGE").toURL()

        val result = repo.fetchRawAndParsedIssuerCredentialInformation(
            issuerEndpoint = url,
        ).assertOk()

        val expected = "$BASE_URL$ISSUER_PATH$ISSUER_ACCEPT_LANGUAGE"

        assertEquals(expected, result.issuerCredentialInfo.credentialIssuer.toString())
    }

    @Test
    fun `Fetching signed issuer metadata returns info`() = runTest {
        handler = { request ->
            when {
                request.isSignedMetadataIssuerResponse() -> respond(
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/jwt"),
                    content = ISSUER_METADATA_JWT_OID4VCI
                )

                else -> error("Unhandled request: ${request.url} -> add in mockHttpClient")
            }
        }

        val url = URI.create(BASE_URL).toURL()

        val result = repo.fetchRawAndParsedIssuerCredentialInformation(issuerEndpoint = url).assertOk()

        assertEquals("$BASE_URL$ISSUER_PATH$ISSUER_OID4VCI", result.issuerCredentialInfo.credentialIssuer.toString())
        assertEquals(ISSUER_METADATA_JWT_OID4VCI, result.rawIssuerCredentialInfo.rawJwt)
    }

    @Test
    fun `Fetching credential issuer metadata uses OID4VCI url as first prio`() = runTest {
        handler = { request ->
            when {
                request.isMetadataOID4VCIIssuerResponse() -> respond(
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, applicationJwt.content),
                    content = ISSUER_METADATA_JWT_OID4VCI
                )

                else -> error("Unhandled request: ${request.url} -> add in mockHttpClient")
            }
        }

        val url = URI.create("$BASE_URL$ISSUER_OID4VCI").toURL()

        val result = repo.fetchRawAndParsedIssuerCredentialInformation(url).assertOk()

        val expected = "$BASE_URL$ISSUER_PATH$ISSUER_OID4VCI"

        assertEquals(expected, result.issuerCredentialInfo.credentialIssuer.toString())
    }

    @Test
    fun `Fetching credential issuer metadata uses OIDC url as second prio`() = runTest {
        handler = { request ->
            when {
                request.isMetadataOID4VCIIssuerErrorResponse() -> throw IOException("network failure")

                request.isMetadataOIDCIssuerResponse() -> respond(
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, applicationJwt.content),
                    content = ISSUER_METADATA_JWT_OIDC
                )

                else -> error("Unhandled request: ${request.url} -> add in mockHttpClient")
            }
        }

        val url = URI.create("$BASE_URL$ISSUER_OIDC").toURL()

        val result = repo.fetchRawAndParsedIssuerCredentialInformation(url).assertOk()

        val expected = "$BASE_URL$ISSUER_PATH$ISSUER_OIDC"

        assertEquals(expected, result.issuerCredentialInfo.credentialIssuer.toString())
    }

    @Test
    fun `Fetching credential issuer metadata returns error if both return an error`() = runTest {
        handler = { request ->
            when {
                request.isMetadataOID4VCIOtherIssuerErrorResponse() -> throw IOException("network failure")
                request.isMetadataOIDCOtherIssuerErrorResponse() -> throw IOException("network failure")
                else -> error("Unhandled request: ${request.url} -> add in mockHttpClient")
            }
        }

        val url = URI.create("$BASE_URL/otherIssuer").toURL()

        repo.fetchRawAndParsedIssuerCredentialInformation(url).assertErrorType(CredentialOfferError.NetworkInfoError::class)
    }

    @Test
    fun `Fetching credential issuer config uses OID4VCI url as first prio`() = runTest {
        handler = { request ->
            when {
                request.isConfigOID4VCIIssuerResponse() -> respond(
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, applicationJwt.content),
                    content = CREDENTIAL_ISSUER_CONFIG_OID4VCI_RESPONSE_JWT
                )

                else -> error("Unhandled request: ${request.url} -> add in mockHttpClient")
            }
        }

        val url = URI.create("$BASE_URL$ISSUER_OID4VCI").toURL()

        val result = repo.fetchIssuerConfiguration(url).assertOk()

        val expected = "$BASE_URL$ISSUER_PATH$ISSUER_OID4VCI"

        assertEquals(expected, result.config.issuer.toString())
    }

    @Test
    fun `Fetching credential issuer config uses OIDC url as second prio`() = runTest {
        handler = { request ->
            when {
                request.isConfigOID4VCIIssuerErrorResponse() -> throw IOException("network failure")

                request.isConfigOIDCIssuerResponse() -> respond(
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, applicationJwt.content),
                    content = CREDENTIAL_ISSUER_CONFIG_OIDC_RESPONSE_JWT
                )

                else -> error("Unhandled request: ${request.url} -> add in mockHttpClient")
            }
        }

        val url = URI.create("$BASE_URL$ISSUER_OIDC").toURL()

        val result = repo.fetchIssuerConfiguration(url).assertOk()

        val expected = "$BASE_URL$ISSUER_PATH$ISSUER_OIDC"

        assertEquals(expected, result.config.issuer.toString())
    }

    @Test
    fun `Fetching credential issuer config returns error if both return an error`() = runTest {
        handler = { request ->
            when {
                request.isConfigOID4VCIOtherIssuerErrorResponse() -> throw IOException("network failure")
                request.isConfigOIDCOtherIssuerErrorResponse() -> throw IOException("network failure")
                else -> error("Unhandled request: ${request.url} -> add in mockHttpClient")
            }
        }

        val url = URI.create("$BASE_URL/otherIssuer").toURL()

        repo.fetchIssuerConfiguration(url).assertErrorType(CredentialOfferError.NetworkInfoError::class)
    }

    @Test
    fun `Fetching verifiable credential returns verifiable credential result`() = runTest {
        val payloadEncryption = mockPayloadEncryption(verifiableCredentialResponseJson)

        val result = repo.fetchCredential(
            issuerEndpoint = verifiableCredentialUrl,
            tokenResponse = tokenResponse,
            request = "credentialRequest",
            payloadEncryption = payloadEncryption,
        ).assertOk()

        assertTrue(result is CredentialResponse.VerifiableCredential) { "response is not a verifiable credential" }
        val credential = (result as CredentialResponse.VerifiableCredential).credentials.first().credential
        assertEquals("credentialJwt", credential)
    }

    @Test
    fun `Fetching deferred credential returns deferred credential result`() = runTest {
        val payloadEncryption = mockPayloadEncryption(deferredCredentialResponseJson, true)

        val result = repo.fetchCredential(
            issuerEndpoint = deferredCredentialUrl,
            tokenResponse = tokenResponse,
            request = "credentialRequest",
            payloadEncryption = payloadEncryption,
        ).assertOk()

        assertTrue(result is CredentialResponse.DeferredCredential) { "response is not a deferred credential" }
        val transactionId = (result as CredentialResponse.DeferredCredential).transactionId
        assertEquals("trxId", transactionId)
    }

    @Test
    fun `Receiving a verifiable credential response with empty credential array returns an error`() = runTest {
        val payloadEncryption = mockPayloadEncryption(emptyCredentialResponseJson)

        repo.fetchCredential(
            issuerEndpoint = verifiableCredentialUrl,
            tokenResponse = tokenResponse,
            request = "credentialRequest",
            payloadEncryption = payloadEncryption,
        ).assertErrorType(CredentialOfferError.Unexpected::class)
    }

    @Test
    fun `Receiving a credential with an invalid json structure returns an error`() = runTest {
        val payloadEncryption = mockPayloadEncryption(invalidCredentialResponseJson)

        repo.fetchCredential(
            issuerEndpoint = verifiableCredentialUrl,
            tokenResponse = tokenResponse,
            request = "credentialRequest",
            payloadEncryption = payloadEncryption,
        ).assertErrorType(CredentialOfferError.Unexpected::class)
    }

    @Test
    fun `Network error during fetching credential returns an error`() = runTest {
        handler = { request ->
            when {
                request.isErrorResponse() -> throw IOException("network failure")
                else -> error("Unhandled request: ${request.url} -> add in mockHttpClient")
            }
        }

        repo.fetchCredential(
            issuerEndpoint = errorUrl,
            tokenResponse = tokenResponse,
            request = "credentialRequest",
            payloadEncryption = mockk(),
        ).assertErrorType(CredentialOfferError.NetworkInfoError::class)
    }

    @SuppressLint("CheckResult")
    @ParameterizedTest(name = "issuerEndpoint={0} -> IETF path={1}")
    @MethodSource("issuerMetadataIetfPaths")
    fun `Issuer metadata IETF request path is sanitized for trailing slashes and empty segments`(
        issuerEndpoint: String,
        expectedPath: String,
    ) = runTest {
        lateinit var requestedPath: String
        handler = { request ->
            requestedPath = request.url.encodedPath
            respond(
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, applicationJwt.content),
                content = ISSUER_METADATA_JWT_OID4VCI,
            )
        }

        repo.fetchRawAndParsedIssuerCredentialInformation(URI.create(issuerEndpoint).toURL())

        assertEquals(expectedPath, requestedPath)
    }

    @SuppressLint("CheckResult")
    @ParameterizedTest(name = "issuerEndpoint={0} -> OIDC fallback path={1}")
    @MethodSource("issuerMetadataOidcFallbackPaths")
    fun `Issuer metadata OIDC fallback request path is sanitized for trailing slashes and empty segments`(
        issuerEndpoint: String,
        expectedPath: String,
    ) = runTest {
        lateinit var requestedPath: String
        var firstCall = true
        handler = { request ->
            if (firstCall) {
                firstCall = false
                throw IOException("force OIDC fallback")
            }
            requestedPath = request.url.encodedPath
            respond(
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, applicationJwt.content),
                content = ISSUER_METADATA_JWT_OID4VCI,
            )
        }

        repo.fetchRawAndParsedIssuerCredentialInformation(URI.create(issuerEndpoint).toURL())

        assertEquals(expectedPath, requestedPath)
    }

    private fun mockPayloadEncryption(payload: String, isDeferred: Boolean = false): PayloadEncryption {
        val keyPair = mockSoftwareKeyPair
        val httpResponseString = createCredentialJwe(keyPair, payload)

        handler = { request ->
            val status = when {
                isDeferred && request.isDeferredCredentialRequestWithResponseEncryption() -> HttpStatusCode.Accepted
                !isDeferred && request.isVerifiableCredentialRequestWithResponseEncryption() -> HttpStatusCode.OK
                else -> error("Unhandled request: ${request.url} -> add in mockHttpClient")
            }
            respond(
                status = status,
                headers = headersOf(HttpHeaders.ContentType, applicationJwt.content),
                content = httpResponseString,
            )
        }

        coEvery { mockDecryptJWE(httpResponseString, keyPair.private) } returns Ok(payload)
        return createResponseEncryption(keyPair)
    }

    private fun createCredentialJwe(keyPair: KeyPair, credentialResponse: String): String {
        val jweHeader = JWEHeader.Builder(JWEAlgorithm.ECDH_ES, EncryptionMethod.A256GCM)
            .compressionAlgorithm(CompressionAlgorithm.DEF)
            .build()
        val jwePayload = Payload(credentialResponse)
        val jwe = JWEObject(jweHeader, jwePayload)
        jwe.encrypt(ECDHEncrypter(keyPair.public as ECPublicKey))
        return jwe.serialize()
    }

    private fun createResponseEncryption(keyPair: KeyPair): PayloadEncryption = PayloadEncryption(
        requestEncryption = mockk(),
        responseEncryption = mockk(),
        responseEncryptionKeyPair = PayloadEncryptionKeyPair(
            keyPair = JWSKeyPair(
                algorithm = SigningAlgorithm.ES256,
                keyPair = keyPair,
                keyId = "keyId",
                bindingType = KeyBindingType.SOFTWARE
            ),
            alg = "alg",
            enc = "enc",
            zip = "zip"
        )
    )

    private fun HttpRequestData.isAcceptLanguageIssuerResponse() =
        this.method == HttpMethod.Get && this.url.encodedPath == "$ISSUER_METADATA_PATH$ISSUER_ACCEPT_LANGUAGE" &&
            this.headers[HttpHeaders.AcceptLanguage] == "de-CH, en, fr-CH, it-CH, rm"

    private fun HttpRequestData.isSignedMetadataIssuerResponse() = mockResponse(expectedPath = ISSUER_METADATA_PATH)

    private fun HttpRequestData.isMetadataOID4VCIIssuerResponse() = mockResponse(expectedPath = "$ISSUER_METADATA_PATH$ISSUER_OID4VCI")

    private fun HttpRequestData.isMetadataOID4VCIIssuerErrorResponse() = mockResponse(expectedPath = "$ISSUER_METADATA_PATH$ISSUER_OIDC")

    private fun HttpRequestData.isMetadataOIDCIssuerResponse() = mockResponse(expectedPath = "$ISSUER_OIDC$ISSUER_METADATA_PATH")

    private fun HttpRequestData.isMetadataOID4VCIOtherIssuerErrorResponse() = mockResponse(
        expectedPath = "$ISSUER_METADATA_PATH/otherIssuer"
    )

    private fun HttpRequestData.isMetadataOIDCOtherIssuerErrorResponse() = mockResponse(expectedPath = "/otherIssuer$ISSUER_METADATA_PATH")

    private fun HttpRequestData.isConfigOID4VCIIssuerResponse() = mockResponse(expectedPath = "$ISSUER_CONFIG_PATH$ISSUER_OID4VCI")

    private fun HttpRequestData.isConfigOID4VCIIssuerErrorResponse() = mockResponse(expectedPath = "$ISSUER_CONFIG_PATH$ISSUER_OIDC")

    private fun HttpRequestData.isConfigOIDCIssuerResponse() = mockResponse(expectedPath = "$ISSUER_OIDC$ISSUER_CONFIG_PATH")

    private fun HttpRequestData.isConfigOID4VCIOtherIssuerErrorResponse() = mockResponse(expectedPath = "$ISSUER_CONFIG_PATH/otherIssuer")

    private fun HttpRequestData.isConfigOIDCOtherIssuerErrorResponse() = mockResponse(expectedPath = "/otherIssuer$ISSUER_CONFIG_PATH")

    private fun HttpRequestData.isVerifiableCredentialRequestWithResponseEncryption() = mockResponse(
        method = HttpMethod.Post,
        expectedPath = "$CREDENTIAL_PATH$VERIFIABLE_PATH",
        contentType = applicationJwt.content
    )

    private fun HttpRequestData.isDeferredCredentialRequestWithResponseEncryption() = mockResponse(
        method = HttpMethod.Post,
        expectedPath = "$CREDENTIAL_PATH$DEFERRED_PATH",
        contentType = applicationJwt.content
    )

    private fun HttpRequestData.isErrorResponse() = mockResponse(method = HttpMethod.Post, expectedPath = "$CREDENTIAL_PATH$ERROR_PATH")

    private fun HttpRequestData.mockResponse(
        method: HttpMethod = HttpMethod.Get,
        expectedPath: String,
        contentType: String? = null
    ): Boolean {
        val result = this.method == method && this.url.encodedPath == expectedPath

        return if (contentType != null) {
            result && this.body.contentType?.content == contentType
        } else {
            result
        }
    }

    private companion object {
        const val ISSUER_METADATA_PATH = "/.well-known/openid-credential-issuer"
        const val ISSUER_CONFIG_PATH = "/.well-known/oauth-authorization-server"
        const val ISSUER_ACCEPT_LANGUAGE = "/issuerAcceptLanguage"
        const val ISSUER_OID4VCI = "/issuerOID4VCI"
        const val ISSUER_OIDC = "/issuerOIDC"

        @JvmStatic
        fun issuerMetadataIetfPaths() = listOf(
            Arguments.of("$BASE_URL$ISSUER_OID4VCI", "$ISSUER_METADATA_PATH$ISSUER_OID4VCI"),
            Arguments.of("$BASE_URL$ISSUER_OID4VCI/", "$ISSUER_METADATA_PATH$ISSUER_OID4VCI"),
            Arguments.of("$BASE_URL/", ISSUER_METADATA_PATH),
            Arguments.of(BASE_URL, ISSUER_METADATA_PATH),
            // empty path segments are collapsed
            Arguments.of("$BASE_URL/$ISSUER_OID4VCI", "$ISSUER_METADATA_PATH$ISSUER_OID4VCI"),
            Arguments.of("$BASE_URL$ISSUER_OID4VCI//", "$ISSUER_METADATA_PATH$ISSUER_OID4VCI"),
            Arguments.of("$BASE_URL///", ISSUER_METADATA_PATH),
        )

        @JvmStatic
        fun issuerMetadataOidcFallbackPaths() = listOf(
            Arguments.of("$BASE_URL$ISSUER_OID4VCI", "$ISSUER_OID4VCI$ISSUER_METADATA_PATH"),
            Arguments.of("$BASE_URL$ISSUER_OID4VCI/", "$ISSUER_OID4VCI$ISSUER_METADATA_PATH"),
            Arguments.of("$BASE_URL/", ISSUER_METADATA_PATH),
            Arguments.of(BASE_URL, ISSUER_METADATA_PATH),
            // empty path segments are collapsed
            Arguments.of("$BASE_URL/$ISSUER_OID4VCI", "$ISSUER_OID4VCI$ISSUER_METADATA_PATH"),
            Arguments.of("$BASE_URL$ISSUER_OID4VCI//", "$ISSUER_OID4VCI$ISSUER_METADATA_PATH"),
            Arguments.of("$BASE_URL///", ISSUER_METADATA_PATH),
        )

        /*
        {
          "credential_issuer": "https://example.com/issuer/issuerAcceptLanguage",
          [...]
        }
         */
        const val ISSUER_METADATA_JWT_ACCEPT_LANGUAGE =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiJ9.eyJjcmVkZW50aWFsX2VuZHBvaW50IjoiaHR0cHM6Ly9leGFtcGxlLmNvbS9jcmVkZW50aWFsL2VuZHBvaW50IiwiY3JlZGVudGlhbF9pc3N1ZXIiOiJodHRwczovL2V4YW1wbGUuY29tL2lzc3Vlci9pc3N1ZXJBY2NlcHRMYW5ndWFnZSIsIm5vbmNlX2VuZHBvaW50IjoiaHR0cHM6Ly9leGFtcGxlLmNvbS9jcmVkZW50aWFsL25vbmNlIiwiY3JlZGVudGlhbF9yZXF1ZXN0X2VuY3J5cHRpb24iOnsiZW5jX3ZhbHVlc19zdXBwb3J0ZWQiOlsiQTEyOEdDTSIsIkEyNTZHQ00iXSwiemlwX3ZhbHVlc19zdXBwb3J0ZWQiOlsiREVGIl0sImVuY3J5cHRpb25fcmVxdWlyZWQiOmZhbHNlLCJqd2tzIjp7ImtleXMiOlt7Imt0eSI6IkVDIiwiY3J2IjoiUC0yNTYiLCJraWQiOiI3ZmY1YWRmZC04MTFjLTRjY2MtYWI4Yy1hYWYwYmJmZDMzZDIiLCJ4IjoiUS0yMWYxbm41WXNUU0d2aDB3clpGaWxVY0RNTkgxTkhDc3JETnJuZXA1SSIsInkiOiJGU1VfWFVScnFJZDBQVldja3FUSGVGUzlpdlNkcEU1WmduNzF1ajVjYjJ3IiwiYWxnIjoiRUNESC1FUyJ9XX19LCJjcmVkZW50aWFsX3Jlc3BvbnNlX2VuY3J5cHRpb24iOnsiZW5jX3ZhbHVlc19zdXBwb3J0ZWQiOlsiQTEyOEdDTSIsIkEyNTZHQ00iXSwiemlwX3ZhbHVlc19zdXBwb3J0ZWQiOlsiREVGIl0sImVuY3J5cHRpb25fcmVxdWlyZWQiOmZhbHNlLCJhbGdfdmFsdWVzX3N1cHBvcnRlZCI6WyJFQ0RILUVTIl19LCJjcmVkZW50aWFsX2NvbmZpZ3VyYXRpb25zX3N1cHBvcnRlZCI6eyJpZGVudGlmaWVyIjp7ImZvcm1hdCI6InZjK3NkLWp3dCIsInZjdCI6InZjdCIsImNyZWRlbnRpYWxfc2lnbmluZ19hbGdfdmFsdWVzX3N1cHBvcnRlZCI6WyJFUzI1NiJdLCJwcm9vZl90eXBlc19zdXBwb3J0ZWQiOnsiand0Ijp7InByb29mX3NpZ25pbmdfYWxnX3ZhbHVlc19zdXBwb3J0ZWQiOlsiRVMyNTYiXX19fX19.TfX4OvJBIlDJCIuQnEO0imx62QEwQ_McetarCl8J-eesbMk9ogtSWPZ3yWbB_2Pp6DGnSVtZU4valDch0yxYKg"

        /*
        {
          "credential_issuer": "https://example.com/issuer/issuerOID4VCI",
          [...]
        }
         */
        const val ISSUER_METADATA_JWT_OID4VCI =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiJ9.eyJjcmVkZW50aWFsX2VuZHBvaW50IjoiaHR0cHM6Ly9leGFtcGxlLmNvbS9jcmVkZW50aWFsL2VuZHBvaW50IiwiY3JlZGVudGlhbF9pc3N1ZXIiOiJodHRwczovL2V4YW1wbGUuY29tL2lzc3Vlci9pc3N1ZXJPSUQ0VkNJIiwibm9uY2VfZW5kcG9pbnQiOiJodHRwczovL2V4YW1wbGUuY29tL2NyZWRlbnRpYWwvbm9uY2UiLCJjcmVkZW50aWFsX3JlcXVlc3RfZW5jcnlwdGlvbiI6eyJlbmNfdmFsdWVzX3N1cHBvcnRlZCI6WyJBMTI4R0NNIiwiQTI1NkdDTSJdLCJ6aXBfdmFsdWVzX3N1cHBvcnRlZCI6WyJERUYiXSwiZW5jcnlwdGlvbl9yZXF1aXJlZCI6ZmFsc2UsImp3a3MiOnsia2V5cyI6W3sia3R5IjoiRUMiLCJjcnYiOiJQLTI1NiIsImtpZCI6IjdmZjVhZGZkLTgxMWMtNGNjYy1hYjhjLWFhZjBiYmZkMzNkMiIsIngiOiJRLTIxZjFubjVZc1RTR3ZoMHdyWkZpbFVjRE1OSDFOSENzckROcm5lcDVJIiwieSI6IkZTVV9YVVJycUlkMFBWV2NrcVRIZUZTOWl2U2RwRTVaZ243MXVqNWNiMnciLCJhbGciOiJFQ0RILUVTIn1dfX0sImNyZWRlbnRpYWxfcmVzcG9uc2VfZW5jcnlwdGlvbiI6eyJlbmNfdmFsdWVzX3N1cHBvcnRlZCI6WyJBMTI4R0NNIiwiQTI1NkdDTSJdLCJ6aXBfdmFsdWVzX3N1cHBvcnRlZCI6WyJERUYiXSwiZW5jcnlwdGlvbl9yZXF1aXJlZCI6ZmFsc2UsImFsZ192YWx1ZXNfc3VwcG9ydGVkIjpbIkVDREgtRVMiXX0sImNyZWRlbnRpYWxfY29uZmlndXJhdGlvbnNfc3VwcG9ydGVkIjp7ImlkZW50aWZpZXIiOnsiZm9ybWF0IjoidmMrc2Qtand0IiwidmN0IjoidmN0IiwiY3JlZGVudGlhbF9zaWduaW5nX2FsZ192YWx1ZXNfc3VwcG9ydGVkIjpbIkVTMjU2Il0sInByb29mX3R5cGVzX3N1cHBvcnRlZCI6eyJqd3QiOnsicHJvb2Zfc2lnbmluZ19hbGdfdmFsdWVzX3N1cHBvcnRlZCI6WyJFUzI1NiJdfX19fX0.6Pexm3Om1zMzDx76ejHbcNSP1ZDrLnjwM0X0TfP1OL8wjb-rydleWBVXXYisiYL7VesKQvuc8r8-ZPZXyGNnZw"

        /*
        {
          "credential_issuer": "https://example.com/issuer/issuerOIDC",
          [...]
        }
         */
        const val ISSUER_METADATA_JWT_OIDC =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJFUzI1NiJ9.eyJjcmVkZW50aWFsX2VuZHBvaW50IjoiaHR0cHM6Ly9leGFtcGxlLmNvbS9jcmVkZW50aWFsL2VuZHBvaW50IiwiY3JlZGVudGlhbF9pc3N1ZXIiOiJodHRwczovL2V4YW1wbGUuY29tL2lzc3Vlci9pc3N1ZXJPSURDIiwibm9uY2VfZW5kcG9pbnQiOiJodHRwczovL2V4YW1wbGUuY29tL2NyZWRlbnRpYWwvbm9uY2UiLCJjcmVkZW50aWFsX3JlcXVlc3RfZW5jcnlwdGlvbiI6eyJlbmNfdmFsdWVzX3N1cHBvcnRlZCI6WyJBMTI4R0NNIiwiQTI1NkdDTSJdLCJ6aXBfdmFsdWVzX3N1cHBvcnRlZCI6WyJERUYiXSwiZW5jcnlwdGlvbl9yZXF1aXJlZCI6ZmFsc2UsImp3a3MiOnsia2V5cyI6W3sia3R5IjoiRUMiLCJjcnYiOiJQLTI1NiIsImtpZCI6IjdmZjVhZGZkLTgxMWMtNGNjYy1hYjhjLWFhZjBiYmZkMzNkMiIsIngiOiJRLTIxZjFubjVZc1RTR3ZoMHdyWkZpbFVjRE1OSDFOSENzckROcm5lcDVJIiwieSI6IkZTVV9YVVJycUlkMFBWV2NrcVRIZUZTOWl2U2RwRTVaZ243MXVqNWNiMnciLCJhbGciOiJFQ0RILUVTIn1dfX0sImNyZWRlbnRpYWxfcmVzcG9uc2VfZW5jcnlwdGlvbiI6eyJlbmNfdmFsdWVzX3N1cHBvcnRlZCI6WyJBMTI4R0NNIiwiQTI1NkdDTSJdLCJ6aXBfdmFsdWVzX3N1cHBvcnRlZCI6WyJERUYiXSwiZW5jcnlwdGlvbl9yZXF1aXJlZCI6ZmFsc2UsImFsZ192YWx1ZXNfc3VwcG9ydGVkIjpbIkVDREgtRVMiXX0sImNyZWRlbnRpYWxfY29uZmlndXJhdGlvbnNfc3VwcG9ydGVkIjp7ImlkZW50aWZpZXIiOnsiZm9ybWF0IjoidmMrc2Qtand0IiwidmN0IjoidmN0IiwiY3JlZGVudGlhbF9zaWduaW5nX2FsZ192YWx1ZXNfc3VwcG9ydGVkIjpbIkVTMjU2Il0sInByb29mX3R5cGVzX3N1cHBvcnRlZCI6eyJqd3QiOnsicHJvb2Zfc2lnbmluZ19hbGdfdmFsdWVzX3N1cHBvcnRlZCI6WyJFUzI1NiJdfX19fX0.1uuuUmbHscsyr538BJ0n8p79JyRPWbH3r_dzjWCIHsRd9hvUXg7IHYQ1O_hxT5wE3udcNAkKyiO5z7u8iK7mDg"

        /*
        {
            "issuer": "https://example.com/issuer$ISSUER_OID4VCI",
            "token_endpoint": "https://example.com/token/endpoint"
        }
         */
        const val CREDENTIAL_ISSUER_CONFIG_OID4VCI_RESPONSE_JWT =
            "eyJhbGciOiJFUzI1NiJ9.eyJpc3N1ZXIiOiJodHRwczovL2V4YW1wbGUuY29tL2lzc3Vlci9pc3N1ZXJPSUQ0VkNJIiwidG9rZW5fZW5kcG9pbnQiOiJodHRwczovL2V4YW1wbGUuY29tL3Rva2VuL2VuZHBvaW50In0.EKeBKSVuiTlTRbLU-cd2ZJw6GMHxoTDEhVuxPCz4Aikj4dMjh0Kl7FHINpnNN10kS5LFqjCjB8HEJ-HYvPD3vA"

        /*
        {
            "issuer": "https://example.com/issuer$ISSUER_OIDC",
            "token_endpoint": "https://example.com/token/endpoint"
        }
         */
        const val CREDENTIAL_ISSUER_CONFIG_OIDC_RESPONSE_JWT =
            "eyJhbGciOiJFUzI1NiJ9.eyJpc3N1ZXIiOiJodHRwczovL2V4YW1wbGUuY29tL2lzc3Vlci9pc3N1ZXJPSURDIiwidG9rZW5fZW5kcG9pbnQiOiJodHRwczovL2V4YW1wbGUuY29tL3Rva2VuL2VuZHBvaW50In0.nc0THTowIYSYBKYZJPScPO4_pJp1RNQ4luhnd8lyTNUI-6eNYI1dsuLYd_H_t6LsfkaFWf2vlxzexBlbzyTWBA"

        const val BASE_URL = "https://example.com"
        const val ISSUER_PATH = "/issuer"
        const val CREDENTIAL_PATH = "/credential"
        const val VERIFIABLE_PATH = "/verifiable"
        const val DEFERRED_PATH = "/deferred"
        const val ERROR_PATH = "/error"
        val verifiableCredentialUrl = createUrl(VERIFIABLE_PATH)
        val deferredCredentialUrl = createUrl(DEFERRED_PATH)
        val errorUrl = createUrl(ERROR_PATH)
        val tokenResponse = TokenResponse(
            accessToken = "accessToken",
            tokenType = TokenType.BEARER,
        )

        val verifiableCredentialResponseJson = """
            {
                "credentials": [
                    {
                        "credential": "credentialJwt"
                    }
                ]
            }
        """.trimIndent()

        val emptyCredentialResponseJson = """
            {
                "credentials": []
            }
        """.trimIndent()

        val deferredCredentialResponseJson = """
            {
                "transaction_id": "trxId",
                "interval": 100
            }
        """.trimIndent()

        val invalidCredentialResponseJson = """
            {
                "content": "invalid"
            }
        """.trimIndent()

        fun createUrl(endpoint: String): URL = URI.create("$BASE_URL$CREDENTIAL_PATH$endpoint").toURL()
    }
}

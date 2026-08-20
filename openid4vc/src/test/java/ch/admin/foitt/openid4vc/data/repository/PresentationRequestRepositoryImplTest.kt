package ch.admin.foitt.openid4vc.data.repository

import ch.admin.foitt.openid4vc.data.PresentationRequestRepositoryImpl
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationResponseConfig
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationResponseParam
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationResponseType
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestError
import ch.admin.foitt.openid4vc.util.SafeJsonTestInstance
import ch.admin.foitt.openid4vc.util.assertErrorType
import ch.admin.foitt.openid4vc.util.assertOk
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull
import java.io.IOException
import java.net.URI

class PresentationRequestRepositoryImplTest {

    private val json = SafeJsonTestInstance.json

    private val safeJson = SafeJsonTestInstance.safeJson

    private lateinit var handler: (suspend MockRequestHandleScope.(HttpRequestData) -> HttpResponseData)

    private val mockHttpClient by lazy {
        HttpClient(MockEngine) {
            // matches the config of the production client, so that 4xx/5xx responses throw
            expectSuccess = true
            install(ContentNegotiation) {
                json(json)
            }
            engine {
                addHandler { request ->
                    handler(request)
                }
            }
        }
    }

    private lateinit var repo: PresentationRequestRepositoryImpl

    @BeforeEach
    fun setUp() {
        repo = PresentationRequestRepositoryImpl(
            httpClient = mockHttpClient,
            safeJson = safeJson,
        )
    }

    @Test
    fun `Submitting a presentation which returns a 2xx returns a success`() = runTest {
        handler = { respond(content = "", status = HttpStatusCode.OK) }

        val result = repo.submitPresentation(URL, authorizationResponseConfig).assertOk()

        assertNull(result.redirectUri)
    }

    @Test
    fun `Submitting a presentation which returns a 2xx with a redirect uri returns a success`() = runTest {
        handler = {
            respond(
                content = """{"redirect_uri": "https://example.com"}""",
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }

        val result = repo.submitPresentation(URL, authorizationResponseConfig).assertOk()

        assertEquals("https://example.com", result.redirectUri)
    }

    @Test
    fun `Submitting a presentation which returns a 400 with an unparseable body returns a success`() = runTest {
        handler = { respondBadRequest(content = "<html>definitely not an HttpErrorBody</html>") }

        repo.submitPresentation(URL, authorizationResponseConfig).assertOk()
    }

    @Test
    fun `Submitting a presentation which returns a 400 with a verification_process_closed error returns a success`() =
        runTest {
            handler = { respondBadRequest(content = """{"error": "verification_process_closed"}""") }

            repo.submitPresentation(URL, authorizationResponseConfig).assertOk()
        }

    @Test
    fun `Submitting a presentation which returns a 400 with an invalid_credential error returns a success`() =
        runTest {
            handler = { respondBadRequest(content = """{"error": "invalid_credential"}""") }

            repo.submitPresentation(URL, authorizationResponseConfig).assertOk()
        }

    @Test
    fun `Submitting a presentation which returns a 400 with an unknown error returns a success`() = runTest {
        handler = { respondBadRequest(content = """{"error": "some_error", "error_description": "some description"}""") }

        repo.submitPresentation(URL, authorizationResponseConfig).assertOk()
    }

    @Test
    fun `Submitting a presentation which returns a 400 with only an error_description returns a success`() = runTest {
        handler = {
            respondBadRequest(
                content = """{"error_description":"OAuth2.0 State mismatch. Expected to receive the state as in Request Object"}""",
            )
        }

        repo.submitPresentation(URL, authorizationResponseConfig).assertOk()
    }

    @Test
    fun `Submitting a presentation which returns a non-400 client error returns a success`() = runTest {
        handler = { respond(content = "", status = HttpStatusCode.Forbidden) }

        repo.submitPresentation(URL, authorizationResponseConfig).assertOk()
    }

    @Test
    fun `Submitting a presentation which returns a 404 returns a success`() = runTest {
        handler = { respond(content = "", status = HttpStatusCode.NotFound) }

        repo.submitPresentation(URL, authorizationResponseConfig).assertOk()
    }

    @Test
    fun `Submitting a presentation which returns a server error returns a success`() = runTest {
        handler = { respond(content = "internal error", status = HttpStatusCode.InternalServerError) }

        repo.submitPresentation(URL, authorizationResponseConfig).assertOk()
    }

    @Test
    fun `Submitting a presentation which returns a 503 returns a success`() = runTest {
        handler = { respond(content = "", status = HttpStatusCode.ServiceUnavailable) }

        repo.submitPresentation(URL, authorizationResponseConfig).assertOk()
    }

    @Test
    fun `Submitting a presentation which fails with an IOException returns a network error`() = runTest {
        // The data never left the device, so the user must see the error
        handler = { throw IOException("no network") }

        repo.submitPresentation(URL, authorizationResponseConfig)
            .assertErrorType(PresentationRequestError.NetworkError::class)
    }

    private fun MockRequestHandleScope.respondBadRequest(content: String) = respond(
        content = content,
        status = HttpStatusCode.BadRequest,
        headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
    )

    private companion object {
        val URL: java.net.URL = URI.create("https://example.com/oid4vp/response").toURL()

        val authorizationResponseConfig = AuthorizationResponseConfig(
            type = AuthorizationResponseType.DCQL,
            params = mapOf(AuthorizationResponseParam.VP_TOKEN to "someVpToken"),
        )
    }
}

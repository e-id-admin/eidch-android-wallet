package ch.admin.foitt.wallet.util

import ch.admin.foitt.wallet.platform.utils.getQueryParameter
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.net.URI

class URIExtTest {

    @Test
    fun `Getting the parameter value of a simple URI returns the value`() = runTest {
        val result = simpleUri.getQueryParameter("param1").assertOk()
        assertEquals(simpleUriParamValue, result)
    }

    @Test
    fun `Getting the parameter value of a complex URI returns the value`() = runTest {
        val result = complexUri.getQueryParameter("param2").assertOk()
        assertEquals(complexUriParamValue, result)
    }

    @Test
    fun `Trying to get a non-existing parameter returns null`() = runTest {
        val result = simpleUri.getQueryParameter("otherParam").assertOkNullable()
        assertEquals(null, result)
    }

    @Test
    fun `Getting the parameter of a deeplink style uri returns a success`() = runTest {
        val result = deeplinkUri.getQueryParameter("client_id").assertOk()
        assertEquals(deeplinkUriParamValue, result)
    }

    @Test
    fun `Getting the parameter of a uri that contains multiple times the same parameter returns the first one`() = runTest {
        val result = multipleParamUri.getQueryParameter("param1").assertOk()
        assertEquals(multipleParamUriParamValue, result)
    }

    @Test
    fun `URI with no query parameters returns null for any key`() = runTest {
        val uri = URI("https://example.com")
        val result = uri.getQueryParameter("anyKey").assertOkNullable()
        assertEquals(null, result)
    }

    @Test
    fun `URI with empty query string returns null`() = runTest {
        val uri = URI("https://example.com?")
        val result = uri.getQueryParameter("key").assertOkNullable()
        assertEquals(null, result)
    }

    @Test
    fun `Parameter with empty value returns empty string`() = runTest {
        val uri = URI("https://example.com?key=")
        val result = uri.getQueryParameter("key").assertOk()
        assertEquals("", result)
    }

    @Test
    fun `Parameter without equals sign returns empty string`() = runTest {
        val uri = URI("https://example.com?key")
        val result = uri.getQueryParameter("key").assertOk()
        assertEquals("", result)
    }

    @Test
    fun `Parameter value containing ampersand is decoded correctly`() = runTest {
        val uri = URI("https://example.com?param=value%26more")
        val result = uri.getQueryParameter("param").assertOk()
        assertEquals("value&more", result)
    }

    @Test
    fun `Parameter value containing equals sign is decoded correctly`() = runTest {
        val uri = URI("https://example.com?param=value%3Dextra")
        val result = uri.getQueryParameter("param").assertOk()
        assertEquals("value=extra", result)
    }

    @Test
    fun `Parameter value with plus sign is decoded as space`() = runTest {
        val uri = URI("https://example.com?param=hello+world")
        val result = uri.getQueryParameter("param").assertOk()
        assertEquals("hello world", result)
    }

    @Test
    fun `Parameter value with percent-encoded space is decoded correctly`() = runTest {
        val uri = URI("https://example.com?param=hello%20world")
        val result = uri.getQueryParameter("param").assertOk()
        assertEquals("hello world", result)
    }

    @Test
    fun `Unicode characters in parameter value are decoded correctly`() = runTest {
        val uri = URI("https://example.com?param=M%C3%BCller")
        val result = uri.getQueryParameter("param").assertOk()
        assertEquals("Müller", result)
    }

    @Test
    fun `Last parameter without trailing ampersand works`() = runTest {
        val uri = URI("https://example.com?param1=value1&param2=value2")
        val result = uri.getQueryParameter("param2").assertOk()
        assertEquals("value2", result)
    }

    @Test
    fun `Empty parameter key returns null`() = runTest {
        val uri = URI("https://example.com?=value")
        val result = uri.getQueryParameter("").assertOkNullable()
        assertEquals(null, result)
    }

    @Test
    fun `Multiple parameters with same name returns first occurrence`() = runTest {
        val uri = URI("https://example.com?param=first&param=second&param=third")
        val result = uri.getQueryParameter("param").assertOk()
        assertEquals("first", result)
    }

    @Test
    fun `Parameter value with multiple encoded characters`() = runTest {
        val uri = URI("https://example.com?param=a%20b%26c%3Dd%2Be")
        val result = uri.getQueryParameter("param").assertOk()
        assertEquals("a b&c=d+e", result)
    }

    @Test
    fun `URI with fragment does not affect query parameter parsing`() = runTest {
        val uri = URI("https://example.com?param=value#fragment")
        val result = uri.getQueryParameter("param").assertOk()
        assertEquals("value", result)
    }

    @Test
    fun `URI with path but no query returns null`() = runTest {
        val uri = URI("https://example.com/path/to/resource")
        val result = uri.getQueryParameter("param").assertOkNullable()
        assertEquals(null, result)
    }

    @Test
    fun `Percent-encoded key is decoded when matching`() = runTest {
        val uri = URI("https://example.com?param%20name=value")
        val result = uri.getQueryParameter("param name").assertOk()
        assertEquals("value", result)
    }

    @Test
    fun `Query with only separators returns null`() = runTest {
        val uri = URI("https://example.com?&&&")
        val result = uri.getQueryParameter("key").assertOkNullable()
        assertEquals(null, result)
    }

    @Test
    fun `Parameter key matching is exact, not prefix`() = runTest {
        val uri = URI("https://example.com?paramExtra=value1&param=value2")
        val result = uri.getQueryParameter("param").assertOk()
        assertEquals("value2", result)
    }

    private val simpleUri = URI("https%3A%2F%2Fexample.com?param1=param1Value")
    private val simpleUriParamValue = "param1Value"

    private val complexUri = URI("https%3A%2F%2Fexample.com?param1=param1Value&param2=value%26value")
    private val complexUriParamValue = "value&value"

    private val multipleParamUri = URI("https%3A%2F%2Fexample.com?param1=param1Value&param1=param1Value2")
    private val multipleParamUriParamValue = "param1Value"

    private val deeplinkUri = URI(
        "swiyu-verify://?client_id=did%3Atdw%3Aexample.com&request_uri=https%3A%2F%2Ftfp.example.com%2Frequest.jwt%2FGkurKxf5T0Y-mnPFCHqWOMiZi4VS138cQO_V7PZHAdM"
    )
    private val deeplinkUriParamValue = "did:tdw:example.com"
}

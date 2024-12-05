package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.JsonPresentationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.JwtPresentationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestError
import ch.admin.foitt.openid4vc.domain.repository.PresentationRequestRepository
import ch.admin.foitt.openid4vc.domain.usecase.FetchPresentationRequest
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockPresentationRequest
import ch.admin.foitt.openid4vc.util.assertErrorType
import ch.admin.foitt.openid4vc.util.assertOk
import ch.admin.foitt.openid4vc.utils.SafeJson
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import java.net.URL

class FetchPresentationRequestImplTest {

    @OptIn(ExperimentalSerializationApi::class)
    private val testJson = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
    }

    private val testSafeJson: SafeJson = SafeJson(
        json = testJson,
    )

    private val testUrl = URL("https://example.com")

    @MockK
    private lateinit var mockPresentationRequestRepository: PresentationRequestRepository

    private lateinit var useCase: FetchPresentationRequest

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        useCase = FetchPresentationRequestImpl(
            safeJson = testSafeJson,
            presentationRequestRepository = mockPresentationRequestRepository,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Fetching a valid presentation request json succeeds`(): Unit = runTest {
        coEvery {
            mockPresentationRequestRepository.fetchPresentationRequest(any())
        } returns Ok(MockPresentationRequest.validJson)

        val result = useCase(testUrl).assertOk()

        assertTrue(result is JsonPresentationRequest)
        assertEquals("zW0qUvtH3AczW8MTTSebAFrSbQsqSjc5", result.nonce)

        coVerifyOrder {
            mockPresentationRequestRepository.fetchPresentationRequest(any())
        }
    }

    @Test
    fun `Fetching a valid presentation request jwt succeeds`(): Unit = runTest {
        coEvery {
            mockPresentationRequestRepository.fetchPresentationRequest(any())
        } returns Ok(MockPresentationRequest.validJwt)

        val result = useCase(testUrl).assertOk()

        assertTrue(result is JwtPresentationRequest)
        assertEquals("I02FibLF4k5EsfDO2jgjDooP4A/ZukQ3", result.nonce)

        coVerifyOrder {
            mockPresentationRequestRepository.fetchPresentationRequest(any())
        }
    }

    @Test
    fun `A failed presentationRequestRepository call return an error`(): Unit = runTest {
        coEvery {
            mockPresentationRequestRepository.fetchPresentationRequest(any())
        } returns Err(PresentationRequestError.NetworkError)

        val result = useCase.invoke(testUrl)
        result.assertErrorType(PresentationRequestError.NetworkError::class)
    }

    @ParameterizedTest
    @ValueSource(
        strings = [
            "aaaa",
            MockPresentationRequest.invalidJwt,
        ]
    )
    fun `An invalid jwt presentation request return an error`(jwt: String): Unit = runTest {
        coEvery { mockPresentationRequestRepository.fetchPresentationRequest(any()) } returns Ok(jwt)

        val result = useCase.invoke(testUrl)
        val error = result.assertErrorType(PresentationRequestError.Unexpected::class)
        assert(error.throwable is SerializationException)
    }

    @Test
    fun `An invalid json presentation request return an error`(): Unit = runTest {
        coEvery {
            mockPresentationRequestRepository.fetchPresentationRequest(any())
        } returns Ok(MockPresentationRequest.invalidJson)
        val result = useCase.invoke(testUrl)
        val error = result.assertErrorType(PresentationRequestError.Unexpected::class)
        assert(error.throwable is SerializationException)
    }
}

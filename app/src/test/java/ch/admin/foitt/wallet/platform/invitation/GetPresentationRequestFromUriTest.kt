package ch.admin.foitt.wallet.platform.invitation

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestContainer
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestError
import ch.admin.foitt.openid4vc.domain.usecase.FetchPresentationRequest
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.GetPresentationRequestFromUri
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.implementation.GetPresentationRequestFromUriImpl
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.get
import com.github.michaelbull.result.getError
import io.mockk.MockKAnnotations
import io.mockk.Ordering
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URI

class GetPresentationRequestFromUriTest {

    @MockK
    private lateinit var mockFetchPresentationRequest: FetchPresentationRequest

    @MockK
    private lateinit var mockPresentationRequestContainer: PresentationRequestContainer

    private val validPresentationUrl =
        URI("https://example.org/get_request_object/88cf0d95-54c9-465f-9b97-0ba782314700")

    private lateinit var getPresentationRequestFromUri: GetPresentationRequestFromUri

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        getPresentationRequestFromUri = GetPresentationRequestFromUriImpl(
            fetchPresentationRequest = mockFetchPresentationRequest,
        )

        coEvery { mockFetchPresentationRequest.invoke(url = any()) } returns Ok(mockPresentationRequestContainer)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `A valid presentation request should return a success`() = runTest {
        val useCaseResult = getPresentationRequestFromUri(
            uri = validPresentationUrl,
        )

        coVerify(ordering = Ordering.ORDERED) {
            mockFetchPresentationRequest.invoke(url = any())
        }

        assertTrue(useCaseResult.get() is PresentationRequestContainer)
    }

    @Test
    fun `A repository network error should return an error`() = runTest {
        coEvery { mockFetchPresentationRequest.invoke(url = any()) } returns Err(PresentationRequestError.NetworkError)

        val useCaseResult = getPresentationRequestFromUri(
            uri = validPresentationUrl,
        )

        coVerify(ordering = Ordering.ORDERED) {
            mockFetchPresentationRequest.invoke(url = any())
        }

        assertTrue(useCaseResult.getError() is InvitationError.NetworkError)
    }

    @Test
    fun `A presentation unexpected error should return an InvalidInvitation error`() = runTest {
        coEvery {
            mockFetchPresentationRequest.invoke(url = any())
        } returns Err(PresentationRequestError.Unexpected(Exception()))

        val useCaseResult = getPresentationRequestFromUri(
            uri = validPresentationUrl,
        )

        coVerify(ordering = Ordering.ORDERED) {
            mockFetchPresentationRequest.invoke(url = any())
        }

        assertTrue(useCaseResult.getError() is InvitationError.InvalidPresentationRequest)
    }

    @Test
    fun `An invalid URL should return an error`() = runTest {
        val useCaseResult = getPresentationRequestFromUri(
            uri = URI("invalid://invalid.com"),
        )

        coVerify(exactly = 0) {
            mockFetchPresentationRequest.invoke(url = any())
        }

        assertTrue(useCaseResult.getError() is InvitationError.InvalidUri)
    }
}

package ch.admin.foitt.wallet.platform.invitation

import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CompatibleCredential
import ch.admin.foitt.wallet.platform.credentialPresentation.mock.MockPresentationRequest
import ch.admin.foitt.wallet.platform.invitation.domain.model.ProcessInvitationResult
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.HandleInvitationProcessingSuccess
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.implementation.HandleInvitationProcessingSuccessImpl
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.walletcomposedestinations.destinations.CredentialOfferScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.PresentationCredentialListScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.PresentationRequestScreenDestination
import com.ramcosta.composedestinations.spec.Direction
import io.mockk.MockKAnnotations
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.runs
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class HandleInvitationProcessingSuccessTest {

    @MockK
    private lateinit var mockNavigationManager: NavigationManager

    private lateinit var handleInvitationProcessingSuccess: HandleInvitationProcessingSuccess

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery { mockNavigationManager.navigateToAndClearCurrent(any()) } just runs

        handleInvitationProcessingSuccess = HandleInvitationProcessingSuccessImpl(
            navManager = mockNavigationManager,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `execution navigates to the defined screen`() = runTest {
        definedSuccessDestinations.forEach { (successResult, destination) ->
            handleInvitationProcessingSuccess(successResult).navigate()

            coVerify(exactly = 1) {
                mockNavigationManager.navigateToAndClearCurrent(destination)
                mockNavigationManager.navigateToAndClearCurrent(destination)
            }
            clearMocks(mockNavigationManager, answers = false)
        }
    }

    companion object {
        private val mockPresentationRequest = MockPresentationRequest.presentationRequest
        private val mockCredentialOfferResult = ProcessInvitationResult.CredentialOffer(0L)

        private val mockCompatibleCredential = CompatibleCredential(
            credentialId = mockCredentialOfferResult.credentialId,
            requestedFields = listOf(),
        )

        private val mockPresentationRequestResult = ProcessInvitationResult.PresentationRequest(
            mockCompatibleCredential,
            mockPresentationRequest,
            shouldCheckTrustStatement = true,
        )

        private val mockPresentationRequestListResult = ProcessInvitationResult.PresentationRequestCredentialList(
            listOf(mockCompatibleCredential),
            mockPresentationRequest,
            shouldCheckTrustStatement = true,
        )

        private val definedSuccessDestinations: Map<ProcessInvitationResult, Direction> = mapOf(
            mockCredentialOfferResult to CredentialOfferScreenDestination(mockCredentialOfferResult.credentialId),
            mockPresentationRequestResult to PresentationRequestScreenDestination(
                mockPresentationRequestResult.credential,
                mockPresentationRequestResult.request,
                shouldFetchTrustStatement = true,
            ),
            mockPresentationRequestListResult to PresentationCredentialListScreenDestination(
                mockPresentationRequestListResult.credentials.toTypedArray(),
                mockPresentationRequestListResult.request,
                shouldFetchTrustStatement = true,
            ),
        )
    }
}

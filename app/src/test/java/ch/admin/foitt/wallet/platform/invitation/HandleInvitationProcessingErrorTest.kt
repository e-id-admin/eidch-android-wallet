package ch.admin.foitt.wallet.platform.invitation

import android.annotation.SuppressLint
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestErrorBody
import ch.admin.foitt.openid4vc.domain.usecase.DeclinePresentation
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError
import ch.admin.foitt.wallet.platform.invitation.domain.model.ProcessInvitationError
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.HandleInvitationProcessingError
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.implementation.HandleInvitationProcessingErrorImpl
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.walletcomposedestinations.destinations.InvalidCredentialErrorScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.InvitationFailureScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.NoInternetConnectionScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.PresentationEmptyWalletScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.PresentationNoMatchScreenDestination
import com.github.michaelbull.result.Ok
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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.FieldSource

class HandleInvitationProcessingErrorTest {
    @MockK
    private lateinit var mockNavigationManager: NavigationManager

    @MockK
    private lateinit var mockDeclinePresentation: DeclinePresentation

    private lateinit var handleInvitationProcessingError: HandleInvitationProcessingError

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery { mockNavigationManager.navigateToAndClearCurrent(any()) } just runs
        coEvery { mockDeclinePresentation(any(), any()) } returns Ok(Unit)

        handleInvitationProcessingError = HandleInvitationProcessingErrorImpl(
            navManager = mockNavigationManager,
            declinePresentation = mockDeclinePresentation,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `execution navigates to the defined error screen`() = runTest {
        definedErrorDestinations.forEach { (error, destination) ->
            handleInvitationProcessingError(
                error,
                SOME_DEEP_LINK,
            ).navigate()

            coVerify(exactly = 1) {
                mockNavigationManager.navigateToAndClearCurrent(direction = any())
                mockNavigationManager.navigateToAndClearCurrent(
                    direction = destination,
                )
            }
            clearMocks(mockNavigationManager, answers = false)
        }
    }

    @SuppressLint("CheckResult")
    @ParameterizedTest
    @FieldSource("processInvitationErrors")
    fun `execution with presentation errors declines the presentation request`(error: ProcessInvitationError) = runTest {
        handleInvitationProcessingError(error, SOME_DEEP_LINK)

        when (error) {
            is InvitationError.InvalidPresentation -> {
                coVerify(exactly = 1) {
                    mockDeclinePresentation(RESPONSE_URI, PresentationRequestErrorBody.ErrorType.INVALID_REQUEST)
                }
            }
            else -> {
                coVerify(exactly = 0) {
                    mockDeclinePresentation(any(), any())
                }
            }
        }
        clearMocks(mockDeclinePresentation, answers = false)
    }

    companion object {
        private const val SOME_DEEP_LINK = "openid-credential-offer://credential_offer=..."
        private const val RESPONSE_URI = "response uri"

        private val definedErrorDestinations: Map<ProcessInvitationError, Direction> = mapOf(
            InvitationError.EmptyWallet to PresentationEmptyWalletScreenDestination,
            InvitationError.InvalidCredentialInvitation to InvalidCredentialErrorScreenDestination,
            InvitationError.InvalidInput to InvitationFailureScreenDestination,
            InvitationError.NetworkError to NoInternetConnectionScreenDestination(SOME_DEEP_LINK),
            InvitationError.NoCompatibleCredential to PresentationNoMatchScreenDestination,
            InvitationError.Unexpected to InvitationFailureScreenDestination,
            InvitationError.InvalidPresentation(RESPONSE_URI) to InvitationFailureScreenDestination,
        )

        @Suppress("unused") // -> used in parametrized test
        private val processInvitationErrors = listOf(
            InvitationError.EmptyWallet,
            InvitationError.InvalidCredentialInvitation,
            InvitationError.InvalidInput,
            InvitationError.NetworkError,
            InvitationError.NoCompatibleCredential,
            InvitationError.Unexpected,
            InvitationError.InvalidPresentation(RESPONSE_URI),
        )
    }
}

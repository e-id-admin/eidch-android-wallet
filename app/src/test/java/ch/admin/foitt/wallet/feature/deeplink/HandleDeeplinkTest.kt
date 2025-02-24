package ch.admin.foitt.wallet.feature.deeplink

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationDefinition
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequest
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CompatibleCredential
import ch.admin.foitt.wallet.platform.deeplink.domain.repository.DeepLinkIntentRepository
import ch.admin.foitt.wallet.platform.deeplink.domain.usecase.HandleDeeplink
import ch.admin.foitt.wallet.platform.deeplink.domain.usecase.implementation.HandleDeeplinkImpl
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationErrorScreenState
import ch.admin.foitt.wallet.platform.invitation.domain.model.ProcessInvitationError
import ch.admin.foitt.wallet.platform.invitation.domain.model.ProcessInvitationResult
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.ProcessInvitation
import ch.admin.foitt.wallet.platform.navArgs.domain.model.CredentialOfferNavArg
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.scaffold.extension.navigateUpOrToRoot
import ch.admin.foitt.walletcomposedestinations.destinations.CredentialOfferScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.EIdIntroScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.HomeScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.InvitationFailureScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.OnboardingSuccessScreenDestination
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.ramcosta.composedestinations.spec.Direction
import io.mockk.MockKAnnotations
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.runs
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class HandleDeeplinkTest {
    @MockK
    private lateinit var mockNavigationManager: NavigationManager

    @MockK
    private lateinit var mockDeepLinkIntentRepository: DeepLinkIntentRepository

    @MockK
    private lateinit var mockEnvironmentSetupRepository: EnvironmentSetupRepository

    @MockK
    private lateinit var mockProcessInvitation: ProcessInvitation

    private lateinit var handleDeeplinkUseCase: HandleDeeplink

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery { mockProcessInvitation(invitationUri = any()) } returns Ok(mockCredentialOfferResult)
        coEvery { mockDeepLinkIntentRepository.get() } returns SOME_DEEP_LINK
        coEvery { mockEnvironmentSetupRepository.eIdRequestEnabled } returns false
        coEvery { mockNavigationManager.navigateToAndPopUpTo(any(), any()) } just runs
        coEvery { mockNavigationManager.navigateToAndClearCurrent(any()) } just runs
        coEvery { mockNavigationManager.navigateUpOrToRoot() } just runs
        coEvery { mockDeepLinkIntentRepository.reset() } just runs

        handleDeeplinkUseCase = HandleDeeplinkImpl(
            navManager = mockNavigationManager,
            deepLinkIntentRepository = mockDeepLinkIntentRepository,
            environmentSetupRepository = mockEnvironmentSetupRepository,
            processInvitation = mockProcessInvitation,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Coming from onboarding, on no intent, with eId feature disabled, navigates to home screen and pop onboarding`() = runTest {
        coEvery { mockDeepLinkIntentRepository.get() } returns null

        handleDeeplinkUseCase(true).navigate()

        coVerifyOrder {
            mockDeepLinkIntentRepository.get()
            mockNavigationManager.navigateToAndPopUpTo(
                direction = HomeScreenDestination,
                route = OnboardingSuccessScreenDestination.route,
            )
        }
    }

    @Test
    fun `Coming from onboarding, on no intent, with eId feature enabled, navigates to eId request screen and pop onboarding`() = runTest {
        coEvery { mockDeepLinkIntentRepository.get() } returns null
        coEvery { mockEnvironmentSetupRepository.eIdRequestEnabled } returns true

        handleDeeplinkUseCase(true).navigate()

        coVerifyOrder {
            mockDeepLinkIntentRepository.get()
            mockNavigationManager.navigateToAndPopUpTo(
                direction = EIdIntroScreenDestination,
                route = OnboardingSuccessScreenDestination.route,
            )
        }
    }

    @Test
    fun `Not being in onboarding, on no intent, navigates up or to root`() = runTest {
        coEvery { mockDeepLinkIntentRepository.get() } returns null

        handleDeeplinkUseCase(false).navigate()

        coVerifyOrder {
            mockDeepLinkIntentRepository.get()
            mockNavigationManager.navigateUpOrToRoot()
        }
    }

    @Test
    fun `On deeplink handling, reset the repository to null`() = runTest {
        handleDeeplinkUseCase(false).navigate()

        coVerifyOrder {
            mockDeepLinkIntentRepository.get()
            mockDeepLinkIntentRepository.reset()
        }
    }

    @Test
    fun `On deeplink handling, in onboarding, pop the onboarding stack in all success cases`() = runTest {
        mockSuccesses.forEach { success ->
            coEvery { mockProcessInvitation(SOME_DEEP_LINK) } returns Ok(success)
            handleDeeplinkUseCase(true).navigate()

            coVerify(exactly = 1) {
                mockNavigationManager.navigateToAndPopUpTo(any(), route = OnboardingSuccessScreenDestination.route)
            }
            clearMocks(mockNavigationManager, answers = false)
        }
    }

    @Test
    fun `On deeplink handling, not in onboarding, navigate and pop current screen in all success cases`() = runTest {
        mockSuccesses.forEach { success ->
            coEvery { mockProcessInvitation(SOME_DEEP_LINK) } returns Ok(success)
            handleDeeplinkUseCase(false).navigate()

            coVerify(exactly = 1) {
                mockNavigationManager.navigateToAndClearCurrent(any())
            }
            clearMocks(mockNavigationManager, answers = false)
        }
    }

    @Test
    fun `On deeplink handling, in onboarding, pop the onboarding stack in all failure cases`() = runTest {
        mockFailures.forEach { failure ->
            coEvery { mockProcessInvitation(SOME_DEEP_LINK) } returns Err(failure)
            handleDeeplinkUseCase(true).navigate()

            coVerify(exactly = 1) {
                mockNavigationManager.navigateToAndPopUpTo(any(), route = OnboardingSuccessScreenDestination.route)
            }
            clearMocks(mockNavigationManager, answers = false)
        }
    }

    @Test
    fun `On deeplink handling, not in onboarding, navigate and pop current screen in all failure cases`() = runTest {
        mockFailures.forEach { failure ->
            coEvery { mockProcessInvitation(SOME_DEEP_LINK) } returns Err(failure)
            handleDeeplinkUseCase(false).navigate()

            coVerify(exactly = 1) {
                mockNavigationManager.navigateToAndClearCurrent(any())
            }
            clearMocks(mockNavigationManager, answers = false)
        }
    }

    @Test
    fun `On credential offer, navigates to credential offer screen`() = runTest {
        coEvery { mockProcessInvitation(SOME_DEEP_LINK) } returns Ok(mockCredentialOfferResult)
        handleDeeplinkUseCase(false).navigate()

        coVerify(exactly = 1) {
            mockNavigationManager.navigateToAndClearCurrent(any())
            mockNavigationManager.navigateToAndClearCurrent(
                direction = CredentialOfferScreenDestination(
                    CredentialOfferNavArg(
                        mockCredentialOfferResult.credentialId
                    )
                ),
            )
        }
    }

    @Test
    fun `On presentation request, navigate to an error`() = runTest {
        coEvery { mockProcessInvitation(SOME_DEEP_LINK) } returns Ok(mockPresentationRequestResult)
        handleDeeplinkUseCase(false).navigate()

        coVerify(exactly = 1) {
            mockNavigationManager.navigateToAndClearCurrent(any())
            mockNavigationManager.navigateToAndClearCurrent(
                direction = InvitationFailureScreenDestination(InvitationErrorScreenState.UNEXPECTED),
            )
        }
    }

    @Test
    fun `On presentation request with multiple credentials, navigate to an error`() = runTest {
        coEvery { mockProcessInvitation(SOME_DEEP_LINK) } returns Ok(mockPresentationRequestListResult)
        handleDeeplinkUseCase(false).navigate()

        coVerify(exactly = 1) {
            mockNavigationManager.navigateToAndClearCurrent(any())
            mockNavigationManager.navigateToAndClearCurrent(
                direction = InvitationFailureScreenDestination(InvitationErrorScreenState.UNEXPECTED),
            )
        }
    }

    @Test
    fun `On deeplink processing failure, navigate to the defined error screen`() = runTest {
        val definedErrorDestinations: Map<ProcessInvitationError, Direction> = mapOf(
            InvitationError.EmptyWallet to InvitationFailureScreenDestination(InvitationErrorScreenState.UNEXPECTED),
            InvitationError.InvalidCredentialOffer to InvitationFailureScreenDestination(InvitationErrorScreenState.INVALID_CREDENTIAL),
            InvitationError.InvalidInput to InvitationFailureScreenDestination(InvitationErrorScreenState.INVALID_CREDENTIAL),
            InvitationError.NoCompatibleCredential to InvitationFailureScreenDestination(InvitationErrorScreenState.UNEXPECTED),
            InvitationError.Unexpected to InvitationFailureScreenDestination(InvitationErrorScreenState.UNEXPECTED),
            InvitationError.NetworkError to InvitationFailureScreenDestination(InvitationErrorScreenState.NETWORK_ERROR),
            InvitationError.UnknownIssuer to InvitationFailureScreenDestination(InvitationErrorScreenState.UNKNOWN_ISSUER),
        )

        definedErrorDestinations.forEach { (error, destination) ->
            coEvery { mockProcessInvitation(SOME_DEEP_LINK) } returns Err(error)
            handleDeeplinkUseCase(false).navigate()

            coVerify(exactly = 1) {
                mockNavigationManager.navigateToAndClearCurrent(any())
                mockNavigationManager.navigateToAndClearCurrent(
                    direction = destination,
                )
            }
            clearMocks(mockNavigationManager, answers = false)
        }
    }

    companion object {
        private const val SOME_DEEP_LINK = "openid-credential-offer://credential_offer=..."
        private val mockCredentialOfferResult = ProcessInvitationResult.CredentialOffer(0L)

        private val mockPresentationRequest = PresentationRequest(
            nonce = "iusto",
            presentationDefinition = PresentationDefinition(
                id = "diam",
                inputDescriptors = listOf(),
                purpose = "purpose",
                name = "name",
            ),
            responseUri = "tincidunt",
            responseMode = "suscipit",
            clientId = "clientId",
            clientIdScheme = "clientIdScheme",
            responseType = "responseType",
            clientMetaData = null
        )

        private val mockCompatibleCredential = CompatibleCredential(
            credentialId = mockCredentialOfferResult.credentialId,
            requestedFields = listOf(),
        )

        private val mockPresentationRequestResult = ProcessInvitationResult.PresentationRequest(
            mockCompatibleCredential,
            mockPresentationRequest,
            shouldCheckTrustStatement = true
        )

        private val mockPresentationRequestListResult = ProcessInvitationResult.PresentationRequestCredentialList(
            listOf(mockCompatibleCredential),
            mockPresentationRequest,
            shouldCheckTrustStatement = true,
        )

        private val mockSuccesses: List<ProcessInvitationResult> = listOf(
            ProcessInvitationResult.CredentialOffer(0L),
            ProcessInvitationResult.PresentationRequest(
                CompatibleCredential(0L, listOf()),
                mockPresentationRequest,
                shouldCheckTrustStatement = true,
            ),
            ProcessInvitationResult.PresentationRequestCredentialList(listOf(), mockPresentationRequest, shouldCheckTrustStatement = true),
        )

        private val mockFailures: List<ProcessInvitationError> = listOf(
            InvitationError.EmptyWallet,
            InvitationError.InvalidCredentialOffer,
            InvitationError.InvalidInput,
            InvitationError.NetworkError,
            InvitationError.NoCompatibleCredential,
            InvitationError.Unexpected,
        )
    }
}

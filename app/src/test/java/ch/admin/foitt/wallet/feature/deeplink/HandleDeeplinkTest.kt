package ch.admin.foitt.wallet.feature.deeplink

import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.ClientIdentifier
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.VerifierInfo
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CompatibleCredential
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.PresentationRequestWithRaw
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.VerificationProcessType
import ch.admin.foitt.wallet.platform.credentialPresentation.mock.MockPresentationRequest.VALID_JWT
import ch.admin.foitt.wallet.platform.deeplink.domain.repository.DeepLinkIntentRepository
import ch.admin.foitt.wallet.platform.deeplink.domain.usecase.HandleDeeplink
import ch.admin.foitt.wallet.platform.deeplink.domain.usecase.implementation.HandleDeeplinkImpl
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationErrorScreenState
import ch.admin.foitt.wallet.platform.invitation.domain.model.ProcessInvitationError
import ch.admin.foitt.wallet.platform.invitation.domain.model.ProcessInvitationResult
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.ProcessInvitation
import ch.admin.foitt.wallet.platform.messageEvents.domain.repository.CredentialEventRepository
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.clearMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import uniffi.heidi_dcql_rust.DcqlQuery
import kotlin.reflect.KClass

class HandleDeeplinkTest {
    @MockK
    private lateinit var mockNavigationManager: NavigationManager

    @MockK
    private lateinit var mockDeepLinkIntentRepository: DeepLinkIntentRepository

    @MockK
    private lateinit var mockEnvironmentSetupRepository: EnvironmentSetupRepository

    @MockK
    private lateinit var mockProcessInvitation: ProcessInvitation

    @MockK
    private lateinit var mockCredentialEventRepository: CredentialEventRepository

    private lateinit var handleDeeplinkUseCase: HandleDeeplink

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery { mockProcessInvitation(invitationUri = any()) } returns Ok(mockCredentialOfferResult)
        coEvery { mockDeepLinkIntentRepository.get() } returns SOME_DEEP_LINK
        coEvery { mockEnvironmentSetupRepository.eIdRequestEnabled } returns false
        coEvery {
            mockNavigationManager.popUpToAndNavigate(popToInclusive = any<KClass<Destination>>(), destination = any())
        } just runs
        coEvery { mockNavigationManager.replaceCurrentWith(any()) } just runs
        coEvery { mockNavigationManager.popBackStackOrToRoot() } just runs
        coEvery { mockDeepLinkIntentRepository.reset() } just runs
        coEvery { mockCredentialEventRepository.setEvent(any()) } just runs

        handleDeeplinkUseCase = HandleDeeplinkImpl(
            navManager = mockNavigationManager,
            deepLinkIntentRepository = mockDeepLinkIntentRepository,
            environmentSetupRepository = mockEnvironmentSetupRepository,
            processInvitation = mockProcessInvitation,
            credentialEventRepository = mockCredentialEventRepository,
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
            mockNavigationManager.popUpToAndNavigate(
                popToInclusive = Destination.OnboardingSuccessScreen::class,
                destination = Destination.HomeScreen
            )
        }
    }

    @Disabled
    @Test
    fun `Coming from onboarding, on no intent, with eId feature enabled, navigates to eId request screen and pop onboarding`() = runTest {
        coEvery { mockDeepLinkIntentRepository.get() } returns null
        coEvery { mockEnvironmentSetupRepository.eIdRequestEnabled } returns true

        handleDeeplinkUseCase(true).navigate()

        coVerifyOrder {
            mockDeepLinkIntentRepository.get()
            mockNavigationManager.popUpToAndNavigate(
                popToInclusive = Destination.OnboardingSuccessScreen::class,
                destination = Destination.EIdIntroScreen
            )
        }
    }

    @Test
    fun `Not being in onboarding, on no intent, navigates up or to root`() = runTest {
        coEvery { mockDeepLinkIntentRepository.get() } returns null

        handleDeeplinkUseCase(false).navigate()

        coVerifyOrder {
            mockDeepLinkIntentRepository.get()
            mockNavigationManager.popBackStackOrToRoot()
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
                mockNavigationManager.popUpToAndNavigate(popToInclusive = Destination.OnboardingSuccessScreen::class, any())
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
                mockNavigationManager.replaceCurrentWith(any())
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
                mockNavigationManager.popUpToAndNavigate(popToInclusive = Destination.OnboardingSuccessScreen::class, destination = any())
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
                mockNavigationManager.replaceCurrentWith(any<Destination.InvitationFailureScreen>())
            }
            clearMocks(mockNavigationManager, answers = false)
        }
    }

    @Test
    fun `On deeplink handling, not in onboarding, navigate to generic error screen`() = runTest {
        mockFailures.forEach { failure ->
            coEvery { mockProcessInvitation(SOME_DEEP_LINK) } returns Err(failure)
            handleDeeplinkUseCase(false).navigate()

            coVerify(exactly = 1) {
                mockNavigationManager.replaceCurrentWith(any<Destination.InvitationFailureScreen>())
            }
            clearMocks(mockNavigationManager, answers = false)
        }
    }

    @Test
    fun `On deeplink handling, not in onboarding, navigate to error screen`() = runTest {
        mockSpecFailures.forEach { failure ->
            coEvery { mockProcessInvitation(SOME_DEEP_LINK) } returns Err(failure)
            handleDeeplinkUseCase(false).navigate()

            coVerify(exactly = 1) {
                mockNavigationManager.replaceCurrentWith(any<Destination.GenericErrorScreen>())
            }
            clearMocks(mockNavigationManager, answers = false)
        }
    }

    @Test
    fun `On credential offer, navigates to credential offer screen`() = runTest {
        coEvery { mockProcessInvitation(SOME_DEEP_LINK) } returns Ok(mockCredentialOfferResult)
        handleDeeplinkUseCase(false).navigate()

        coVerify(exactly = 1) {
            mockNavigationManager.replaceCurrentWith(
                destination = Destination.CredentialOfferScreen(credentialId = mockCredentialOfferResult.credentialId),
            )
        }
    }

    @ParameterizedTest
    @ValueSource(
        booleans = [true, false]
    )
    fun `On deferred credential, do not navigate anywhere, and set a credential notification`(fromOnboarding: Boolean) = runTest {
        coEvery { mockProcessInvitation(SOME_DEEP_LINK) } returns Ok(mockDeferredCredentialResult)

        handleDeeplinkUseCase(fromOnboarding = fromOnboarding).navigate()

        coVerifyOrder {
            mockDeepLinkIntentRepository.reset()
            mockProcessInvitation(invitationUri = any())
            mockCredentialEventRepository.setEvent(any())
        }

        coVerify(exactly = 0) {
            mockNavigationManager.replaceCurrentWith(any())
            mockNavigationManager.navigateTo(any())
            mockNavigationManager.popBackStackOrToRoot()
        }
    }

    @Test
    fun `On presentation request, navigate to presentation request screen`() = runTest {
        coEvery { mockProcessInvitation(SOME_DEEP_LINK) } returns Ok(mockPresentationRequestResult)
        handleDeeplinkUseCase(false).navigate()

        coVerify(exactly = 1) {
            mockNavigationManager.replaceCurrentWith(any())
            mockNavigationManager.replaceCurrentWith(
                destination = Destination.PresentationRequestScreen(
                    compatibleCredential = mockPresentationRequestResult.credential,
                    presentationRequestWithRaw = mockPresentationRequestResult.request,
                ),
            )
        }
    }

    @Test
    fun `On presentation request with multiple credentials, navigate to presentation credential list screen`() = runTest {
        coEvery { mockProcessInvitation(SOME_DEEP_LINK) } returns Ok(mockPresentationRequestListResult)
        handleDeeplinkUseCase(false).navigate()

        coVerify(exactly = 1) {
            mockNavigationManager.replaceCurrentWith(any())
            mockNavigationManager.replaceCurrentWith(
                destination = Destination.PresentationCredentialListScreen(
                    compatibleCredentials = mockPresentationRequestListResult.credentials,
                    presentationRequestWithRaw = mockPresentationRequestListResult.request,
                ),
            )
        }
    }

    @TestFactory
    fun `On deeplink processing failure, navigate to the defined error screen`() = runTest {
        val definedErrorDestinations: Map<ProcessInvitationError, Destination> = mapOf(
            InvitationError.InvalidPresentationRequest to Destination.InvitationFailureScreen(
                invitationError = InvitationErrorScreenState.INVALID_PRESENTATION,
                responseUri = null,
                state = null,
            ),
            InvitationError.EmptyWallet("some uri", "state") to Destination.InvitationFailureScreen(
                invitationError = InvitationErrorScreenState.EMPTY_WALLET,
                responseUri = null,
                state = null,
            ),
            InvitationError.InvalidCredentialOffer to Destination.InvitationFailureScreen(
                invitationError = InvitationErrorScreenState.INVALID_CREDENTIAL,
                responseUri = null,
                state = null,
            ),
            InvitationError.InvalidInput to Destination.InvitationFailureScreen(
                invitationError = InvitationErrorScreenState.INVALID_CREDENTIAL,
                responseUri = null,
                state = null,
            ),
            InvitationError.NoCompatibleCredential("some uri", "state") to Destination.InvitationFailureScreen(
                invitationError = InvitationErrorScreenState.NO_COMPATIBLE_CREDENTIAL,
                responseUri = null,
                state = null,
            ),
            InvitationError.Unexpected to Destination.InvitationFailureScreen(
                invitationError = InvitationErrorScreenState.UNEXPECTED,
                responseUri = null,
                state = null,
            ),
            InvitationError.NetworkError to Destination.InvitationFailureScreen(
                invitationError = InvitationErrorScreenState.NETWORK_ERROR,
                responseUri = null,
                state = null,
            ),
            InvitationError.UnknownIssuer to Destination.InvitationFailureScreen(
                invitationError = InvitationErrorScreenState.UNKNOWN_ISSUER,
                responseUri = null,
                state = null,
            ),
        )

        definedErrorDestinations.forEach { (error, destination) ->
            coEvery { mockProcessInvitation(SOME_DEEP_LINK) } returns Err(error)
            handleDeeplinkUseCase(false).navigate()

            coVerify(exactly = 1) {
                mockNavigationManager.replaceCurrentWith(any())
                mockNavigationManager.replaceCurrentWith(
                    destination = destination,
                )
            }
            clearMocks(mockNavigationManager, answers = false)
        }
    }

    companion object {
        private val mockDcqlQuery = mockk<DcqlQuery>(relaxed = true)
        private const val SOME_DEEP_LINK = "openid-credential-offer://credential_offer=..."
        private val mockCredentialOfferResult = ProcessInvitationResult.CredentialOffer(0L)
        private val mockDeferredCredentialResult = ProcessInvitationResult.DeferredCredential(0L)

        private val mockAuthorizationRequest = AuthorizationRequest(
            nonce = "iusto",
            responseUri = "tincidunt",
            responseMode = "suscipit",
            clientIdentifier = mockk<ClientIdentifier>(),
            responseType = "responseType",
            dcqlQuery = mockDcqlQuery,
            clientMetaData = null,
            state = null,
            expectedOrigins = emptyList(),
            verifierInfo = listOf(
                VerifierInfo(
                    format = "jwt",
                    data = Jwt(
                        rawJwt = VALID_JWT
                    )
                )
            ),
            scope = null
        )

        private val mockPresentationRequestWithRaw = PresentationRequestWithRaw(
            authorizationRequest = mockAuthorizationRequest,
            rawPresentationRequest = "raw presentation request",
            verificationProcessType = VerificationProcessType.NETWORK,
            dcqlQuery = mockDcqlQuery,
        )

        private val mockCompatibleCredential = CompatibleCredential(
            credentialId = mockCredentialOfferResult.credentialId,
            presentationPaths = listOf(),
            dcqlQueryId = "1"
        )

        private val mockPresentationRequestResult = ProcessInvitationResult.PresentationRequest(
            mockCompatibleCredential,
            mockPresentationRequestWithRaw,
        )

        private val mockPresentationRequestListResult = ProcessInvitationResult.PresentationRequestCredentialList(
            setOf(mockCompatibleCredential),
            mockPresentationRequestWithRaw,
        )

        private val mockSuccesses: List<ProcessInvitationResult> = listOf(
            ProcessInvitationResult.CredentialOffer(0L),
            ProcessInvitationResult.PresentationRequest(
                CompatibleCredential(0L, listOf(), "1"),
                mockPresentationRequestWithRaw,
            ),
            ProcessInvitationResult.PresentationRequestCredentialList(
                setOf(),
                mockPresentationRequestWithRaw,
            ),
        )

        private val mockFailures: List<ProcessInvitationError> = listOf(
            InvitationError.EmptyWallet("some uri", "state"),
            InvitationError.InvalidCredentialOffer,
            InvitationError.InvalidInput,
            InvitationError.NetworkError,
            InvitationError.NoCompatibleCredential("some uri", "state"),
            InvitationError.MetadataMisconfiguration("Message"),
            InvitationError.Unexpected,
        )

        private val mockSpecFailures = listOf(
            InvitationError.CredentialRequestDenied,
            InvitationError.InsufficientScope,
            InvitationError.InvalidClient,
            InvitationError.InvalidCredentialRequest,
            InvitationError.InvalidEncryptionParameters,
            InvitationError.InvalidNonce,
            InvitationError.InvalidProof,
            InvitationError.InvalidRequest,
            InvitationError.InvalidRequestBearerToken,
            InvitationError.InvalidToken,
            InvitationError.UnauthorizedClient,
            InvitationError.UnauthorizedGrantType,
            InvitationError.UnknownCredentialConfiguration,
            InvitationError.UnknownCredentialIdentifier,
        )
    }
}

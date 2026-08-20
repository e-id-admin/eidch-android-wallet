package ch.admin.foitt.wallet.feature.home.presentation

import android.content.Context
import ch.admin.foitt.wallet.feature.credentialDetail.domain.usecase.HasExhaustedBatchCopies
import ch.admin.foitt.wallet.feature.home.domain.usecase.DeleteEIdRequestCase
import ch.admin.foitt.wallet.feature.home.domain.usecase.EIdRequestsPriorityOrdering
import ch.admin.foitt.wallet.feature.home.domain.usecase.GetEIdRequestsFlow
import ch.admin.foitt.wallet.feature.otp.domain.repository.OtpStateCompletionRepository
import ch.admin.foitt.wallet.platform.batch.domain.usecase.RefreshBatchCredentials
import ch.admin.foitt.wallet.platform.credential.domain.repository.CredentialRefreshRepository
import ch.admin.foitt.wallet.platform.credential.domain.usecase.RefreshDeferredCredentials
import ch.admin.foitt.wallet.platform.credential.presentation.adapter.GetCredentialCardState
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.UpdateAllCredentialStatuses
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.PollSIdRequestAfterFileSubmit
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.UpdateAllSIdStatuses
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.messageEvents.domain.model.CredentialEvent
import ch.admin.foitt.wallet.platform.messageEvents.domain.repository.CredentialEventRepository
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.GetCredentialsWithDetailsFlow
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.GetDeferredCredentialsWithDetailsFlow
import io.mockk.MockKAnnotations
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockKExtension::class)
class HomeViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @MockK(relaxed = true)
    private lateinit var appContext: Context

    @MockK(relaxed = true)
    private lateinit var getCredentialsWithDetailsFlow: GetCredentialsWithDetailsFlow

    @MockK(relaxed = true)
    private lateinit var getDeferredCredentialsWithDetailsFlow: GetDeferredCredentialsWithDetailsFlow

    @MockK(relaxed = true)
    private lateinit var getEIdRequestsFlow: GetEIdRequestsFlow

    @MockK(relaxed = true)
    private lateinit var getCredentialCardState: GetCredentialCardState

    @MockK(relaxed = true)
    private lateinit var hasExhaustedBatchCopies: HasExhaustedBatchCopies

    @MockK(relaxed = true)
    private lateinit var updateAllCredentialStatuses: UpdateAllCredentialStatuses

    @MockK(relaxed = true)
    private lateinit var updateAllSIdStatuses: UpdateAllSIdStatuses

    @MockK(relaxed = true)
    private lateinit var refreshDeferredCredentials: RefreshDeferredCredentials

    @MockK(relaxed = true)
    private lateinit var refreshBatchCredentials: RefreshBatchCredentials

    @MockK(relaxed = true)
    private lateinit var credentialRefreshRepository: CredentialRefreshRepository

    @MockK(relaxed = true)
    private lateinit var deleteEIdRequestCase: DeleteEIdRequestCase

    @MockK(relaxed = true)
    private lateinit var environmentSetupRepository: EnvironmentSetupRepository

    @MockK(relaxed = true)
    private lateinit var navManager: NavigationManager

    @MockK(relaxed = true)
    private lateinit var credentialEventRepository: CredentialEventRepository

    @MockK(relaxed = true)
    private lateinit var otpStateCompletionRepository: OtpStateCompletionRepository

    @MockK(relaxed = true)
    private lateinit var eIdRequestsPriorityOrdering: EIdRequestsPriorityOrdering

    @MockK(relaxed = true)
    private lateinit var pollSIdRequestAfterFileSubmit: PollSIdRequestAfterFileSubmit

    @MockK(relaxed = true)
    private lateinit var setTopBarState: SetTopBarState

    private lateinit var viewModel: HomeViewModel

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)

        every { credentialEventRepository.event } returns MutableStateFlow(CredentialEvent.NONE)
        every { environmentSetupRepository.eIdRequestEnabled } returns false
        every { environmentSetupRepository.batchIssuanceEnabled } returns true
        every { credentialRefreshRepository.isRefreshDue() } returns true

        viewModel = HomeViewModel(
            appContext = appContext,
            getCredentialsWithDetailsFlow = getCredentialsWithDetailsFlow,
            getDeferredCredentialsWithDetailsFlow = getDeferredCredentialsWithDetailsFlow,
            getEIdRequestsFlow = getEIdRequestsFlow,
            getCredentialCardState = getCredentialCardState,
            hasExhaustedBatchCopies = hasExhaustedBatchCopies,
            updateAllCredentialStatuses = updateAllCredentialStatuses,
            updateAllSIdStatuses = updateAllSIdStatuses,
            refreshDeferredCredentials = refreshDeferredCredentials,
            refreshBatchCredentials = refreshBatchCredentials,
            credentialRefreshRepository = credentialRefreshRepository,
            deleteEIdRequestCase = deleteEIdRequestCase,
            environmentSetupRepository = environmentSetupRepository,
            navManager = navManager,
            credentialEventRepository = credentialEventRepository,
            otpStateCompletionRepository = otpStateCompletionRepository,
            eIdRequestsPriorityOrdering = eIdRequestsPriorityOrdering,
            pollSIdRequestAfterFileSubmit = pollSIdRequestAfterFileSubmit,
            setTopBarState = setTopBarState,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    @Test
    fun `onResume when refresh is due refreshes deferred, batch and credential statuses once and stamps the cooldown`() =
        runTest(testDispatcher) {
            viewModel.onResume()
            advanceUntilIdle()

            verify(exactly = 1) { credentialRefreshRepository.markRefreshed() }
            coVerify(exactly = 1) { refreshDeferredCredentials() }
            coVerify(exactly = 1) { refreshBatchCredentials() }
            coVerify(exactly = 1) { updateAllCredentialStatuses() }
        }

    @Test
    fun `onResume refreshes the batch credentials before updating the credential statuses`() = runTest(testDispatcher) {
        viewModel.onResume()
        advanceUntilIdle()

        coVerifyOrder {
            refreshBatchCredentials()
            updateAllCredentialStatuses()
        }
    }

    @Test
    fun `onResume does not refresh the batch credentials when batch issuance is disabled`() = runTest(testDispatcher) {
        every { environmentSetupRepository.batchIssuanceEnabled } returns false

        viewModel.onResume()
        advanceUntilIdle()

        coVerify(exactly = 0) { refreshBatchCredentials() }
        coVerify(exactly = 1) { updateAllCredentialStatuses() }
    }

    @Test
    fun `onResume when refresh is not due does nothing`() = runTest(testDispatcher) {
        every { credentialRefreshRepository.isRefreshDue() } returns false

        viewModel.onResume()
        advanceUntilIdle()

        verify(exactly = 0) { credentialRefreshRepository.markRefreshed() }
        coVerify(exactly = 0) { refreshDeferredCredentials() }
        coVerify(exactly = 0) { refreshBatchCredentials() }
        coVerify(exactly = 0) { updateAllCredentialStatuses() }
    }

    @Test
    fun `concurrent onResume while a refresh is in flight launches only one update`() = runTest(testDispatcher) {
        // No advanceUntilIdle between calls: the first launch sets statusRefreshJob, the second is guarded out.
        viewModel.onResume()
        viewModel.onResume()
        advanceUntilIdle()

        verify(exactly = 1) { credentialRefreshRepository.markRefreshed() }
        coVerify(exactly = 1) { updateAllCredentialStatuses() }
    }

    @Test
    fun `onRefresh ignores the cooldown and refreshes everything`() = runTest(testDispatcher) {
        every { credentialRefreshRepository.isRefreshDue() } returns false

        viewModel.onRefresh()
        advanceUntilIdle()

        verify(exactly = 1) { credentialRefreshRepository.markRefreshed() }
        coVerify(exactly = 1) { refreshDeferredCredentials() }
        coVerify(exactly = 1) { refreshBatchCredentials() }
        coVerify(exactly = 1) { updateAllSIdStatuses() }
        coVerify(exactly = 1) { updateAllCredentialStatuses() }
    }

    @Test
    fun `onRefresh while a refresh is already running does not launch a second one`() = runTest(testDispatcher) {
        // Two calls before idle: the first sets isRefreshing, the second is guarded out.
        viewModel.onRefresh()
        viewModel.onRefresh()
        advanceUntilIdle()

        verify(exactly = 1) { credentialRefreshRepository.markRefreshed() }
        coVerify(exactly = 1) { updateAllCredentialStatuses() }
    }
}

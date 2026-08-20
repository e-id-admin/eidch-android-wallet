package ch.admin.foitt.wallet.feature.settings.presentation

import android.content.Context
import ch.admin.foitt.wallet.feature.otp.domain.repository.OtpStateCompletionRepository
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.domain.model.Destination
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.unmockkAll
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(MockKExtension::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @MockK(relaxed = true)
    private lateinit var navManager: NavigationManager

    @MockK(relaxed = true)
    private lateinit var otpStateCompletionRepository: OtpStateCompletionRepository

    @MockK(relaxed = true)
    private lateinit var appContext: Context

    @MockK(relaxed = true)
    private lateinit var environmentSetupRepository: EnvironmentSetupRepository

    @MockK(relaxed = true)
    private lateinit var setTopBarState: SetTopBarState

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        Dispatchers.setMain(testDispatcher)

        every { environmentSetupRepository.devsSettingsEnabled } returns false
        every { environmentSetupRepository.isLottieViewerEnabled } returns false
        coEvery { otpStateCompletionRepository.getOtpFlowWasDone() } returns false
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
        Dispatchers.resetMain()
    }

    // devsSettingsEnabled is read once at construction, so each test stubs the flavor before creating the view model.
    private fun createViewModel() = SettingsViewModel(
        navManager = navManager,
        otpStateCompletionRepository = otpStateCompletionRepository,
        appContext = appContext,
        environmentSetupRepository = environmentSetupRepository,
        setTopBarState = setTopBarState,
    )

    @Test
    fun `devsSettingsEnabled is true on flavors that enable the devs section`() = runTest(testDispatcher) {
        every { environmentSetupRepository.devsSettingsEnabled } returns true

        assertEquals(true, createViewModel().devsSettingsEnabled)
    }

    @Test
    fun `devsSettingsEnabled is false on flavors that disable the devs section`() = runTest(testDispatcher) {
        every { environmentSetupRepository.devsSettingsEnabled } returns false

        assertEquals(false, createViewModel().devsSettingsEnabled)
    }

    @Test
    fun `onLottieViewer is null when the lottie viewer is disabled`() = runTest(testDispatcher) {
        every { environmentSetupRepository.isLottieViewerEnabled } returns false

        assertNull(createViewModel().onLottieViewer)
    }

    @Test
    fun `onLottieViewer navigates to the lottie viewer when enabled`() = runTest(testDispatcher) {
        every { environmentSetupRepository.isLottieViewerEnabled } returns true

        val onLottieViewer = createViewModel().onLottieViewer
        assertNotNull(onLottieViewer)
        onLottieViewer?.invoke()

        verify(exactly = 1) { navManager.navigateTo(Destination.LottieViewerScreen) }
    }

    @Test
    fun `the lottie viewer is independent of the devs section flag`() = runTest(testDispatcher) {
        every { environmentSetupRepository.devsSettingsEnabled } returns true
        every { environmentSetupRepository.isLottieViewerEnabled } returns false

        val viewModel = createViewModel()

        assertEquals(true, viewModel.devsSettingsEnabled)
        assertNull(viewModel.onLottieViewer)
    }

    @Test
    fun `otpBypassValue is seeded from the stored otp completion state`() = runTest(testDispatcher) {
        coEvery { otpStateCompletionRepository.getOtpFlowWasDone() } returns true

        val viewModel = createViewModel()
        advanceUntilIdle()

        assertEquals(true, viewModel.otpBypassValue.value)
    }

    @Test
    fun `onChangeOtpBypass flips the stored value and the exposed state`() = runTest(testDispatcher) {
        val viewModel = createViewModel()
        advanceUntilIdle()

        viewModel.onChangeOtpBypass()
        advanceUntilIdle()

        coVerify(exactly = 1) { otpStateCompletionRepository.setOtpFlowWasDone(isCompleted = true) }
        assertEquals(true, viewModel.otpBypassValue.value)

        viewModel.onChangeOtpBypass()
        advanceUntilIdle()

        coVerify(exactly = 1) { otpStateCompletionRepository.setOtpFlowWasDone(isCompleted = false) }
        assertEquals(false, viewModel.otpBypassValue.value)
    }

    @Test
    fun `settings entries navigate to their destinations`() = runTest(testDispatcher) {
        val viewModel = createViewModel()

        viewModel.onSecurityAndPrivacy()
        viewModel.onLanguage()
        viewModel.onAccessibility()
        viewModel.onLicenses()
        viewModel.onImprint()

        verify(exactly = 1) { navManager.navigateTo(Destination.SecuritySettingsScreen) }
        verify(exactly = 1) { navManager.navigateTo(Destination.LanguageScreen) }
        verify(exactly = 1) { navManager.navigateTo(Destination.AccessibilityScreen) }
        verify(exactly = 1) { navManager.navigateTo(Destination.LicencesScreen) }
        verify(exactly = 1) { navManager.navigateTo(Destination.ImpressumScreen) }
    }
}

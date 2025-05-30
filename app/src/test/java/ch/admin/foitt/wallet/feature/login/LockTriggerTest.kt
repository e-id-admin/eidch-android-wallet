package ch.admin.foitt.wallet.feature.login

import ch.admin.foitt.wallet.feature.login.domain.usecase.IsDeviceSecureLockScreenConfigured
import ch.admin.foitt.wallet.feature.login.domain.usecase.implementation.LockTriggerImpl
import ch.admin.foitt.wallet.platform.appLifecycleRepository.domain.model.AppLifecycleState
import ch.admin.foitt.wallet.platform.appLifecycleRepository.domain.usecase.GetAppLifecycleState
import ch.admin.foitt.wallet.platform.database.domain.usecase.CloseAppDatabase
import ch.admin.foitt.wallet.platform.database.domain.usecase.IsAppDatabaseOpen
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.walletcomposedestinations.destinations.Destination
import ch.admin.foitt.walletcomposedestinations.destinations.HomeScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.LockScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.OnboardingIntroScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.UnsecuredDeviceScreenDestination
import com.ramcosta.composedestinations.spec.Direction
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class LockTriggerTest {

    @MockK
    private lateinit var mockNavManager: NavigationManager

    @MockK
    private lateinit var mockCloseAppDatabase: CloseAppDatabase

    @MockK
    private lateinit var mockGetAppLifecycleState: GetAppLifecycleState

    @MockK
    private lateinit var mockIsAppDatabaseOpen: IsAppDatabaseOpen

    @MockK
    private lateinit var mockIsDeviceSecureLockScreenConfigured: IsDeviceSecureLockScreenConfigured

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        coEvery { mockNavManager.navigateTo(direction = any()) } just Runs
        coEvery { mockCloseAppDatabase() } just Runs
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Standard screens in background with open DB and a device lockscreen, lock DB and navigate to lock screen`() = lockTest(
        currentDestination = HomeScreenDestination,
        appLifecycleState = AppLifecycleState.Background,
        isAppDatabaseOpen = true,
        isDeviceLockScreenConfigured = true,
        isCloseAppDatabaseCalled = true,
        newDestination = LockScreenDestination,
    )

    @Test
    fun `Standard screens in background with open DB and no device lockscreen, lock DB and navigate to no device lockscreen screen`() = lockTest(
        currentDestination = HomeScreenDestination,
        appLifecycleState = AppLifecycleState.Background,
        isAppDatabaseOpen = true,
        isDeviceLockScreenConfigured = false,
        isCloseAppDatabaseCalled = true,
        newDestination = UnsecuredDeviceScreenDestination,
    )

    @Test
    fun `Standard screens in background with closed DB and a device lockscreen, lock DB and navigate to lock screen`() = lockTest(
        currentDestination = HomeScreenDestination,
        appLifecycleState = AppLifecycleState.Background,
        isAppDatabaseOpen = false,
        isDeviceLockScreenConfigured = true,
        isCloseAppDatabaseCalled = true,
        newDestination = LockScreenDestination,
    )

    @Test
    fun `Standard screens in background with closed DB and no device lockscreen, lock DB and navigate to no device lockscreen screen`() = lockTest(
        currentDestination = HomeScreenDestination,
        appLifecycleState = AppLifecycleState.Background,
        isAppDatabaseOpen = false,
        isDeviceLockScreenConfigured = false,
        isCloseAppDatabaseCalled = true,
        newDestination = UnsecuredDeviceScreenDestination,
    )

    @Test
    fun `Standard screens in foreground with open DB and a device lockscreen, do nothing`() = lockTest(
        currentDestination = HomeScreenDestination,
        appLifecycleState = AppLifecycleState.Foreground,
        isAppDatabaseOpen = true,
        isDeviceLockScreenConfigured = true,
        isCloseAppDatabaseCalled = false,
        newDestination = null,
    )

    @Test
    fun `Standard screens in foreground with open DB and no device lockscreen, lock DB and navigate to no device lockscreen screen`() = lockTest(
        currentDestination = HomeScreenDestination,
        appLifecycleState = AppLifecycleState.Foreground,
        isAppDatabaseOpen = true,
        isDeviceLockScreenConfigured = false,
        isCloseAppDatabaseCalled = true,
        newDestination = UnsecuredDeviceScreenDestination,
    )

    @Test
    fun `Standard screens in foreground with closed DB and a device lockscreen, lock DB and navigate to lock screen`() = lockTest(
        currentDestination = HomeScreenDestination,
        appLifecycleState = AppLifecycleState.Foreground,
        isAppDatabaseOpen = false,
        isDeviceLockScreenConfigured = true,
        isCloseAppDatabaseCalled = true,
        newDestination = LockScreenDestination,
    )

    @Test
    fun `Standard screens in foreground with closed DB and no device lockscreen, lock DB and navigate to no device lockscreen screen`() = lockTest(
        currentDestination = HomeScreenDestination,
        appLifecycleState = AppLifecycleState.Foreground,
        isAppDatabaseOpen = false,
        isDeviceLockScreenConfigured = false,
        isCloseAppDatabaseCalled = true,
        newDestination = UnsecuredDeviceScreenDestination,
    )

    @Test
    fun `Whitelisted screens in background with open DB and a device lockscreen, only lock DB`() = lockTest(
        currentDestination = OnboardingIntroScreenDestination,
        appLifecycleState = AppLifecycleState.Background,
        isAppDatabaseOpen = true,
        isDeviceLockScreenConfigured = true,
        isCloseAppDatabaseCalled = true,
        newDestination = null,
    )

    @Test
    fun `Whitelisted screens in background with open DB and no device lockscreen, lock DB and navigate to no device lockscreen screen`() = lockTest(
        currentDestination = OnboardingIntroScreenDestination,
        appLifecycleState = AppLifecycleState.Background,
        isAppDatabaseOpen = true,
        isDeviceLockScreenConfigured = false,
        isCloseAppDatabaseCalled = true,
        newDestination = UnsecuredDeviceScreenDestination,
    )

    @Test
    fun `Whitelisted screens in background with closed DB and a device lockscreen, only lock DB`() = lockTest(
        currentDestination = OnboardingIntroScreenDestination,
        appLifecycleState = AppLifecycleState.Background,
        isAppDatabaseOpen = false,
        isDeviceLockScreenConfigured = true,
        isCloseAppDatabaseCalled = true,
        newDestination = null,
    )

    @Test
    fun `Whitelisted screens in background with closed DB and no device lockscreen, lock DB and navigate to no device lockscreen screen`() = lockTest(
        currentDestination = OnboardingIntroScreenDestination,
        appLifecycleState = AppLifecycleState.Background,
        isAppDatabaseOpen = false,
        isDeviceLockScreenConfigured = false,
        isCloseAppDatabaseCalled = true,
        newDestination = UnsecuredDeviceScreenDestination,
    )

    @Test
    fun `Whitelisted screens in foreground with open DB and a device lockscreen, only lock DB`() = lockTest(
        currentDestination = OnboardingIntroScreenDestination,
        appLifecycleState = AppLifecycleState.Foreground,
        isAppDatabaseOpen = true,
        isDeviceLockScreenConfigured = true,
        isCloseAppDatabaseCalled = true,
        newDestination = null,
    )

    @Test
    fun `Whitelisted screens in foreground with open DB and no device lockscreen, lock DB and navigate to no device lockscreen screen`() = lockTest(
        currentDestination = OnboardingIntroScreenDestination,
        appLifecycleState = AppLifecycleState.Foreground,
        isAppDatabaseOpen = true,
        isDeviceLockScreenConfigured = false,
        isCloseAppDatabaseCalled = true,
        newDestination = UnsecuredDeviceScreenDestination,
    )

    @Test
    fun `Whitelisted screens in foreground with closed DB and a device lockscreen, only lock DB`() = lockTest(
        currentDestination = OnboardingIntroScreenDestination,
        appLifecycleState = AppLifecycleState.Foreground,
        isAppDatabaseOpen = false,
        isDeviceLockScreenConfigured = true,
        isCloseAppDatabaseCalled = true,
        newDestination = null,
    )

    @Test
    fun `Whitelisted screens in foreground with closed DB and no device lockscreen, lock DB and navigate to no device lockscreen screen`() = lockTest(
        currentDestination = OnboardingIntroScreenDestination,
        appLifecycleState = AppLifecycleState.Foreground,
        isAppDatabaseOpen = false,
        isDeviceLockScreenConfigured = false,
        isCloseAppDatabaseCalled = true,
        newDestination = UnsecuredDeviceScreenDestination,
    )

    @Test
    fun `NoDevicePinSet screen in foreground with closed DB and no device lockscreen, only lock DB`() = lockTest(
        currentDestination = UnsecuredDeviceScreenDestination,
        appLifecycleState = AppLifecycleState.Foreground,
        isAppDatabaseOpen = false,
        isDeviceLockScreenConfigured = false,
        isCloseAppDatabaseCalled = true,
        newDestination = null,
    )

    @Test
    fun `NoDevicePinSet screen in background with closed DB and no device lockscreen, only lock DB`() = lockTest(
        currentDestination = UnsecuredDeviceScreenDestination,
        appLifecycleState = AppLifecycleState.Background,
        isAppDatabaseOpen = false,
        isDeviceLockScreenConfigured = false,
        isCloseAppDatabaseCalled = true,
        newDestination = null,
    )

    @Test
    fun `Null screen in background with open DB and a device lockscreen, does nothing`() = lockTest(
        currentDestination = null,
        appLifecycleState = AppLifecycleState.Background,
        isAppDatabaseOpen = true,
        isDeviceLockScreenConfigured = true,
        isCloseAppDatabaseCalled = false,
        newDestination = null,
    )

    @Test
    fun `Null screen in background with open DB and no device lockscreen, does nothing`() = lockTest(
        currentDestination = null,
        appLifecycleState = AppLifecycleState.Background,
        isAppDatabaseOpen = true,
        isDeviceLockScreenConfigured = false,
        isCloseAppDatabaseCalled = false,
        newDestination = null,
    )

    @Test
    fun `Null screen in background with closed DB and a device lockscreen, does nothing`() = lockTest(
        currentDestination = null,
        appLifecycleState = AppLifecycleState.Background,
        isAppDatabaseOpen = false,
        isDeviceLockScreenConfigured = true,
        isCloseAppDatabaseCalled = false,
        newDestination = null,
    )

    @Test
    fun `Null screen in background with closed DB and no device lockscreen, does nothing`() = lockTest(
        currentDestination = null,
        appLifecycleState = AppLifecycleState.Background,
        isAppDatabaseOpen = false,
        isDeviceLockScreenConfigured = false,
        isCloseAppDatabaseCalled = false,
        newDestination = null,
    )

    @Test
    fun `Null screen in foreground with open DB and a device lockscreen, does nothing`() = lockTest(
        currentDestination = null,
        appLifecycleState = AppLifecycleState.Foreground,
        isAppDatabaseOpen = true,
        isDeviceLockScreenConfigured = true,
        isCloseAppDatabaseCalled = false,
        newDestination = null,
    )

    @Test
    fun `Null screen in foreground with open DB and no device lockscreen, does nothing`() = lockTest(
        currentDestination = null,
        appLifecycleState = AppLifecycleState.Foreground,
        isAppDatabaseOpen = true,
        isDeviceLockScreenConfigured = false,
        isCloseAppDatabaseCalled = false,
        newDestination = null,
    )

    @Test
    fun `Null screen in foreground with closed DB and a device lockscreen, does nothing`() = lockTest(
        currentDestination = null,
        appLifecycleState = AppLifecycleState.Foreground,
        isAppDatabaseOpen = false,
        isDeviceLockScreenConfigured = true,
        isCloseAppDatabaseCalled = false,
        newDestination = null,
    )

    @Test
    fun `Null screen in foreground with closed DB and no device lockscreen, does nothing`() = lockTest(
        currentDestination = null,
        appLifecycleState = AppLifecycleState.Foreground,
        isAppDatabaseOpen = false,
        isDeviceLockScreenConfigured = false,
        isCloseAppDatabaseCalled = false,
        newDestination = null,
    )

    private fun lockTest(
        currentDestination: Destination?,
        appLifecycleState: AppLifecycleState,
        isAppDatabaseOpen: Boolean,
        isDeviceLockScreenConfigured: Boolean,
        isCloseAppDatabaseCalled: Boolean,
        newDestination: Direction?,
    ) = runTest {
        coEvery { mockNavManager.currentDestinationFlow } returns MutableStateFlow(currentDestination)
        coEvery { mockNavManager.currentDestination } returns currentDestination
        coEvery { mockGetAppLifecycleState() } returns MutableStateFlow(appLifecycleState)
        coEvery { mockIsAppDatabaseOpen.invoke() } returns isAppDatabaseOpen
        coEvery { mockIsDeviceSecureLockScreenConfigured.invoke() } returns isDeviceLockScreenConfigured

        val useCase = LockTriggerImpl(
            navManager = mockNavManager,
            closeAppDatabase = mockCloseAppDatabase,
            getAppLifecycleState = mockGetAppLifecycleState,
            isAppDatabaseOpen = mockIsAppDatabaseOpen,
            isDeviceSecureLockScreenConfigured = mockIsDeviceSecureLockScreenConfigured,
            ioDispatcherScope = backgroundScope,
        )

        useCase().value.navigate()

        verify(isCloseAppDatabaseCalled = isCloseAppDatabaseCalled, destination = newDestination)
    }

    private fun verify(isCloseAppDatabaseCalled: Boolean, destination: Direction?) {
        coVerify(exactly = if (isCloseAppDatabaseCalled) 1 else 0) {
            mockCloseAppDatabase()
        }
        destination?.let {
            coVerify(exactly = 1) {
                mockNavManager.navigateTo(direction = destination)
            }
        } ?: coVerify(exactly = 0) { mockNavManager.navigateTo(any()) }
    }
}

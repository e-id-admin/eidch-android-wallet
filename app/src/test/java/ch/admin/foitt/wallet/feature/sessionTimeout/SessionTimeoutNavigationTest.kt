package ch.admin.foitt.wallet.feature.sessionTimeout

import ch.admin.foitt.wallet.feature.sessionTimeout.domain.SessionTimeoutNavigation
import ch.admin.foitt.wallet.feature.sessionTimeout.domain.implementation.SessionTimeoutNavigationImpl
import ch.admin.foitt.wallet.platform.login.domain.usecase.NavigateToLogin
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.navigation.utils.blackListedDestinationsLockScreen
import ch.admin.foitt.walletcomposedestinations.destinations.BiometricLoginScreenDestination
import ch.admin.foitt.walletcomposedestinations.destinations.HomeScreenDestination
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class SessionTimeoutNavigationTest {

    @MockK
    private lateinit var mockNavManager: NavigationManager

    @MockK
    private lateinit var mockNavigateToLogin: NavigateToLogin

    private lateinit var sessionTimeoutNavigation: SessionTimeoutNavigation

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        sessionTimeoutNavigation = SessionTimeoutNavigationImpl(
            mockNavManager,
            mockNavigateToLogin,
        )

        coEvery { mockNavManager.navigateTo(any()) } just Runs
    }

    @TestFactory
    fun `Blacklisted destinations do not navigate to the login screen`(): List<DynamicTest> {
        return blackListedDestinationsLockScreen.map { destination ->
            DynamicTest.dynamicTest("$destination should be blacklisted") {
                runTest {
                    coEvery { mockNavManager.currentDestination } returns destination
                    assertNull(sessionTimeoutNavigation())
                }
            }
        }
    }

    @Test
    fun `Non-blacklisted destination navigates to the login screen`() = runTest {
        val navToLoginReturn = BiometricLoginScreenDestination

        coEvery { mockNavManager.currentDestination } returns HomeScreenDestination
        coEvery { mockNavigateToLogin() } returns navToLoginReturn

        val result = sessionTimeoutNavigation()

        assertEquals(navToLoginReturn, result)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }
}

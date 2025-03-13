package ch.admin.foitt.wallet.platform.login.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.login.domain.Constants.MAX_LOGIN_ATTEMPTS
import ch.admin.foitt.wallet.platform.login.domain.repository.LoginAttemptsRepository
import ch.admin.foitt.wallet.platform.login.domain.usecase.GetRemainingLoginAttempts
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetRemainingLoginAttemptsImplTest {

    @MockK
    lateinit var mockLoginAttemptsRepository: LoginAttemptsRepository

    private lateinit var getRemainingLoginAttempts: GetRemainingLoginAttempts

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        getRemainingLoginAttempts = GetRemainingLoginAttemptsImpl(
            mockLoginAttemptsRepository,
        )

        coEvery { mockLoginAttemptsRepository.getAttempts() } returns 0
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Initially return max attempts`() = runTest {
        val result = getRemainingLoginAttempts()
        assertEquals(MAX_LOGIN_ATTEMPTS, result)
    }

    @Test
    fun `After 2 attempts return the remaining ones`() = runTest {
        coEvery { mockLoginAttemptsRepository.getAttempts() } returns 2

        val result = getRemainingLoginAttempts()
        assertEquals(3, result)
    }

    @Test
    fun `After max attempts return 0`() = runTest {
        coEvery { mockLoginAttemptsRepository.getAttempts() } returns MAX_LOGIN_ATTEMPTS

        val result = getRemainingLoginAttempts()
        assertEquals(0, result)
    }
}

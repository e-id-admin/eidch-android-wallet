package ch.admin.foitt.wallet.platform.login.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.login.domain.repository.LockoutStartRepository
import ch.admin.foitt.wallet.platform.login.domain.repository.LoginAttemptsRepository
import ch.admin.foitt.wallet.platform.login.domain.usecase.ResetLockout
import io.mockk.MockKAnnotations
import io.mockk.Ordering
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

class ResetLockoutImplTest {

    @MockK
    lateinit var mockLockoutStartRepository: LockoutStartRepository

    @MockK
    lateinit var mockLoginAttemptsRepository: LoginAttemptsRepository

    private lateinit var resetLockout: ResetLockout

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        resetLockout = ResetLockoutImpl(
            mockLockoutStartRepository,
            mockLoginAttemptsRepository,
        )

        coEvery { mockLockoutStartRepository.deleteStartingTime() } just runs
        coEvery { mockLoginAttemptsRepository.deleteAttempts() } just runs
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Correct methods are called`() = runTest {
        resetLockout()

        coVerify(ordering = Ordering.SEQUENCE) {
            mockLockoutStartRepository.deleteStartingTime()
            mockLoginAttemptsRepository.deleteAttempts()
        }
    }
}

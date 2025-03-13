package ch.admin.foitt.wallet.platform.login.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.login.domain.repository.LoginAttemptsRepository
import ch.admin.foitt.wallet.platform.login.domain.usecase.IncreaseFailedLoginAttemptsCounter
import io.mockk.MockKAnnotations
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

class IncreaseFailedLoginAttemptsCounterImplTest {

    @MockK
    lateinit var mockLoginAttemptsRepository: LoginAttemptsRepository

    private lateinit var increaseFailedLoginAttemptsCounter: IncreaseFailedLoginAttemptsCounter

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        increaseFailedLoginAttemptsCounter = IncreaseFailedLoginAttemptsCounterImpl(
            mockLoginAttemptsRepository
        )

        coEvery { mockLoginAttemptsRepository.increase() } just runs
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Verify correct call`() = runTest {
        increaseFailedLoginAttemptsCounter()

        coVerify(exactly = 1) {
            mockLoginAttemptsRepository.increase()
        }
    }
}

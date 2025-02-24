package ch.admin.foitt.wallet.platform.login.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.appSetupState.domain.repository.UseBiometricLoginRepository
import ch.admin.foitt.wallet.platform.login.domain.usecase.BiometricLoginEnabled
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class BiometricLoginEnabledImplTest {
    @MockK
    private lateinit var mockUseBiometricLoginRepository: UseBiometricLoginRepository

    private lateinit var useCase: BiometricLoginEnabled

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery { mockUseBiometricLoginRepository.getUseBiometricLogin() } returns true

        useCase = BiometricLoginEnabledImpl(
            repo = mockUseBiometricLoginRepository,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `BiometricLoginEnabled gets the value from the UseBiometricLoginRepo`() = runTest {
        val result = useCase()

        assertEquals(true, result)
    }
}

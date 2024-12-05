package ch.admin.foitt.wallet.platform.biometrics

import android.annotation.SuppressLint
import ch.admin.foitt.wallet.platform.appSetupState.domain.repository.UseBiometricLoginRepository
import ch.admin.foitt.wallet.platform.biometrics.domain.usecase.SaveUseBiometricLogin
import ch.admin.foitt.wallet.platform.biometrics.domain.usecase.implementation.SaveUseBiometricLoginImpl
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.runs
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SaveUseBiometricLoginImplTest {

    @MockK
    private lateinit var mockUseBiometricLoginRepository: UseBiometricLoginRepository

    private lateinit var testedUseCase: SaveUseBiometricLogin

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        testedUseCase = SaveUseBiometricLoginImpl(
            repo = mockUseBiometricLoginRepository
        )

        // Sunny path by default
        coEvery { mockUseBiometricLoginRepository.saveUseBiometricLogin(any()) } just runs
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @SuppressLint("CheckResult")
    @Test
    fun `Call the usecase calls the repository method with the right parameter `() = runTest {
        testedUseCase(isEnabled = true)

        coVerifyOrder {
            mockUseBiometricLoginRepository.saveUseBiometricLogin(true)
        }

        testedUseCase(isEnabled = false)

        coVerifyOrder {
            mockUseBiometricLoginRepository.saveUseBiometricLogin(false)
        }
    }
}

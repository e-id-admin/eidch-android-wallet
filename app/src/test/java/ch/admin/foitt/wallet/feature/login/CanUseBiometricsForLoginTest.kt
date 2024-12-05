package ch.admin.foitt.wallet.feature.login

import ch.admin.foitt.wallet.platform.biometricPrompt.domain.model.BiometricManagerResult
import ch.admin.foitt.wallet.platform.biometricPrompt.domain.usecase.BiometricsStatus
import ch.admin.foitt.wallet.platform.biometrics.domain.model.BiometricsError
import ch.admin.foitt.wallet.platform.biometrics.domain.usecase.GetBiometricsCipher
import ch.admin.foitt.wallet.platform.login.domain.model.CanUseBiometricsForLoginResult
import ch.admin.foitt.wallet.platform.login.domain.usecase.BiometricLoginEnabled
import ch.admin.foitt.wallet.platform.login.domain.usecase.CanUseBiometricsForLogin
import ch.admin.foitt.wallet.platform.login.domain.usecase.implementation.CanUseBiometricsForLoginImpl
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.crypto.Cipher

class CanUseBiometricsForLoginTest {
    @MockK
    private lateinit var mockBiometricsStatus: BiometricsStatus

    @MockK
    private lateinit var mockBiometricLoginEnabled: BiometricLoginEnabled

    @MockK
    private lateinit var mockGetBiometricsCipher: GetBiometricsCipher

    @MockK
    private lateinit var mockCipher: Cipher

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var testedUseCase: CanUseBiometricsForLogin

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery { mockBiometricLoginEnabled.invoke() } returns true
        coEvery { mockBiometricsStatus.invoke() } returns BiometricManagerResult.Available
        coEvery { mockGetBiometricsCipher.invoke() } returns Ok(mockCipher)

        testedUseCase = CanUseBiometricsForLoginImpl(
            ioDispatcher = testDispatcher,
            biometricsStatus = mockBiometricsStatus,
            biometricLoginEnabled = mockBiometricLoginEnabled,
            getBiometricsCipher = mockGetBiometricsCipher,
        )
    }

    @Test
    fun `biometrics set up on device and biometrics available returns usable`() = runTest(testDispatcher) {
        val result = testedUseCase.invoke()

        assertTrue(result == CanUseBiometricsForLoginResult.Usable)
    }

    @Test
    fun `biometrics set up on device and biometrics can enroll returns removed in settings`() = runTest(testDispatcher) {
        coEvery { mockBiometricsStatus.invoke() } returns BiometricManagerResult.CanEnroll

        val result = testedUseCase.invoke()

        assertTrue(result == CanUseBiometricsForLoginResult.RemovedInDeviceSettings)
    }

    @Test
    fun `biometrics set up on device and biometrics disabled returns deactivated in settings`() = runTest(testDispatcher) {
        coEvery { mockBiometricsStatus.invoke() } returns BiometricManagerResult.Disabled

        val result = testedUseCase.invoke()

        assertTrue(result == CanUseBiometricsForLoginResult.DeactivatedInDeviceSettings)
    }

    @Test
    fun `biometrics set up on device and biometrics unsupported returns unexpected`() = runTest(testDispatcher) {
        coEvery { mockBiometricsStatus.invoke() } returns BiometricManagerResult.Unsupported

        val result = testedUseCase.invoke()

        assertTrue(result == CanUseBiometricsForLoginResult.NoHardwareAvailable)
    }

    @Test
    fun `biometrics not set up on device returns unusable`() = runTest(testDispatcher) {
        coEvery { mockBiometricLoginEnabled.invoke() } returns false

        val result = testedUseCase.invoke()

        assertTrue(result == CanUseBiometricsForLoginResult.NotSetUpInApp)
    }

    @Test
    fun `biometrics set up in the app, but key invalid, returns biometrics changed`() = runTest(testDispatcher) {
        coEvery { mockGetBiometricsCipher() } returns Err(BiometricsError.InvalidatedKey)

        val result = testedUseCase.invoke()

        assertTrue(result == CanUseBiometricsForLoginResult.Changed)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }
}

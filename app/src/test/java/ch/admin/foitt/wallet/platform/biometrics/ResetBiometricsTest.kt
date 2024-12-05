package ch.admin.foitt.wallet.platform.biometrics

import ch.admin.foitt.wallet.platform.appSetupState.domain.repository.UseBiometricLoginRepository
import ch.admin.foitt.wallet.platform.biometrics.domain.model.ResetBiometricsError
import ch.admin.foitt.wallet.platform.biometrics.domain.usecase.ResetBiometrics
import ch.admin.foitt.wallet.platform.biometrics.domain.usecase.implementation.ResetBiometricsImpl
import ch.admin.foitt.wallet.platform.keystoreCrypto.domain.model.DeleteSecretKeyError
import ch.admin.foitt.wallet.platform.passphrase.domain.repository.PassphraseRepository
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.DeleteSecretKey
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.getError
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.runs
import io.mockk.unmockkAll
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.security.KeyStoreException

class ResetBiometricsTest {

    @MockK
    private lateinit var mockDeleteSecretKey: DeleteSecretKey

    @MockK
    private lateinit var mockPassphraseRepository: PassphraseRepository

    @MockK
    private lateinit var mockUseBiometricLoginRepository: UseBiometricLoginRepository

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var testUseCase: ResetBiometrics

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery { mockDeleteSecretKey() } returns Ok(Unit)
        coEvery { mockPassphraseRepository.deletePassphrase() } just runs
        coEvery { mockUseBiometricLoginRepository.saveUseBiometricLogin(any()) } just runs

        testUseCase = ResetBiometricsImpl(
            deleteSecretKey = mockDeleteSecretKey,
            passphraseRepository = mockPassphraseRepository,
            useBiometricLoginRepository = mockUseBiometricLoginRepository,
            ioDispatcher = testDispatcher,
        )
    }

    @Test
    fun `Disabling biometrics deletes the passphrase, resets biometric login flag, and delete the key`() = runTest(testDispatcher) {
        testUseCase.invoke()

        coVerifyOrder {
            mockPassphraseRepository.deletePassphrase()
            mockUseBiometricLoginRepository.saveUseBiometricLogin(false)
            mockDeleteSecretKey()
        }
    }

    @Test
    fun `Error during key deletion returns an error`() = runTest(testDispatcher) {
        val error = Err(DeleteSecretKeyError.Unexpected(KeyStoreException("keystore exception")))
        coEvery { mockDeleteSecretKey() } returns error

        val result = testUseCase.invoke()

        assertNotNull(result.getError())
        assertTrue(result.getError() is ResetBiometricsError.Unexpected)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }
}

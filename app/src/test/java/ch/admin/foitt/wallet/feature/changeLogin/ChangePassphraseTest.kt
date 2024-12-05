package ch.admin.foitt.wallet.feature.changeLogin

import android.annotation.SuppressLint
import ch.admin.foitt.wallet.feature.changeLogin.domain.model.ChangePassphraseError
import ch.admin.foitt.wallet.feature.changeLogin.domain.usecase.ChangePassphrase
import ch.admin.foitt.wallet.feature.changeLogin.domain.usecase.implementation.ChangePassphraseImpl
import ch.admin.foitt.wallet.platform.appSetupState.domain.repository.UseBiometricLoginRepository
import ch.admin.foitt.wallet.platform.biometrics.domain.usecase.ResetBiometrics
import ch.admin.foitt.wallet.platform.crypto.domain.model.HashDataError
import ch.admin.foitt.wallet.platform.crypto.domain.model.HashedData
import ch.admin.foitt.wallet.platform.database.domain.model.DatabaseError
import ch.admin.foitt.wallet.platform.database.domain.usecase.ChangeDatabasePassphrase
import ch.admin.foitt.wallet.platform.passphrase.domain.model.PepperPassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.model.PepperedData
import ch.admin.foitt.wallet.platform.passphrase.domain.repository.PepperIvRepository
import ch.admin.foitt.wallet.platform.passphrase.domain.repository.SaltRepository
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.HashPassphrase
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.PepperPassphrase
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.SavePassphraseWasDeleted
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ChangePassphraseTest {

    @MockK
    private lateinit var mockHashPassphrase: HashPassphrase

    @MockK
    private lateinit var mockPepperPassphrase: PepperPassphrase

    @MockK
    private lateinit var mockChangeDatabasePassphrase: ChangeDatabasePassphrase

    @MockK
    private lateinit var mockResetBiometrics: ResetBiometrics

    @MockK
    private lateinit var mockSaltRepository: SaltRepository

    @MockK
    private lateinit var mockUseBiometricLoginRepository: UseBiometricLoginRepository

    @MockK
    private lateinit var mockSavePassphraseWasDeleted: SavePassphraseWasDeleted

    @MockK
    private lateinit var mockPepperIvRepository: PepperIvRepository

    private val hashedData = HashedData(byteArrayOf(0, 1), byteArrayOf(1, 0))
    private val pepperedData = PepperedData(byteArrayOf(0, 0), byteArrayOf(1, 1))
    private val isBiometricsEnabled = true

    private lateinit var changePassphrase: ChangePassphrase

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        changePassphrase = ChangePassphraseImpl(
            hashPassphrase = mockHashPassphrase,
            pepperPassphrase = mockPepperPassphrase,
            changeDatabasePassphrase = mockChangeDatabasePassphrase,
            resetBiometrics = mockResetBiometrics,
            saltRepository = mockSaltRepository,
            pepperIvRepository = mockPepperIvRepository,
            useBiometricLoginRepository = mockUseBiometricLoginRepository,
            savePassphraseWasDeleted = mockSavePassphraseWasDeleted,
        )

        // Setup the happy path
        coEvery { mockHashPassphrase.invoke(any(), any()) } returns Ok(hashedData)
        coEvery { mockPepperPassphrase.invoke(any(), any()) } returns Ok(pepperedData)
        coEvery { mockChangeDatabasePassphrase.invoke(newPassphrase = any()) } returns Ok(Unit)
        coEvery { mockResetBiometrics.invoke() } returns Ok(Unit)
        coEvery { mockSaltRepository.save(any()) } just Runs
        coEvery { mockPepperIvRepository.save(any()) } just Runs
        coEvery { mockUseBiometricLoginRepository.getUseBiometricLogin() } returns isBiometricsEnabled
        coEvery { mockSavePassphraseWasDeleted.invoke(any()) } just Runs
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @SuppressLint("CheckResult")
    @Test
    fun `A successful passphrase change should follow specific steps`() = runTest {
        changePassphrase.invoke("123").assertOk()

        coVerifyOrder {
            mockHashPassphrase.invoke(any(), any())
            mockPepperPassphrase.invoke(any(), any())
            mockChangeDatabasePassphrase.invoke(newPassphrase = any())
            mockSaltRepository.save(any())
            mockPepperIvRepository.save(any())
            mockResetBiometrics.invoke()
            mockSavePassphraseWasDeleted.invoke(isBiometricsEnabled)
        }
    }

    @SuppressLint("CheckResult")
    @Test
    fun `A successful passphrase change should reset the salt and pepper`() = runTest {
        changePassphrase.invoke("123").assertOk()

        coVerifyOrder {
            mockHashPassphrase.invoke(any(), initializeSalt = true)
            mockPepperPassphrase.invoke(any(), initializePepper = true)
            mockSaltRepository.save(hashedData.salt)
            mockPepperIvRepository.save(pepperedData.initializationVector)
        }
    }

    @Test
    fun `A successful passphrase change with enabled biometrics should save that the passphrase was deleted`() = runTest {
        coEvery { mockUseBiometricLoginRepository.getUseBiometricLogin() } returns true

        changePassphrase.invoke("123").assertOk()

        coVerifyOrder {
            mockSavePassphraseWasDeleted.invoke(true)
        }
    }

    @Test
    fun `A successful passphrase change with disabled biometrics should not save that the passphrase was deleted`() = runTest {
        coEvery { mockUseBiometricLoginRepository.getUseBiometricLogin() } returns false

        changePassphrase.invoke("123").assertOk()

        coVerifyOrder {
            mockSavePassphraseWasDeleted.invoke(false)
        }
    }

    @SuppressLint("CheckResult")
    @Test
    fun `A failed hashing interrupt the process and return an unexpected error`() = runTest {
        coEvery { mockHashPassphrase.invoke(pin = any(), any()) } returns Err(HashDataError.Unexpected(Exception()))
        val useCaseResult = changePassphrase.invoke("123")

        coVerify(exactly = 0) {
            mockPepperPassphrase.invoke(passphrase = any(), any())
            mockChangeDatabasePassphrase.invoke(newPassphrase = any())
            mockSaltRepository.save(any())
            mockPepperIvRepository.save(any())
        }
        coVerifyOrder {
            mockHashPassphrase.invoke(pin = any(), any())
        }
        useCaseResult.assertErrorType(ChangePassphraseError.Unexpected::class)
    }

    @SuppressLint("CheckResult")
    @Test
    fun `A failed peppering interrupt the process and return an unexpected error`() = runTest {
        coEvery {
            mockPepperPassphrase.invoke(passphrase = any(), any())
        } returns Err(PepperPassphraseError.Unexpected(Exception()))
        val useCaseResult = changePassphrase.invoke("123")

        coVerify(exactly = 0) {
            mockChangeDatabasePassphrase.invoke(newPassphrase = any())
            mockSaltRepository.save(any())
            mockPepperIvRepository.save(any())
        }

        coVerifyOrder {
            mockHashPassphrase.invoke(pin = any(), any())
            mockPepperPassphrase.invoke(passphrase = any(), any())
        }
        useCaseResult.assertErrorType(ChangePassphraseError.Unexpected::class)
    }

    @SuppressLint("CheckResult")
    @Test
    fun `A failed passphrase change interrupt the process and return an unexpected error`() = runTest {
        coEvery {
            mockChangeDatabasePassphrase.invoke(newPassphrase = any())
        } returns Err(DatabaseError.ReKeyFailed(Exception()))
        val useCaseResult = changePassphrase.invoke("123")

        coVerify(exactly = 0) {
            mockSaltRepository.save(any())
            mockPepperIvRepository.save(any())
        }

        coVerifyOrder {
            mockHashPassphrase.invoke(pin = any(), any())
            mockPepperPassphrase.invoke(passphrase = any(), any())
            mockChangeDatabasePassphrase.invoke(newPassphrase = any())
        }
        useCaseResult.assertErrorType(ChangePassphraseError.Unexpected::class)
    }
}

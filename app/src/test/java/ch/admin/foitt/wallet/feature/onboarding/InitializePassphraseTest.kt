package ch.admin.foitt.wallet.feature.onboarding

import android.annotation.SuppressLint
import ch.admin.foitt.wallet.platform.crypto.domain.model.HashDataError
import ch.admin.foitt.wallet.platform.crypto.domain.model.HashedData
import ch.admin.foitt.wallet.platform.database.domain.usecase.CreateAppDatabase
import ch.admin.foitt.wallet.platform.passphrase.domain.model.EncryptAndSavePassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.model.InitializePassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.model.PepperedData
import ch.admin.foitt.wallet.platform.passphrase.domain.repository.PepperIvRepository
import ch.admin.foitt.wallet.platform.passphrase.domain.repository.SaltRepository
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.EncryptAndSavePassphrase
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.HashPassphrase
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.PepperPassphrase
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.implementation.InitializePassphraseImpl
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.get
import com.github.michaelbull.result.getError
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.unmockkAll
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.crypto.Cipher

internal class InitializePassphraseTest {

    @MockK
    private lateinit var mockHashPassphrase: HashPassphrase

    @MockK
    private lateinit var mockPepperPassphrase: PepperPassphrase

    @MockK
    private lateinit var mockEncryptAndSavePassphrase: EncryptAndSavePassphrase

    @MockK
    private lateinit var mockCipher: Cipher

    @MockK
    private lateinit var mockCreateAppDatabase: CreateAppDatabase

    @MockK
    private lateinit var mockSaltRepository: SaltRepository

    @MockK
    private lateinit var mockPepperIvRepository: PepperIvRepository

    private val hashedData = HashedData(byteArrayOf(0, 1), byteArrayOf(1, 0))
    private val pepperedData = PepperedData(byteArrayOf(0, 0), byteArrayOf(1, 1))

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var passPhraseInitUseCase: InitializePassphraseImpl

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        passPhraseInitUseCase = InitializePassphraseImpl(
            ioDispatcher = testDispatcher,
            encryptAndSavePassphrase = mockEncryptAndSavePassphrase,
            createAppDatabase = mockCreateAppDatabase,
            hashPassphrase = mockHashPassphrase,
            pepperPassphrase = mockPepperPassphrase,
            saltRepository = mockSaltRepository,
            pepperIvRepository = mockPepperIvRepository,
        )

        coEvery { mockEncryptAndSavePassphrase.invoke(any(), any()) } returns Ok(Unit)
        coEvery { mockCreateAppDatabase.invoke(any()) } returns Ok(Unit)
        coEvery { mockHashPassphrase.invoke(any(), any()) } returns Ok(hashedData)
        coEvery { mockPepperPassphrase.invoke(any(), any()) } returns Ok(pepperedData)
        coEvery { mockSaltRepository.save(any()) } just Runs
        coEvery { mockPepperIvRepository.save(any()) } just Runs
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @SuppressLint("CheckResult")
    @Test
    fun `A successful init without cipher should follow specific steps`() = runTest(testDispatcher) {
        val result = passPhraseInitUseCase(pin = "0", null)
        assertNotNull(result.get())

        coVerify(exactly = 0) {
            mockEncryptAndSavePassphrase.invoke(any(), any())
        }

        coVerifyOrder {
            mockHashPassphrase.invoke(any(), any())
            mockPepperPassphrase.invoke(any(), any())
            mockCreateAppDatabase(any())
            mockSaltRepository.save(any())
            mockPepperIvRepository.save(any())
        }
    }

    @SuppressLint("CheckResult")
    @Test
    fun `A successful init with cipher should follow specific steps`() = runTest(testDispatcher) {
        val result = passPhraseInitUseCase(pin = "0", mockCipher)
        assertNotNull(result.get())

        coVerifyOrder {
            mockHashPassphrase.invoke(any(), any())
            mockPepperPassphrase.invoke(any(), any())
            mockCreateAppDatabase(any())
            mockSaltRepository.save(any())
            mockPepperIvRepository.save(any())
            mockEncryptAndSavePassphrase(any(), any())
        }
    }

    @SuppressLint("CheckResult")
    @Test
    fun `A successful init should save the results in repositories`() = runTest(testDispatcher) {
        passPhraseInitUseCase(pin = "0", null)

        coVerifyOrder {
            mockSaltRepository.save(hashedData.salt)
            mockPepperIvRepository.save(pepperedData.initializationVector)
        }
    }

    @SuppressLint("CheckResult")
    @Test
    fun `A failed hash should fail the passphrase init`() = runTest(testDispatcher) {
        val error = HashDataError.Unexpected(Exception())
        coEvery { mockHashPassphrase.invoke(any(), any()) } returns Err(error)

        val result = passPhraseInitUseCase(pin = "0", null)
        assertTrue(result.getError() is InitializePassphraseError.Unexpected)

        coVerify(exactly = 0) {
            mockPepperPassphrase.invoke(any(), any())
            mockCreateAppDatabase.invoke(any())
            mockSaltRepository.save(any())
            mockPepperIvRepository.save(any())
            mockEncryptAndSavePassphrase.invoke(any(), any())
        }
    }

    @Test
    fun `A failed passphrase encryption should return an error`() = runTest(testDispatcher) {
        val encryptError = EncryptAndSavePassphraseError.Unexpected(Exception("EncryptionException"))

        coEvery { mockEncryptAndSavePassphrase(any(), any()) } returns Err(encryptError)

        val result = passPhraseInitUseCase(pin = "pin", mockCipher)

        assertTrue(result.getError() is InitializePassphraseError.Unexpected)
        coVerify {
            mockEncryptAndSavePassphrase(any(), any())
        }
    }
}

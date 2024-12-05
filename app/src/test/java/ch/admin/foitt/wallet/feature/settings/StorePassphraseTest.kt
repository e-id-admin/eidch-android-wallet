package ch.admin.foitt.wallet.feature.settings

import android.annotation.SuppressLint
import ch.admin.foitt.wallet.platform.crypto.domain.model.HashDataError
import ch.admin.foitt.wallet.platform.crypto.domain.model.HashedData
import ch.admin.foitt.wallet.platform.passphrase.domain.model.EncryptAndSavePassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.model.PepperPassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.model.PepperedData
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.EncryptAndSavePassphrase
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.HashPassphrase
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.PepperPassphrase
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.StorePassphrase
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.implementation.StorePassphraseImpl
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.get
import com.github.michaelbull.result.getError
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import javax.crypto.Cipher

class StorePassphraseTest {

    @MockK
    private lateinit var mockHashPassphrase: HashPassphrase

    @MockK
    private lateinit var mockPepperPassphrase: PepperPassphrase

    @MockK
    private lateinit var mockEncryptAndSavePassphrase: EncryptAndSavePassphrase

    @MockK
    private lateinit var mockCipher: Cipher

    private val hashedData = HashedData(byteArrayOf(0, 1), byteArrayOf(1, 0))
    private val pepperedData = PepperedData(byteArrayOf(0, 0), byteArrayOf(1, 1))

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var storePassphrase: StorePassphrase

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        storePassphrase = StorePassphraseImpl(
            hashPassphrase = mockHashPassphrase,
            encryptAndSavePassphrase = mockEncryptAndSavePassphrase,
            pepperPassphrase = mockPepperPassphrase,
            ioDispatcher = testDispatcher,
        )

        coEvery { mockHashPassphrase.invoke(any(), any()) } returns Ok(hashedData)
        coEvery { mockEncryptAndSavePassphrase.invoke(any(), any()) } returns Ok(Unit)
        coEvery { mockPepperPassphrase.invoke(any(), any()) } returns Ok(pepperedData)
    }

    @SuppressLint("CheckResult")
    @Test
    fun `Saving the passphrase should follow specific steps`() = runTest(testDispatcher) {
        val result = storePassphrase(pin = "123456", mockCipher)
        assertNotNull(result.get())
        assertNull(result.getError())

        coVerifyOrder {
            mockHashPassphrase.invoke(any(), any())
            mockPepperPassphrase.invoke(any(), any())
            mockEncryptAndSavePassphrase.invoke(any(), any())
        }
    }

    @SuppressLint("CheckResult")
    @Test
    fun `A failed hash should fail the passphrase storing`() = runTest(testDispatcher) {
        val exception = HashDataError.Unexpected(Exception())
        coEvery { mockHashPassphrase.invoke(any(), any()) } returns Err(exception)

        val result = storePassphrase(pin = "123456", mockCipher)
        assertNull(result.get())
        assertNotNull(result.getError())

        coVerify(exactly = 0) {
            mockPepperPassphrase.invoke(any(), any())
        }
    }

    @SuppressLint("CheckResult")
    @Test
    fun `A failed pepper should fail the passphrase storing`() = runTest(testDispatcher) {
        val exception = PepperPassphraseError.Unexpected(Exception())
        coEvery { mockPepperPassphrase.invoke(any(), any()) } returns Err(exception)

        val result = storePassphrase(pin = "123456", mockCipher)
        assertNull(result.get())
        assertNotNull(result.getError())

        coVerify(exactly = 0) {
            mockEncryptAndSavePassphrase.invoke(any(), any())
        }
    }

    @SuppressLint("CheckResult")
    @Test
    fun `A failed passphrase encryption should return an error`() = runTest(testDispatcher) {
        val encryptError = EncryptAndSavePassphraseError.Unexpected(Exception("EncryptionException"))

        coEvery { mockEncryptAndSavePassphrase.invoke(any(), any()) } returns Err(encryptError)

        val result = storePassphrase(pin = "pin", mockCipher)
        assertNull(result.get())
        assertNotNull(result.getError())

        coVerifyOrder {
            mockHashPassphrase.invoke(any(), any())
            mockPepperPassphrase.invoke(any(), any())
            mockEncryptAndSavePassphrase.invoke(any(), any())
        }
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }
}

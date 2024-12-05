package ch.admin.foitt.wallet.platform.authenticateWithPassphrase

import android.annotation.SuppressLint
import ch.admin.foitt.wallet.platform.authenticateWithPassphrase.domain.model.AuthenticateWithPassphraseError
import ch.admin.foitt.wallet.platform.authenticateWithPassphrase.domain.usecase.AuthenticateWithPassphrase
import ch.admin.foitt.wallet.platform.authenticateWithPassphrase.domain.usecase.implementation.AuthenticateWithPassphraseImpl
import ch.admin.foitt.wallet.platform.crypto.domain.model.HashDataError
import ch.admin.foitt.wallet.platform.crypto.domain.model.HashedData
import ch.admin.foitt.wallet.platform.database.domain.model.DatabaseError
import ch.admin.foitt.wallet.platform.database.domain.usecase.CheckDatabasePassphrase
import ch.admin.foitt.wallet.platform.passphrase.domain.model.PepperPassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.model.PepperedData
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.HashPassphrase
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.PepperPassphrase
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetErrorDialogState
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.get
import com.github.michaelbull.result.getError
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
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AuthenticateWithPassphraseTest {

    @MockK
    private lateinit var mockHashPassphrase: HashPassphrase

    @MockK
    private lateinit var mockPepperPassphrase: PepperPassphrase

    @MockK
    private lateinit var mockCheckDatabasePassphrase: CheckDatabasePassphrase

    @MockK
    private lateinit var mockSetErrorDialogState: SetErrorDialogState

    private val hashedData = HashedData(byteArrayOf(0, 1), byteArrayOf(1, 0))
    private val pepperedData = PepperedData(byteArrayOf(0, 0), byteArrayOf(1, 1))

    private lateinit var testedUseCase: AuthenticateWithPassphrase

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery { mockHashPassphrase(any(), any()) } returns Ok(hashedData)
        coEvery { mockPepperPassphrase(any(), any()) } returns Ok(pepperedData)
        coEvery { mockCheckDatabasePassphrase(any()) } returns Ok(Unit)
        coEvery { mockSetErrorDialogState.invoke(any()) } just runs

        testedUseCase = AuthenticateWithPassphraseImpl(
            hashPassphrase = mockHashPassphrase,
            pepperPassphrase = mockPepperPassphrase,
            checkDatabasePassphrase = mockCheckDatabasePassphrase,
            setErrorDialogState = mockSetErrorDialogState
        )
    }

    @SuppressLint("CheckResult")
    @Test
    fun `A successful auth with pin call follows specific steps`() = runTest {
        val result = testedUseCase(passphrase = "123")

        assertNotNull(result.get())
        assertNull(result.getError())

        coVerify(ordering = Ordering.ORDERED) {
            mockHashPassphrase.invoke(any(), any())
            mockPepperPassphrase.invoke(any(), any())
            mockCheckDatabasePassphrase.invoke(any())
        }
    }

    @Test
    fun `A failed hash fails the check and shows an error`() = runTest {
        coEvery { mockHashPassphrase(any(), any()) } returns Err(HashDataError.Unexpected(Exception()))

        val result = testedUseCase(passphrase = "123")

        assertTrue(result.getError() is AuthenticateWithPassphraseError.Unexpected)
        coVerify(exactly = 1) {
            mockSetErrorDialogState.invoke(any())
        }
    }

    @Test
    fun `A failed peppering should fail the check and shows an error`() = runTest {
        coEvery { mockPepperPassphrase(any(), any()) } returns Err(PepperPassphraseError.Unexpected(Exception()))

        val result = testedUseCase(passphrase = "123")

        assertTrue(result.getError() is AuthenticateWithPassphraseError.Unexpected)
        coVerify(exactly = 1) {
            mockSetErrorDialogState.invoke(any())
        }
    }

    @Test
    fun `A wrong passphrase should fail the check and return an error`() = runTest {
        coEvery { mockCheckDatabasePassphrase(any()) } returns Err(DatabaseError.WrongPassphrase(Exception()))

        val result = testedUseCase(passphrase = "123")

        assertTrue(result.getError() is AuthenticateWithPassphraseError.InvalidPassphrase)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }
}

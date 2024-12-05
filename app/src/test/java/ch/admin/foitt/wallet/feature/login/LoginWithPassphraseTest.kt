package ch.admin.foitt.wallet.feature.login

import android.annotation.SuppressLint
import ch.admin.foitt.wallet.platform.crypto.domain.model.HashDataError
import ch.admin.foitt.wallet.platform.crypto.domain.model.HashedData
import ch.admin.foitt.wallet.platform.database.domain.model.DatabaseError
import ch.admin.foitt.wallet.platform.database.domain.usecase.OpenAppDatabase
import ch.admin.foitt.wallet.platform.login.domain.model.LoginError
import ch.admin.foitt.wallet.platform.login.domain.usecase.implementation.LoginWithPassphraseImpl
import ch.admin.foitt.wallet.platform.passphrase.domain.model.PepperPassphraseError
import ch.admin.foitt.wallet.platform.passphrase.domain.model.PepperedData
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.HashPassphrase
import ch.admin.foitt.wallet.platform.passphrase.domain.usecase.PepperPassphrase
import ch.admin.foitt.wallet.platform.userInteraction.domain.usecase.UserInteraction
import ch.admin.foitt.wallet.util.assertErrorType
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.get
import io.mockk.MockKAnnotations
import io.mockk.Ordering
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class LoginWithPassphraseTest {
    @MockK
    private lateinit var mockOpenAppDatabase: OpenAppDatabase

    @MockK
    private lateinit var mockPepperPassphrase: PepperPassphrase

    @MockK
    private lateinit var mockHashPassphrase: HashPassphrase

    @MockK
    private lateinit var mockUserInteraction: UserInteraction

    private lateinit var useCase: LoginWithPassphraseImpl

    private val hashedData = HashedData(byteArrayOf(0, 1), byteArrayOf(1, 0))
    private val pepperedData = PepperedData(byteArrayOf(0, 0), byteArrayOf(1, 1))

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        coEvery { mockOpenAppDatabase(any()) } returns Ok(Unit)
        coEvery { mockHashPassphrase(any(), any()) } returns Ok(hashedData)
        coEvery { mockPepperPassphrase(any(), any()) } returns Ok(pepperedData)
        coEvery { mockUserInteraction() } just Runs

        useCase = LoginWithPassphraseImpl(
            hashPassphrase = mockHashPassphrase,
            openAppDatabase = mockOpenAppDatabase,
            pepperPassphrase = mockPepperPassphrase,
            userInteraction = mockUserInteraction,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @SuppressLint("CheckResult")
    @Test
    fun `A Successful CheckPin call specific steps`() = runTest {
        val result = useCase("x")

        assertNotNull(result.get())
        coVerify(ordering = Ordering.ORDERED) {
            mockHashPassphrase.invoke(any(), any())
            mockPepperPassphrase.invoke(any(), any())
            mockOpenAppDatabase.invoke(any())
        }
    }

    @Test
    fun `A failed hash should fail the pin check and return an unexpected error`() = runTest {
        coEvery { mockHashPassphrase(any(), any()) } returns Err(HashDataError.Unexpected(Exception()))

        val result = useCase("x")

        result.assertErrorType(LoginError.Unexpected::class)
    }

    @Test
    fun `A failed peppering should fail the pin check and return an unexpected error`() = runTest {
        val exception = Exception("pepper failed")
        coEvery { mockPepperPassphrase(any(), any()) } returns Err(PepperPassphraseError.Unexpected(exception))

        val result = useCase("x")

        result.assertErrorType(LoginError.Unexpected::class)
    }

    @Test
    fun `A wrong sqlcipher passphrase should fail the pin check and return an invalid passphrase error`() = runTest {
        coEvery { mockOpenAppDatabase(any()) } returns Err(DatabaseError.WrongPassphrase(Exception()))

        val result = useCase("x")

        result.assertErrorType(LoginError.InvalidPassphrase::class)
    }

    @Test
    fun `An SQLite exception should fail the pin check and return an unexpected error`() = runTest {
        coEvery { mockOpenAppDatabase(any()) } returns Err(DatabaseError.SetupFailed(Exception()))

        val result = useCase("x")

        result.assertErrorType(LoginError.Unexpected::class)
    }
}

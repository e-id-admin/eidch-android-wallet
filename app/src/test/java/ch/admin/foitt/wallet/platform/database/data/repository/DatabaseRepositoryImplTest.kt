package ch.admin.foitt.wallet.platform.database.data.repository

import ch.admin.foitt.wallet.platform.database.data.AppDatabase
import ch.admin.foitt.wallet.platform.database.data.DatabaseInitializer
import ch.admin.foitt.wallet.platform.database.data.DatabaseWrapper
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DatabaseRepositoryImplTest {
    private val testScheduler = TestCoroutineScheduler()
    private val testDispatcher = UnconfinedTestDispatcher(testScheduler)
    private val testScope = TestScope(testDispatcher)

    @MockK
    private lateinit var mockDatabaseInitializer: DatabaseInitializer

    @MockK
    private lateinit var mockAppDatabase: AppDatabase

    private lateinit var databaseWrapper: DatabaseWrapper

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        databaseWrapper = DatabaseWrapper(
            coroutineScope = testScope.backgroundScope,
            ioDispatcher = testDispatcher,
            databaseInitializer = mockDatabaseInitializer,
        )

        success()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `creating database does not lock mutex twice`() = testScope.runTest {
        databaseWrapper.mutexOwner = this

        val result = databaseWrapper.createDatabase(ByteArray(0))

        result.assertOk()
    }

    @Test
    fun `closing database does not lock mutex twice`() = testScope.runTest {
        databaseWrapper.mutexOwner = this

        databaseWrapper.close()
    }

    @Test
    fun `opening database does not lock mutex twice`() = testScope.runTest {
        databaseWrapper.mutexOwner = this

        val result = databaseWrapper.open(ByteArray(0))

        result.assertOk()
    }

    @Test
    fun `checking if correct passphrase was given does not lock mutex twice`() = testScope.runTest {
        databaseWrapper.mutexOwner = this

        val result = databaseWrapper.checkIfCorrectPassphrase(ByteArray(0))

        result.assertOk()
    }

    @Test
    fun `changing passphrase does not lock mutex twice`() = testScope.runTest {
        databaseWrapper.mutexOwner = this

        databaseWrapper.open(ByteArray(0)).assertOk()

        val result = databaseWrapper.changePassphrase(ByteArray(0))

        result.assertOk()
    }

    private fun success() {
        every { mockDatabaseInitializer.create(any()) } returns Ok(mockAppDatabase)
        coEvery { mockAppDatabase.tryDecrypt() } returns Ok(Unit)
        coEvery { mockAppDatabase.close() } just runs
        coEvery { mockAppDatabase.changePassword(any()) } returns Ok(Unit)
        coEvery { mockAppDatabase.isOpen } returns true

        mockDaos()
    }

    private fun mockDaos() {
        every { mockAppDatabase.credentialDao() } returns mockk()
        every { mockAppDatabase.credentialDisplayDao() } returns mockk()
        every { mockAppDatabase.credentialClaimDao() } returns mockk()
        every { mockAppDatabase.credentialClaimDisplayDao() } returns mockk()
        every { mockAppDatabase.credentialIssuerDisplayDao() } returns mockk()
        every { mockAppDatabase.credentialWithDetailsDao() } returns mockk()
        every { mockAppDatabase.credentialWithDisplaysAndClaimsDao() } returns mockk()
        every { mockAppDatabase.credentialWithDisplaysDao() } returns mockk()
        every { mockAppDatabase.credentialWithIssuerAndDisplaysDao() } returns mockk()
        every { mockAppDatabase.eIdRequestCaseDao() } returns mockk()
        every { mockAppDatabase.eIdRequestStateDao() } returns mockk()
    }
}

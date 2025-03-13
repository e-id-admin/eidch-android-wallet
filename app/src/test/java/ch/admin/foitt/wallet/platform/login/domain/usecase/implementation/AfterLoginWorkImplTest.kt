package ch.admin.foitt.wallet.platform.login.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.UpdateAllCredentialStatuses
import ch.admin.foitt.wallet.platform.database.domain.model.DatabaseState
import ch.admin.foitt.wallet.platform.database.domain.repository.DatabaseRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.UpdateAllSIdStatuses
import ch.admin.foitt.wallet.platform.login.domain.usecase.AfterLoginWork
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.runs
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AfterLoginWorkImplTest {
    @MockK
    private lateinit var mockDatabaseRepository: DatabaseRepository

    @MockK
    private lateinit var mockUpdateAllCredentialStatuses: UpdateAllCredentialStatuses

    @MockK
    private lateinit var mockUpdateAllSIdStatuses: UpdateAllSIdStatuses

    private lateinit var stateFlow: MutableStateFlow<DatabaseState>

    private lateinit var useCase: AfterLoginWork

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery { mockUpdateAllCredentialStatuses() } just runs
        coEvery { mockUpdateAllSIdStatuses() } just runs

        useCase = AfterLoginWorkImpl(
            databaseRepository = mockDatabaseRepository,
            updateAllCredentialStatuses = mockUpdateAllCredentialStatuses,
            updateAllSIdStatuses = mockUpdateAllSIdStatuses
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `AfterLoginWork updates the status of all credentials and EId when the DB state changes to open`() = runTest {
        // initially closed
        stateFlow = MutableStateFlow(DatabaseState.CLOSED)

        coEvery { mockDatabaseRepository.databaseState } returns stateFlow

        // because of the hot StateFlow we need to cancel the collection after some time
        val job = launch {
            useCase()
        }

        // change to open (= login is done)
        stateFlow.value = DatabaseState.OPEN

        advanceUntilIdle()
        job.cancel()

        coVerify(exactly = 1) {
            mockUpdateAllCredentialStatuses()
            mockUpdateAllSIdStatuses()
        }
    }

    @Test
    fun `AfterLoginWork does not update the status of all credential and EId when the DB state changes to closed`() = runTest {
        // initially open
        stateFlow = MutableStateFlow(DatabaseState.OPEN)

        coEvery { mockDatabaseRepository.databaseState } returns stateFlow

        // because of the hot StateFlow we need to cancel the collection after some time
        val job = launch {
            useCase()
        }

        // change to closed
        stateFlow.value = DatabaseState.CLOSED

        advanceUntilIdle()
        job.cancel()

        coVerify(exactly = 0) {
            mockUpdateAllCredentialStatuses()
            mockUpdateAllSIdStatuses()
        }
    }
}

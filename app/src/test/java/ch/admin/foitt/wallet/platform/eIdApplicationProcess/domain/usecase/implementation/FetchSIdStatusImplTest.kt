package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestQueueState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.StateResponse
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.SIdRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.FetchSIdStatus
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FetchSIdStatusImplTest {

    @MockK
    private lateinit var mockEIdRepository: SIdRepository

    lateinit var fetchSIdStatus: FetchSIdStatus

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        fetchSIdStatus = FetchSIdStatusImpl(mockEIdRepository)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Successfully fetch an eID status`() = runTest {
        val stateResponse = StateResponse(
            state = EIdRequestQueueState.IN_QUEUING,
            queueInformation = null,
            legalRepresentant = null,
            onlineSessionStartTimeout = null
        )

        coEvery { mockEIdRepository.fetchSIdState("caseID") } returns Ok(stateResponse)

        val response = fetchSIdStatus("caseID").assertOk()

        assertEquals(EIdRequestQueueState.IN_QUEUING, response.state)
    }

    @Test
    fun `Unsuccessfully fetch an eID status`() = runTest {
        coEvery { mockEIdRepository.fetchSIdState("caseID") } returns Err(EIdRequestError.Unexpected(IllegalStateException()))

        fetchSIdStatus("caseID").assertErrorType(EIdRequestError.Unexpected::class)
    }
}

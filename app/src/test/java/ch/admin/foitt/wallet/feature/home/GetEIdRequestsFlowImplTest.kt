package ch.admin.foitt.wallet.feature.home

import ch.admin.foitt.wallet.feature.home.domain.model.HomeError
import ch.admin.foitt.wallet.feature.home.domain.usecase.GetEIdRequestsFlow
import ch.admin.foitt.wallet.feature.home.domain.usecase.implementation.GetEIdRequestsFlowImpl
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestCaseWithStateRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation.mock.EIdRequestMocks.eIdRequestCaseWithState
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetEIdRequestsFlowImplTest {

    @MockK
    private lateinit var mockEIdRequestCaseWithStateRepository: EIdRequestCaseWithStateRepository

    lateinit var getEIdRequestsFlow: GetEIdRequestsFlow

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        getEIdRequestsFlow = GetEIdRequestsFlowImpl(mockEIdRequestCaseWithStateRepository)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Successfully getting the eId requests from the db returns an Ok`() = runTest {
        coEvery {
            mockEIdRequestCaseWithStateRepository.getEIdRequestCasesWithStatesFlow()
        } returns flow { emit(Ok(listOf(eIdRequestCaseWithState))) }

        val result = getEIdRequestsFlow().firstOrNull()

        assertNotNull(result)
        result?.assertOk()
    }

    @Test
    fun `Getting the eId requests maps error from the repo`() = runTest {
        val exception = IllegalStateException("error in db")
        coEvery {
            mockEIdRequestCaseWithStateRepository.getEIdRequestCasesWithStatesFlow()
        } returns flow { emit(Err(EIdRequestError.Unexpected(exception))) }

        val result = getEIdRequestsFlow().firstOrNull()

        assertNotNull(result)
        result?.assertErrorType(HomeError.Unexpected::class)
    }
}

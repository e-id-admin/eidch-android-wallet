package ch.admin.foitt.wallet.feature.home

import ch.admin.foitt.wallet.feature.home.domain.model.HomeError
import ch.admin.foitt.wallet.feature.home.domain.usecase.DeleteEIdRequestCase
import ch.admin.foitt.wallet.feature.home.domain.usecase.implementation.DeleteEIdRequestCaseImpl
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestCaseWithState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestCaseWithStateRepositoryError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestCaseRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestCaseWithStateRepository
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DeleteEIdRequestCaseImplTest {

    @MockK
    private lateinit var mockEIdRequestCaseRepository: EIdRequestCaseRepository

    private var eIdRequestCasesWithStatesFlow =
        MutableStateFlow<Result<List<EIdRequestCaseWithState>, EIdRequestCaseWithStateRepositoryError>>(Ok(emptyList()))

    @MockK
    private lateinit var mockEIdRequestCaseWithStateRepository: EIdRequestCaseWithStateRepository

    lateinit var deleteEIdRequestCase: DeleteEIdRequestCase

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        deleteEIdRequestCase = DeleteEIdRequestCaseImpl(mockEIdRequestCaseRepository)

        coEvery {
            mockEIdRequestCaseWithStateRepository.getEIdRequestCasesWithStatesFlow()
        } returns eIdRequestCasesWithStatesFlow
    }

    @Test
    fun `Successful delete returns ok(unit)`() = runTest {
        coEvery {
            mockEIdRequestCaseRepository.deleteEIdRequestCase("caseId")
        } returns Ok(Unit)

        deleteEIdRequestCase("caseId").assertOk()
    }

    @Test
    fun `Failed delete returns an error`() = runTest {
        val exception = IllegalStateException("error deleting entry")

        coEvery {
            mockEIdRequestCaseRepository.deleteEIdRequestCase("caseId")
        } returns Err(EIdRequestError.Unexpected(exception))

        deleteEIdRequestCase("caseId").assertErrorType(HomeError.Unexpected::class)
    }
}

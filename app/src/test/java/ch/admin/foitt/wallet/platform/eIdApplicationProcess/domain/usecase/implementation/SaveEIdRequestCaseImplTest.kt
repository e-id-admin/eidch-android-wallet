package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestCase
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestCaseRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.SaveEIdRequestCase
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SaveEIdRequestCaseImplTest {

    @MockK
    private lateinit var mockEIdRequestCase: EIdRequestCase

    @MockK
    private lateinit var mockEIdRequestCaseRepository: EIdRequestCaseRepository

    lateinit var saveEIdRequestCase: SaveEIdRequestCase

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        saveEIdRequestCase = SaveEIdRequestCaseImpl(mockEIdRequestCaseRepository)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Successfully saving an eID request case returns an Ok`() = runTest {
        coEvery { mockEIdRequestCaseRepository.saveEIdRequestCase(mockEIdRequestCase) } returns Ok(Unit)

        saveEIdRequestCase(mockEIdRequestCase).assertOk()
    }

    @Test
    fun `Saving an eID request case maps error from the repo`() = runTest {
        val exception = IllegalStateException("error in db")
        coEvery {
            mockEIdRequestCaseRepository.saveEIdRequestCase(mockEIdRequestCase)
        } returns Err(EIdRequestError.Unexpected(exception))

        saveEIdRequestCase(mockEIdRequestCase).assertErrorType(EIdRequestError.Unexpected::class)
    }
}

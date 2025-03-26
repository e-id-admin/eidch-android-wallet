package ch.admin.foitt.wallet.feature.mrzScan

import ch.admin.foitt.wallet.feature.mrzScan.domain.model.MrzScanError
import ch.admin.foitt.wallet.feature.mrzScan.domain.usecase.SaveEIdRequestState
import ch.admin.foitt.wallet.feature.mrzScan.domain.usecase.implementation.SaveEIdRequestStateImpl
import ch.admin.foitt.wallet.platform.database.domain.model.EIdRequestState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestStateRepository
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

class SaveEIdRequestStateImplTest {
    @MockK
    private lateinit var mockEIdRequestState: EIdRequestState

    @MockK
    private lateinit var mockEIdRequestStateRepository: EIdRequestStateRepository

    lateinit var saveEIdRequestState: SaveEIdRequestState

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        saveEIdRequestState = SaveEIdRequestStateImpl(mockEIdRequestStateRepository)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Successfully saving an eID request state returns an Ok`() = runTest {
        coEvery { mockEIdRequestStateRepository.saveEIdRequestState(mockEIdRequestState) } returns Ok(REQUEST_STATE_ID)

        saveEIdRequestState(mockEIdRequestState).assertOk()
    }

    @Test
    fun `Saving an eID request case maps error from the repo`() = runTest {
        val exception = IllegalStateException("error in db")
        coEvery {
            mockEIdRequestStateRepository.saveEIdRequestState(mockEIdRequestState)
        } returns Err(EIdRequestError.Unexpected(exception))

        saveEIdRequestState(mockEIdRequestState).assertErrorType(MrzScanError.Unexpected::class)
    }

    private companion object {
        const val REQUEST_STATE_ID = 1L
    }
}

package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.ResolveDidError
import ch.admin.foitt.openid4vc.domain.repository.FetchDidLogRepository
import ch.admin.foitt.openid4vc.domain.usecase.ResolveDid
import ch.admin.foitt.openid4vc.util.assertErrorType
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URL

class ResolveDidImplTest {

    @MockK
    private lateinit var mockFetchDidLogRepository: FetchDidLogRepository

    private lateinit var useCase: ResolveDid

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = ResolveDidImpl(
            repo = mockFetchDidLogRepository,
        )

        coEvery { mockFetchDidLogRepository.fetchDidLog(any<URL>()) } returns Ok("")
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `failure to use did-resolver returns ResolveDidError_Unexpected`() = runTest {
        useCase("didString").assertErrorType(ResolveDidError.Unexpected::class)
    }
}

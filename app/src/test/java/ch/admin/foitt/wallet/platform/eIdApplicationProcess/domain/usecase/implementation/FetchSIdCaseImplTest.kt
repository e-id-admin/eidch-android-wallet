package ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.ApplyRequest
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.CaseResponse
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.IdentityType
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.SIdRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.FetchSIdCase
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

class FetchSIdCaseImplTest {

    @MockK
    private lateinit var mockEIdRepository: SIdRepository

    lateinit var fetchSIdCase: FetchSIdCase
    lateinit var applyRequest: ApplyRequest

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        fetchSIdCase = FetchSIdCaseImpl(mockEIdRepository)

        applyRequest = ApplyRequest(
            mrz = listOf("ID<<<I7A<<<<<<7<<<<<<<<<<<<<<<", "1001015X3012316<<<<<<<<<<<<<<2", "MINDERJAEHRIGE<<ANNETTE<<<<<<<"),
            legalRepresentant = false,
            email = null
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Successfully fetch an eID case`() = runTest {
        val caseResponse = CaseResponse(
            caseId = "1234-5678-abcd-1234567890ABCDE",
            surname = "Muster",
            givenNames = "Max Felix",
            dateOfBirth = "1989-08-24T00:00:00Z",
            identityType = IdentityType.SWISS_PASS,
            identityNumber = "A123456789",
            validUntil = "2028-12-23T00:00:00Z",
            legalRepresentant = false,
            email = "user@examle.com"
        )

        coEvery { mockEIdRepository.fetchSIdCase(applyRequest) } returns Ok(caseResponse)

        var response = fetchSIdCase(applyRequest).assertOk()

        assertEquals("1234-5678-abcd-1234567890ABCDE", response.caseId)
        assertEquals("Muster", response.surname)
        assertEquals(false, response.legalRepresentant)
    }

    @Test
    fun `Unsuccessfully fetch an eID case`() = runTest {
        coEvery {
            mockEIdRepository.fetchSIdCase(applyRequest)
        } returns Err(EIdRequestError.Unexpected(IllegalStateException()))

        fetchSIdCase(applyRequest).assertErrorType(EIdRequestError.Unexpected::class)
    }
}

package ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimDisplay
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimData
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.GetCredentialClaimData
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.GetCredentialClaimDisplays
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.MapToCredentialClaimData
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetCredentialClaimDataImplTest {

    @MockK
    private lateinit var mockGetCredentialClaimDisplays: GetCredentialClaimDisplays

    @MockK
    private lateinit var mockMapToCredentialClaimData: MapToCredentialClaimData

    @MockK
    private lateinit var mockCredentialClaim: CredentialClaim

    @MockK
    private lateinit var mockCredentialClaimData: CredentialClaimData

    private val mockCredentialClaimDisplays = emptyList<CredentialClaimDisplay>()

    private lateinit var useCase: GetCredentialClaimData

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        every { mockCredentialClaim.id } returns CREDENTIAL_CLAIM_ID

        coEvery { mockGetCredentialClaimDisplays(CREDENTIAL_CLAIM_ID) } returns Ok(mockCredentialClaimDisplays)
        coEvery {
            mockMapToCredentialClaimData(mockCredentialClaim, mockCredentialClaimDisplays)
        } returns Ok(mockCredentialClaimData)

        useCase = GetCredentialClaimDataImpl(
            getCredentialClaimDisplays = mockGetCredentialClaimDisplays,
            mapToCredentialClaimData = mockMapToCredentialClaimData,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `GetCredentialClaimData runs specific steps`() = runTest {
        useCase(mockCredentialClaim).assertOk()

        coVerify {
            mockGetCredentialClaimDisplays(CREDENTIAL_CLAIM_ID)
            mockMapToCredentialClaimData(mockCredentialClaim, mockCredentialClaimDisplays)
        }
    }

    @Test
    fun `GetCredentialClaimData maps errors from getCredentialClaimDisplays`() = runTest {
        coEvery { mockGetCredentialClaimDisplays(CREDENTIAL_CLAIM_ID) } returns Err(SsiError.Unexpected(Exception()))

        useCase(mockCredentialClaim).assertErrorType(SsiError.Unexpected::class)
    }

    @Test
    fun `GetCredentialClaimData maps errors from mapToCredentialClaimData`() = runTest {
        coEvery {
            mockMapToCredentialClaimData(mockCredentialClaim, mockCredentialClaimDisplays)
        } returns Err(SsiError.Unexpected(Exception()))

        useCase(mockCredentialClaim).assertErrorType(SsiError.Unexpected::class)
    }

    private companion object {
        const val CREDENTIAL_CLAIM_ID = 1L
    }
}

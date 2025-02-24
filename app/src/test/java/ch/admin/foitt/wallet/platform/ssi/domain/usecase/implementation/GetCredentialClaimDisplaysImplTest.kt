package ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimDisplay
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialClaimDisplayRepo
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.GetCredentialClaimDisplays
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

class GetCredentialClaimDisplaysImplTest {

    @MockK
    private lateinit var mockCredentialClaimDisplayRepo: CredentialClaimDisplayRepo

    private val mockCredentialClaimDisplays = emptyList<CredentialClaimDisplay>()

    private lateinit var useCase: GetCredentialClaimDisplays

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery { mockCredentialClaimDisplayRepo.getByClaimId(CREDENTIAL_CLAIM_ID) } returns Ok(mockCredentialClaimDisplays)

        useCase = GetCredentialClaimDisplaysImpl(
            credentialClaimDisplayRepo = mockCredentialClaimDisplayRepo,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `GetCredentialClaimDisplays returns a list of credential claim displays`() = runTest {
        val result = useCase(CREDENTIAL_CLAIM_ID).assertOk()

        assertEquals(mockCredentialClaimDisplays, result)
    }

    @Test
    fun `GetCredentialClaimDisplays maps errors from the repository`() = runTest {
        coEvery {
            mockCredentialClaimDisplayRepo.getByClaimId(CREDENTIAL_CLAIM_ID)
        } returns Err(SsiError.Unexpected(Exception()))

        useCase(CREDENTIAL_CLAIM_ID).assertErrorType(SsiError.Unexpected::class)
    }

    private companion object {
        const val CREDENTIAL_CLAIM_ID = 1L
    }
}

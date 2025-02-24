package ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialClaimRepo
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.GetCredentialClaims
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

class GetCredentialClaimsImplTest {

    @MockK
    private lateinit var mockCredentialClaimRepo: CredentialClaimRepo

    private val mockCredentialClaims = emptyList<CredentialClaim>()

    private lateinit var useCase: GetCredentialClaims

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery { mockCredentialClaimRepo.getByCredentialId(CREDENTIAL_ID) } returns Ok(mockCredentialClaims)

        useCase = GetCredentialClaimsImpl(
            credentialClaimRepo = mockCredentialClaimRepo,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `GetCredentialClaims returns a list of credential claims`() = runTest {
        val result = useCase(CREDENTIAL_ID).assertOk()

        assertEquals(mockCredentialClaims, result)
    }

    @Test
    fun `GetCredentialClaimDisplays maps errors from the repository`() = runTest {
        coEvery {
            mockCredentialClaimRepo.getByCredentialId(CREDENTIAL_ID)
        } returns Err(SsiError.Unexpected(Exception()))

        useCase(CREDENTIAL_ID).assertErrorType(SsiError.Unexpected::class)
    }

    private companion object {
        const val CREDENTIAL_ID = 1L
    }
}

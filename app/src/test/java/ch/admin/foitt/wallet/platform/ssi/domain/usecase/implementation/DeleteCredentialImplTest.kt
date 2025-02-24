package ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialRepo
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.DeleteCredential
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DeleteCredentialImplTest {

    @MockK
    private lateinit var mockCredentialRepository: CredentialRepo

    @MockK
    private lateinit var mockCredential: Credential

    private lateinit var useCase: DeleteCredential

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery { mockCredential.keyBindingIdentifier } returns PRIVATE_KEY_IDENTIFIER
        coEvery { mockCredentialRepository.getById(CREDENTIAL_ID) } returns Ok(mockCredential)
        coEvery { mockCredentialRepository.deleteById(CREDENTIAL_ID) } returns Ok(Unit)

        useCase = DeleteCredentialImpl(
            credentialRepo = mockCredentialRepository
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Deleting a credential succeeds and runs specific steps`() = runTest {
        useCase(CREDENTIAL_ID).assertOk()

        coVerify(exactly = 1) {
            mockCredentialRepository.getById(CREDENTIAL_ID)
            mockCredentialRepository.deleteById(CREDENTIAL_ID)
        }
    }

    @Test
    fun `Deletion is not attempted if the returned credential is already null`() = runTest {
        coEvery { mockCredentialRepository.getById(CREDENTIAL_ID) } returns Ok(null)

        useCase(CREDENTIAL_ID).assertOk()

        coVerify(exactly = 0) {
            mockCredentialRepository.deleteById(CREDENTIAL_ID)
        }
    }

    @Test
    fun `Deleting a credential maps errors from getting the credential`() = runTest {
        coEvery { mockCredentialRepository.getById(CREDENTIAL_ID) } returns Err(SsiError.Unexpected(Exception()))

        useCase(CREDENTIAL_ID).assertErrorType(SsiError.Unexpected::class)
    }

    @Test
    fun `Deleting a credential maps errors from deleting the credential`() = runTest {
        coEvery { mockCredentialRepository.deleteById(CREDENTIAL_ID) } returns Err(SsiError.Unexpected(Exception()))

        useCase(CREDENTIAL_ID).assertErrorType(SsiError.Unexpected::class)
    }

    private companion object {
        const val CREDENTIAL_ID = 1L
        const val PRIVATE_KEY_IDENTIFIER = "privateKeyIdentifier"
    }
}

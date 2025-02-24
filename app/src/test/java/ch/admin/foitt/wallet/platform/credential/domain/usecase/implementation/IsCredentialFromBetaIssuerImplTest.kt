package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GetAnyCredential
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockCredential.vcSdJwtCredentialBeta
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockCredential.vcSdJwtCredentialProd
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class IsCredentialFromBetaIssuerImplTest {

    @MockK
    private lateinit var mockGetAnyCredential: GetAnyCredential

    private lateinit var useCase: IsCredentialFromBetaIssuerImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = IsCredentialFromBetaIssuerImpl(mockGetAnyCredential)
    }

    @Test
    fun `Getting a PROD payload should return false`() = runTest {
        coEvery { mockGetAnyCredential(any()) } returns Ok(vcSdJwtCredentialProd)
        val result = useCase(credentialId = 1L)

        assertEquals(false, result)
    }

    @Test
    fun `Getting a BETA payload should return true`() = runTest {
        coEvery { mockGetAnyCredential(any()) } returns Ok(vcSdJwtCredentialBeta)
        val result = useCase(credentialId = 1L)

        assertEquals(true, result)
    }

    @Test
    fun `Getting a null AnyCredential returns false`() = runTest {
        coEvery { mockGetAnyCredential(any()) } returns Ok(null)
        val result = useCase(credentialId = 1L)

        assertEquals(false, result)
    }

    @Test
    fun `Getting an error from AnyCredential returns false`() = runTest {
        coEvery { mockGetAnyCredential(any()) } returns Err(CredentialError.Unexpected(Exception()))
        val result = useCase(credentialId = 1L)

        assertEquals(false, result)
    }
}

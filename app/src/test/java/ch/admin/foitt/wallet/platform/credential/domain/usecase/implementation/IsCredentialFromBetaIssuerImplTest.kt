package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.credential.domain.usecase.GetAnyCredential
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockCredential
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
        coEvery { mockGetAnyCredential(any()) } returns Ok(MockCredential.vcSdjCredentialProd)
        val result = useCase(credentialId = 1L)

        assertEquals(false, result)
    }

    @Test
    fun `Getting a BETA payload should return true`() = runTest {
        coEvery { mockGetAnyCredential(any()) } returns Ok(MockCredential.vcSdjCredentialBeta)
        val result = useCase(credentialId = 1L)

        assertEquals(true, result)
    }

    @Test
    fun `Getting an empty payload should return false`() = runTest {
        coEvery { mockGetAnyCredential(any()) } returns Ok(MockCredential.vcSdjCredentialEmptyPayload)
        val result = useCase(credentialId = 1L)

        assertEquals(false, result)
    }
}
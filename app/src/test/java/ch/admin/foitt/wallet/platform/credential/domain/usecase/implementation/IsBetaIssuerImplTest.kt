package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockCredential.vcSdJwtCredentialBeta
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockCredential.vcSdJwtCredentialProd
import io.mockk.MockKAnnotations
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class IsBetaIssuerImplTest {

    private lateinit var useCase: IsBetaIssuerImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = IsBetaIssuerImpl()
    }

    @Test
    fun `Getting a PROD payload should return false`() = runTest {
        val result = useCase(vcSdJwtCredentialProd.issuer)

        assertEquals(false, result)
    }

    @Test
    fun `Getting a BETA payload should return true`() = runTest {
        val result = useCase(vcSdJwtCredentialBeta.issuer)

        assertEquals(true, result)
    }

    @Test
    fun `Passing a null issuer returns false`() = runTest {
        assertFalse(useCase(null))
    }
}

package ch.admin.foitt.wallet.platform.trustRegistry

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustRegistryErrors
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.FetchAnyCredentialTrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.FetchTrustStatementFromDid
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation.FetchAnyCredentialTrustStatementImpl
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FetchAnyCredentialTrustStatementImpTest {

    @MockK
    private lateinit var mockFetchTrustStatementFromDid: FetchTrustStatementFromDid

    private lateinit var useCase: FetchAnyCredentialTrustStatement

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        useCase = FetchAnyCredentialTrustStatementImpl(
            mockFetchTrustStatementFromDid,
        )
        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Successfully fetching a trust statement returns that trust statement`() = runTest {
        val result = useCase(mockAnyCredential).assertOk()

        assertEquals(mockTrustStatement, result)
    }

    @Test
    fun `An unknown credential format returns an error`(): Unit = runTest {
        coEvery { mockAnyCredential.format } returns CredentialFormat.UNKNOWN

        useCase(mockAnyCredential).assertErrorType(TrustRegistryErrors.Unexpected::class)
    }

    @Test
    fun `A faulty credential payload returns an error`(): Unit = runTest {
        coEvery { mockAnyCredential.payload } returns "a"

        useCase(mockAnyCredential).assertErrorType(TrustRegistryErrors.Unexpected::class)
    }

    private fun setupDefaultMocks() {
        coEvery { mockFetchTrustStatementFromDid(mockDid) } returns Ok(mockTrustStatement)
        coEvery { mockAnyCredential.payload } returns mockPayload
        coEvery { mockAnyCredential.format } returns CredentialFormat.VC_SD_JWT
    }

    private val mockAnyCredential = mockk<AnyCredential>()
    private val mockTrustStatement = mockk<TrustStatement>()
    private val mockDid = "did:tdw:identifier"

    /*
    {
        "iss":"did:tdw:identifier"
    }
     */
    private val mockPayload = "ewogICJ0eXAiOiJ2YytzZC1qd3QiLAogICJhbGciOiJFUzI1NiIKfQ.ewogICJpc3MiOiJkaWQ6dGR3OmlkZW50aWZpZXIiCn0.ZXdvZ0lDSjBlWEFpT2lKMll5dHpaQzFxZDNRaUxBb2dJQ0poYkdjaU9pSkZVekkxTmlJS2ZRLi5MNG13YUg1aV9KeXA1cVJtamc3VE9aSHM2VjlSa3E2TEVLTi1fRzFXTzlCazhHRjVXSjdzdFlpbGxNZjZWVGxtT1Fhd1prR21rMWdlNmFEX2FiNWg2QQ"
}

package ch.admin.foitt.wallet.platform.trustRegistry

import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ProtectedIssuanceTrustListStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustRegistryError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.repository.TrustStatementRepository
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.GetTrustDomainFromDid
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ProcessProtectedIssuanceTrustListStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateProtectedIssuanceTrustListStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation.ProcessProtectedIssuanceTrustListStatementImpl
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ProcessProtectedIssuanceTrustListStatementImplTest {
    @MockK
    private lateinit var mockGetTrustDomainFromDid: GetTrustDomainFromDid

    @MockK
    private lateinit var mockTrustStatementRepository: TrustStatementRepository

    @MockK
    private lateinit var mockValidateProtectedIssuanceTrustListStatement: ValidateProtectedIssuanceTrustListStatement

    @MockK
    private lateinit var mockProtectedIssuanceTrustListStatementJwt: Jwt

    @MockK
    private lateinit var mockProtectedIssuanceTrustListStatement: ProtectedIssuanceTrustListStatement

    private lateinit var useCase: ProcessProtectedIssuanceTrustListStatement

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        useCase = ProcessProtectedIssuanceTrustListStatementImpl(
            getTrustDomainFromDid = mockGetTrustDomainFromDid,
            trustStatementRepository = mockTrustStatementRepository,
            validateProtectedIssuanceTrustListStatement = mockValidateProtectedIssuanceTrustListStatement,
        )

        every { mockProtectedIssuanceTrustListStatement.vctValues } returns listOf(VCT)

        coEvery { mockGetTrustDomainFromDid(ISSUER_DID) } returns Ok(ISSUER_DOMAIN)

        coEvery {
            mockTrustStatementRepository.fetchProtectedIssuanceTrustListStatement(ISSUER_DOMAIN)
        } returns Ok(mockProtectedIssuanceTrustListStatementJwt)

        coEvery {
            mockValidateProtectedIssuanceTrustListStatement(mockProtectedIssuanceTrustListStatementJwt)
        } returns Ok(mockProtectedIssuanceTrustListStatement)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `A ProtectedIssuanceTrustListStatement is correctly processed`() = runTest {
        val result = useCase(
            issuerDid = ISSUER_DID,
            vct = VCT,
        ).assertOk()

        assertTrue(result)
    }

    @Test
    fun `Processing a ProtectedIssuanceTrustListStatement maps trust domain errors`() = runTest {
        val exception = IllegalStateException("trust error")
        coEvery {
            mockGetTrustDomainFromDid(any())
        } returns Err(TrustRegistryError.Unexpected(exception))

        useCase(
            issuerDid = ISSUER_DID,
            vct = VCT,
        ).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `Processing a ProtectedIssuanceTrustListStatement maps trust repo errors`() = runTest {
        val exception = IllegalStateException("trust error")
        coEvery {
            mockTrustStatementRepository.fetchProtectedIssuanceTrustListStatement(any())
        } returns Err(TrustRegistryError.Unexpected(exception))

        useCase(
            issuerDid = ISSUER_DID,
            vct = VCT,
        ).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `Processing a ProtectedIssuanceTrustListStatement maps validation errors`() = runTest {
        val exception = IllegalStateException("trust error")
        coEvery {
            mockValidateProtectedIssuanceTrustListStatement(any())
        } returns Err(TrustRegistryError.Unexpected(exception))

        useCase(
            issuerDid = ISSUER_DID,
            vct = VCT,
        ).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `A ProtectedIssuanceTrustListStatement that does not contain the vct value returns false`() = runTest {
        val result = useCase(
            issuerDid = ISSUER_DID,
            vct = "other vct"
        ).assertOk()

        assertFalse(result)
    }

    private companion object Companion {
        const val ISSUER_DID = "issuer did"
        const val ISSUER_DOMAIN = "issuer domain"
        const val VCT = "vct"
    }
}

package ch.admin.foitt.wallet.platform.trustRegistry

import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ProtectedIssuanceAuthorizationObject
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ProtectedIssuanceAuthorizationTrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustRegistryError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatementActor
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VcSchemaTrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.FetchVcSchemaTrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ProcessProtectedIssuanceAuthorizationTrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ProcessProtectedIssuanceTrustListStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateProtectedIssuanceAuthorizationTrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation.ProcessProtectedIssuanceAuthorizationTrustStatementImpl
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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ProcessProtectedIssuanceAuthorizationTrustStatementImplTest {
    @MockK
    private lateinit var mockProcessProtectedIssuanceTrustListStatement: ProcessProtectedIssuanceTrustListStatement

    @MockK
    private lateinit var mockValidateProtectedIssuanceAuthorizationTrustStatement:
        ValidateProtectedIssuanceAuthorizationTrustStatement

    @MockK
    private lateinit var mockFetchVcSchemaTrustStatus: FetchVcSchemaTrustStatus

    @MockK
    private lateinit var mockProtectedIssuanceAuthorizationTrustStatementJwt: Jwt

    @MockK
    private lateinit var mockProtectedIssuanceAuthorizationTrustStatement: ProtectedIssuanceAuthorizationTrustStatement

    @MockK
    private lateinit var mockProtectedIssuanceAuthorizationObject: ProtectedIssuanceAuthorizationObject

    private lateinit var useCase: ProcessProtectedIssuanceAuthorizationTrustStatement

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        useCase = ProcessProtectedIssuanceAuthorizationTrustStatementImpl(
            processProtectedIssuanceTrustListStatement = mockProcessProtectedIssuanceTrustListStatement,
            validateProtectedIssuanceAuthorizationTrustStatement = mockValidateProtectedIssuanceAuthorizationTrustStatement,
            fetchVcSchemaTrustStatus = mockFetchVcSchemaTrustStatus,
        )

        every { mockProtectedIssuanceAuthorizationObject.vct } returns VCT

        every {
            mockProtectedIssuanceAuthorizationTrustStatement.protectedIssuanceAuthorizationObject
        } returns mockProtectedIssuanceAuthorizationObject

        coEvery {
            mockProcessProtectedIssuanceTrustListStatement(
                issuerDid = ISSUER_DID,
                vct = VCT,
            )
        } returns Ok(true)

        coEvery {
            mockValidateProtectedIssuanceAuthorizationTrustStatement(
                trustStatement = mockProtectedIssuanceAuthorizationTrustStatementJwt,
                actorDid = ISSUER_DID,
            )
        } returns Ok(mockProtectedIssuanceAuthorizationTrustStatement)

        coEvery {
            mockFetchVcSchemaTrustStatus(
                trustStatementActor = TrustStatementActor.ISSUER,
                actorDid = ISSUER_DID,
                vcSchemaId = VCT,
            )
        } returns Ok(VcSchemaTrustStatus.TRUSTED)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `A ProtectedIssuanceAuthorizationTrustStatement is correctly processed and returns trusted`() = runTest {
        val result = useCase(
            protectedIssuanceAuthorizationTrustStatement = mockProtectedIssuanceAuthorizationTrustStatementJwt,
            actorDid = ISSUER_DID,
            vct = VCT,
        ).assertOk()

        assertEquals(VcSchemaTrustStatus.TRUSTED, result)

        coVerify(exactly = 1) {
            mockValidateProtectedIssuanceAuthorizationTrustStatement(any(), any())
        }
        coVerify(exactly = 0) {
            mockFetchVcSchemaTrustStatus(any(), any(), any())
        }
    }

    @Test
    fun `Processing a piaTS when issuance is not protected returns unprotected`() = runTest {
        coEvery {
            mockProcessProtectedIssuanceTrustListStatement(
                issuerDid = ISSUER_DID,
                vct = VCT,
            )
        } returns Ok(false)

        val result = useCase(
            protectedIssuanceAuthorizationTrustStatement = mockProtectedIssuanceAuthorizationTrustStatementJwt,
            actorDid = ISSUER_DID,
            vct = VCT,
        ).assertOk()

        assertEquals(VcSchemaTrustStatus.UNPROTECTED, result)

        coVerify(exactly = 0) {
            mockValidateProtectedIssuanceAuthorizationTrustStatement(any(), any())
            mockFetchVcSchemaTrustStatus(any(), any(), any())
        }
    }

    @Test
    fun `Processing a ProtectedIssuanceAuthorizationTrustStatement where issuance is protected but piaTS is missing falls back to V1`() = runTest {
        val result = useCase(
            protectedIssuanceAuthorizationTrustStatement = null,
            actorDid = ISSUER_DID,
            vct = VCT,
        ).assertOk()

        assertEquals(VcSchemaTrustStatus.TRUSTED, result)

        coVerify(exactly = 1) {
            mockFetchVcSchemaTrustStatus(any(), any(), any())
        }
    }

    @Test
    fun `Processing a ProtectedIssuanceAuthorizationTrustStatement where issuance is protected but piaTS is missing and V1 is untrusted returns an error`() = runTest {
        coEvery { mockFetchVcSchemaTrustStatus(any(), any(), any()) } returns Ok(VcSchemaTrustStatus.NOT_TRUSTED)

        useCase(
            protectedIssuanceAuthorizationTrustStatement = null,
            actorDid = ISSUER_DID,
            vct = VCT,
        ).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `Processing a ProtectedIssuanceAuthorizationTrustStatement where issuance is protected but piaTS is missing and V1 fails returns an error`() = runTest {
        coEvery { mockFetchVcSchemaTrustStatus(any(), any(), any()) } returns
            Err(TrustRegistryError.Unexpected(IllegalStateException()))

        useCase(
            protectedIssuanceAuthorizationTrustStatement = null,
            actorDid = ISSUER_DID,
            vct = VCT,
        ).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `Processing a ProtectedIssuanceAuthorizationTrustStatement maps validation errors`() = runTest {
        val exception = IllegalStateException("trust error")
        coEvery {
            mockValidateProtectedIssuanceAuthorizationTrustStatement(any(), any())
        } returns Err(TrustRegistryError.Unexpected(exception))

        useCase(
            protectedIssuanceAuthorizationTrustStatement = mockProtectedIssuanceAuthorizationTrustStatementJwt,
            actorDid = ISSUER_DID,
            vct = VCT,
        ).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `Processing a ProtectedIssuanceAuthorizationTrustStatement where list statement could not be processed returns trusted`() = runTest {
        val exception = IllegalStateException("trust error")
        coEvery {
            mockValidateProtectedIssuanceAuthorizationTrustStatement(any(), any())
        } returns Err(TrustRegistryError.Unexpected(exception))

        useCase(
            protectedIssuanceAuthorizationTrustStatement = mockProtectedIssuanceAuthorizationTrustStatementJwt,
            actorDid = ISSUER_DID,
            vct = VCT,
        ).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `A ProtectedIssuanceAuthorizationTrustStatement that does not contain the vct value returns an error`() = runTest {
        every { mockProtectedIssuanceAuthorizationObject.vct } returns "other vct"

        useCase(
            protectedIssuanceAuthorizationTrustStatement = mockProtectedIssuanceAuthorizationTrustStatementJwt,
            actorDid = ISSUER_DID,
            vct = VCT,
        ).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `A ProtectedIssuanceAuthorizationTrustStatement that does not equal the credential vct returns an error`() = runTest {
        val otherVct = "other vct"
        coEvery {
            mockProcessProtectedIssuanceTrustListStatement(
                issuerDid = ISSUER_DID,
                vct = otherVct,
            )
        } returns Ok(true)

        useCase(
            protectedIssuanceAuthorizationTrustStatement = mockProtectedIssuanceAuthorizationTrustStatementJwt,
            actorDid = ISSUER_DID,
            vct = otherVct,
        ).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    private companion object Companion {
        const val ISSUER_DID = "issuer did"
        const val VCT = "vct"
    }
}

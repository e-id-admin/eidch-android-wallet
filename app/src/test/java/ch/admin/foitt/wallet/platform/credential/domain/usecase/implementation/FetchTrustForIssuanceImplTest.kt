package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.wallet.platform.actorEnvironment.domain.model.ActorEnvironment
import ch.admin.foitt.wallet.platform.actorEnvironment.domain.usecase.GetActorEnvironment
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.FetchTrustForIssuance
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockNonComplianceData.nonComplianceData
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IdentityV1TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IdentityV2TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustRegistryError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VcSchemaTrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.CheckActorCompliance
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ProcessIdentityV1TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ProcessProtectedIssuanceAuthorizationTrustStatement
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import ch.admin.foitt.wallet.util.assertOkNullable
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull

class FetchTrustForIssuanceImplTest {
    @MockK
    private lateinit var mockProcessProtectedIssuanceAuthorizationTrustStatement:
        ProcessProtectedIssuanceAuthorizationTrustStatement

    @MockK
    private lateinit var mockGetActorEnvironment: GetActorEnvironment

    @MockK
    private lateinit var mockProcessIdentityV1TrustStatement: ProcessIdentityV1TrustStatement

    @MockK
    private lateinit var mockCheckActorCompliance: CheckActorCompliance

    @MockK
    private lateinit var mockIdentityV1TrustStatement: IdentityV1TrustStatement

    @MockK
    private lateinit var mockIdentityV2TrustStatement: IdentityV2TrustStatement

    @MockK
    private lateinit var mockProtectedIssuanceAuthorizationTrustStatementJwt: Jwt

    private lateinit var useCase: FetchTrustForIssuance

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = FetchTrustForIssuanceImpl(
            processProtectedIssuanceAuthorizationTrustStatement = mockProcessProtectedIssuanceAuthorizationTrustStatement,
            getActorEnvironment = mockGetActorEnvironment,
            processIdentityV1TrustStatement = mockProcessIdentityV1TrustStatement,
            checkActorCompliance = mockCheckActorCompliance,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `FetchTrustForIssuance for a V2 trust statement runs specific things`() = runTest {
        val result = useCase(
            identityTrustStatement = mockIdentityV2TrustStatement,
            protectedIssuanceAuthorizationTrustStatement = mockProtectedIssuanceAuthorizationTrustStatementJwt,
            issuerDid = ISSUER_DID,
            vcSchemaId = VC_SCHEMA_ID,
        ).assertOk()

        assertEquals(mockIdentityV2TrustStatement, result?.identityTrustStatement)
        assertEquals(VcSchemaTrustStatus.TRUSTED, result?.vcSchemaTrustStatus)
        assertEquals(nonComplianceData, result?.nonComplianceData)

        coVerify(exactly = 1) {
            mockProcessProtectedIssuanceAuthorizationTrustStatement(
                protectedIssuanceAuthorizationTrustStatement = mockProtectedIssuanceAuthorizationTrustStatementJwt,
                actorDid = ISSUER_DID,
                vct = VC_SCHEMA_ID,
            )
        }
        coVerify(exactly = 0) {
            mockProcessIdentityV1TrustStatement(any())
        }
        coVerify(exactly = 1) {
            mockCheckActorCompliance(actorDid = ISSUER_DID)
        }
    }

    @Test
    fun `FetchTrustForIssuance maps errors from processing piaTS`() = runTest {
        val exception = IllegalStateException("trust error")
        coEvery {
            mockProcessProtectedIssuanceAuthorizationTrustStatement(any(), any(), any())
        } returns Err(TrustRegistryError.Unexpected(exception))

        useCase(
            identityTrustStatement = mockIdentityV2TrustStatement,
            protectedIssuanceAuthorizationTrustStatement = mockProtectedIssuanceAuthorizationTrustStatementJwt,
            issuerDid = ISSUER_DID,
            vcSchemaId = VC_SCHEMA_ID,
        ).assertErrorType(CredentialError.UnauthorizedIssuance::class)
    }

    @Test
    fun `FetchTrustForIssuance for a V1 trust statement runs specific things`() = runTest {
        val result = useCase(
            identityTrustStatement = null,
            protectedIssuanceAuthorizationTrustStatement = null,
            issuerDid = ISSUER_DID,
            vcSchemaId = VC_SCHEMA_ID,
        ).assertOk()

        assertEquals(mockIdentityV1TrustStatement, result?.identityTrustStatement)
        assertEquals(VcSchemaTrustStatus.TRUSTED, result?.vcSchemaTrustStatus)
        assertEquals(nonComplianceData, result?.nonComplianceData)

        coVerify(exactly = 1) {
            mockProcessIdentityV1TrustStatement(ISSUER_DID)
            mockProcessProtectedIssuanceAuthorizationTrustStatement(null, ISSUER_DID, VC_SCHEMA_ID)
            mockCheckActorCompliance(actorDid = ISSUER_DID)
        }
    }

    @Test
    fun `FetchTrustForIssuance passes along the null result`() = runTest {
        coEvery { mockProcessIdentityV1TrustStatement(any()) } returns Ok(null)

        val result = useCase(
            identityTrustStatement = null,
            protectedIssuanceAuthorizationTrustStatement = mockProtectedIssuanceAuthorizationTrustStatementJwt,
            issuerDid = ISSUER_DID,
            vcSchemaId = VC_SCHEMA_ID,
        ).assertOk()

        assertNull(result?.identityTrustStatement)
    }

    @Test
    fun `FetchTrustForIssuance maps errors from identity v1 processing and returns unverified issuer`() = runTest {
        val exception = IllegalStateException("trust error")
        coEvery {
            mockProcessIdentityV1TrustStatement(any())
        } returns Err(TrustRegistryError.Unexpected(exception))

        useCase(
            identityTrustStatement = null,
            protectedIssuanceAuthorizationTrustStatement = mockProtectedIssuanceAuthorizationTrustStatementJwt,
            issuerDid = ISSUER_DID,
            vcSchemaId = VC_SCHEMA_ID,
        ).assertErrorType(CredentialError.UnverifiedIssuer::class)
    }

    @Test
    fun `FetchTrustForIssuance where the actor is external returns null`() = runTest {
        coEvery { mockGetActorEnvironment(any()) } returns ActorEnvironment.EXTERNAL

        val result = useCase(
            identityTrustStatement = null,
            protectedIssuanceAuthorizationTrustStatement = null,
            issuerDid = ISSUER_DID,
            vcSchemaId = VC_SCHEMA_ID,
        ).assertOkNullable()

        assertNull(result)
        coVerify(exactly = 0) {
            mockCheckActorCompliance(any())
        }
    }

    private fun setupDefaultMocks() {
        coEvery {
            mockProcessProtectedIssuanceAuthorizationTrustStatement(
                protectedIssuanceAuthorizationTrustStatement = mockProtectedIssuanceAuthorizationTrustStatementJwt,
                actorDid = ISSUER_DID,
                vct = VC_SCHEMA_ID,
            )
        } returns Ok(VcSchemaTrustStatus.TRUSTED)
        coEvery {
            mockProcessProtectedIssuanceAuthorizationTrustStatement(
                protectedIssuanceAuthorizationTrustStatement = null,
                actorDid = ISSUER_DID,
                vct = VC_SCHEMA_ID,
            )
        } returns Ok(VcSchemaTrustStatus.TRUSTED)
        coEvery {
            mockGetActorEnvironment(ISSUER_DID)
        } returns ActorEnvironment.PRODUCTION
        coEvery {
            mockProcessIdentityV1TrustStatement(ISSUER_DID)
        } returns Ok(mockIdentityV1TrustStatement)
        coEvery {
            mockCheckActorCompliance(ISSUER_DID)
        } returns nonComplianceData
    }

    private companion object {
        const val ISSUER_DID = "issuer did"
        const val VC_SCHEMA_ID = "vcSchemaId"
    }
}

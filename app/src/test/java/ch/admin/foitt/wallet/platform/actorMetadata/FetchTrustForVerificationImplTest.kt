package ch.admin.foitt.wallet.platform.actorMetadata

import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.ClientIdentifier
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.VerifierInfo
import ch.admin.foitt.wallet.platform.actorEnvironment.domain.model.ActorEnvironment
import ch.admin.foitt.wallet.platform.actorEnvironment.domain.usecase.GetActorEnvironment
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorMetaDataError
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.FetchTrustForVerification
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.implementation.FetchTrustForVerificationImpl
import ch.admin.foitt.wallet.platform.actorMetadata.mock.ActorMetadataMocks.nonComplianceData
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IdentityV1TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IdentityV2TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustRegistryError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VcSchemaTrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VerificationQueryPublicStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.CheckActorCompliance
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ProcessIdentityTrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ProcessIdentityV1TrustStatement
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
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FetchTrustForVerificationImplTest {
    @MockK
    private lateinit var mockGetActorEnvironment: GetActorEnvironment

    @MockK
    private lateinit var mockProcessIdentityV1TrustStatement: ProcessIdentityV1TrustStatement

    @MockK
    private lateinit var mockProcessIdentityTrustStatement: ProcessIdentityTrustStatement

    @MockK
    private lateinit var mockCheckActorCompliance: CheckActorCompliance

    @MockK
    private lateinit var mockAuthorizationRequest: AuthorizationRequest

    @MockK
    private lateinit var mockClientIdentifier: ClientIdentifier

    @MockK
    private lateinit var mockIdentityV2Jwt: Jwt

    @MockK
    private lateinit var mockVqPsJwt: Jwt

    @MockK
    private lateinit var mockIdentityV2TrustStatement: IdentityV2TrustStatement

    @MockK
    private lateinit var mockIdentityV1TrustStatement: IdentityV1TrustStatement

    private lateinit var useCase: FetchTrustForVerification

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        useCase = FetchTrustForVerificationImpl(
            getActorEnvironment = mockGetActorEnvironment,
            processIdentityV1TrustStatement = mockProcessIdentityV1TrustStatement,
            processIdentityTrustStatement = mockProcessIdentityTrustStatement,
            checkActorCompliance = mockCheckActorCompliance,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    private fun setupDefaultMocks() {
        every { mockIdentityV2Jwt.type } returns IdentityV2TrustStatement.TYPE
        every { mockVqPsJwt.type } returns VerificationQueryPublicStatement.TYPE
        every { mockClientIdentifier.clientId } returns CLIENT_ID
        every { mockIdentityV2TrustStatement.sub } returns CLIENT_ID
        every { mockIdentityV1TrustStatement.sub } returns CLIENT_ID
        every { mockAuthorizationRequest.clientIdentifier } returns mockClientIdentifier
        every { mockAuthorizationRequest.responseUri } returns RESPONSE_URI
        every { mockAuthorizationRequest.verifierInfo } returns listOf(
            VerifierInfo(format = "jwt", data = mockIdentityV2Jwt),
        )

        coEvery { mockGetActorEnvironment(any()) } returns ActorEnvironment.PRODUCTION
        coEvery {
            mockProcessIdentityTrustStatement(any(), any())
        } returns Ok(mockIdentityV2TrustStatement)
        coEvery { mockProcessIdentityV1TrustStatement(any()) } returns Ok(mockIdentityV1TrustStatement)
        coEvery { mockCheckActorCompliance(any()) } returns nonComplianceData
    }

    @Test
    fun `Successful fetch with Identity V2 returns TRUSTED`() = runTest {
        val result = useCase(mockAuthorizationRequest).assertOk()

        assertEquals(mockIdentityV2TrustStatement, result.identityTrustStatement)
        assertEquals(VcSchemaTrustStatus.UNPROTECTED, result.vcSchemaTrustStatus)
        assertEquals(nonComplianceData, result.nonComplianceData)
        coVerify(exactly = 1) {
            mockCheckActorCompliance(actorDid = CLIENT_ID)
        }
    }

    @Test
    fun `Fetch without Identity V2 uses Identity V1 if in PRODUCTION`() = runTest {
        every { mockAuthorizationRequest.verifierInfo } returns null

        val result = useCase(mockAuthorizationRequest).assertOk()

        assertEquals(mockIdentityV1TrustStatement, result.identityTrustStatement)
        assertEquals(VcSchemaTrustStatus.UNPROTECTED, result.vcSchemaTrustStatus)
    }

    @Test
    fun `Fetch without Identity V2 uses Identity V1 if in BETA`() = runTest {
        every { mockAuthorizationRequest.verifierInfo } returns null
        coEvery { mockGetActorEnvironment(any()) } returns ActorEnvironment.BETA

        val result = useCase(mockAuthorizationRequest).assertOk()

        assertEquals(mockIdentityV1TrustStatement, result.identityTrustStatement)
        assertEquals(VcSchemaTrustStatus.UNPROTECTED, result.vcSchemaTrustStatus)
    }

    @Test
    fun `Fetch without Identity V2 returns null statement if in EXTERNAL`() = runTest {
        every { mockAuthorizationRequest.verifierInfo } returns null
        coEvery { mockGetActorEnvironment(any()) } returns ActorEnvironment.EXTERNAL

        val result = useCase(mockAuthorizationRequest).assertOk()

        assertNull(result.identityTrustStatement)
        assertEquals(VcSchemaTrustStatus.UNPROTECTED, result.vcSchemaTrustStatus)
    }

    @Test
    fun `Identity v2 error is mapped to unverified verifier`() = runTest {
        val exception = IllegalStateException("trust error")
        coEvery {
            mockProcessIdentityTrustStatement(any(), any())
        } returns Err(TrustRegistryError.Unexpected(exception))

        useCase(mockAuthorizationRequest).assertErrorType(ActorMetaDataError.UnverifiedVerifier::class)
    }

    @Test
    fun `Identity v1 null result is passed along`() = runTest {
        every { mockAuthorizationRequest.verifierInfo } returns null
        coEvery {
            mockProcessIdentityV1TrustStatement(any())
        } returns Ok(null)

        val result = useCase(mockAuthorizationRequest).assertOk()
        assertNull(result.identityTrustStatement)
    }

    @Test
    fun `Identity v1 error is mapped to unverified verifier`() = runTest {
        every { mockAuthorizationRequest.verifierInfo } returns null
        val exception = IllegalStateException("trust error")
        coEvery {
            mockProcessIdentityV1TrustStatement(any())
        } returns Err(TrustRegistryError.Unexpected(exception))

        useCase(mockAuthorizationRequest).assertErrorType(ActorMetaDataError.UnverifiedVerifier::class)
    }

    private companion object {
        const val CLIENT_ID = "did:tdw:example.com"
        const val RESPONSE_URI = "response uri"
    }
}

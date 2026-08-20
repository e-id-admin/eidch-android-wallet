package ch.admin.foitt.wallet.platform.trustRegistry

import ch.admin.foitt.openid4vc.domain.model.claimsPathPointer.ClaimsPathPointerComponent
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.ClientIdentifier
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.VerifierInfo
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.PresentationRequestWithRaw
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.VerificationProcessType
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IdentityV2TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ProtectedVerificationClaims
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustRegistryError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VerificationAuthorizationTrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ProcessVerificationAuthorizationTrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateVerificationAuthorizationTrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation.ProcessVerificationAuthorizationTrustStatementImpl
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ProcessVerificationAuthorizationTrustStatementImplTest {
    @MockK
    private lateinit var mockProtectedVerificationClaims: ProtectedVerificationClaims

    @MockK
    private lateinit var mockValidateVerificationAuthorizationTrustStatement:
        ValidateVerificationAuthorizationTrustStatement

    @MockK
    private lateinit var mockAuthorizationRequest: AuthorizationRequest

    @MockK
    private lateinit var mockClientIdentifier: ClientIdentifier

    @MockK
    private lateinit var mockPresentationRequestWithRaw: PresentationRequestWithRaw

    @MockK
    private lateinit var mockIdTSJwt: Jwt

    @MockK
    private lateinit var mockPvaTSJwt: Jwt

    @MockK
    private lateinit var mockPvaTS: VerificationAuthorizationTrustStatement

    private lateinit var useCase: ProcessVerificationAuthorizationTrustStatement

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        setupMocks()
        useCase = ProcessVerificationAuthorizationTrustStatementImpl(
            validateVerificationAuthorizationTrustStatement = mockValidateVerificationAuthorizationTrustStatement,
            protectedVerificationClaims = mockProtectedVerificationClaims,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `A VerificationAuthorizationTrustStatement is correctly processed`() = runTest {
        useCase(mockPresentationRequestWithRaw, listOf(CLAIMS_PATH_POINTER)).assertOk()

        coVerify(exactly = 1) {
            mockValidateVerificationAuthorizationTrustStatement(any(), any())
        }
    }

    @Test
    fun `A VerificationAuthorizationTrustStatement is correctly processed for multiple requested claims`() = runTest {
        val otherProtectedClaim = listOf(
            ClaimsPathPointerComponent.Index(0),
            ClaimsPathPointerComponent.String(PROTECTED_CLAIM_NAME),
            ClaimsPathPointerComponent.Null
        )
        val otherClaim = listOf(ClaimsPathPointerComponent.String("other"))
        useCase(
            mockPresentationRequestWithRaw,
            listOf(CLAIMS_PATH_POINTER, otherClaim, otherProtectedClaim)
        ).assertOk()

        coVerify(exactly = 1) {
            mockValidateVerificationAuthorizationTrustStatement(any(), any())
        }
    }

    @Test
    fun `A VerificationAuthorizationTrustStatement is correctly processed even when multiple jwts are present`() = runTest {
        val wrong = mockk<VerifierInfo> {
            every { data.type } returns "wrong"
        }
        val correct = mockk<VerifierInfo> {
            every { data } returns mockPvaTSJwt
        }

        every { mockAuthorizationRequest.verifierInfo } returns listOf(wrong, correct)

        useCase(mockPresentationRequestWithRaw, listOf(CLAIMS_PATH_POINTER)).assertOk()
    }

    @Test
    fun `Processing a VerificationAuthorizationTrustStatement does not evaluate the statement when no claim is protected`() = runTest {
        every { mockProtectedVerificationClaims.claims } returns emptySet()

        useCase(mockPresentationRequestWithRaw, listOf(CLAIMS_PATH_POINTER)).assertOk()

        coVerify(exactly = 0) {
            mockValidateVerificationAuthorizationTrustStatement(any(), any())
        }
    }

    @Test
    fun `Processing a VerificationAuthorizationTrustStatement does not evaluate the statement when no claim is requested`() = runTest {
        useCase(mockPresentationRequestWithRaw, emptyList()).assertOk()

        coVerify(exactly = 0) {
            mockValidateVerificationAuthorizationTrustStatement(any(), any())
        }
    }

    @Test
    fun `Processing a VerificationAuthorizationTrustStatement does not evaluate the statement when verified proximity`() = runTest {
        every { mockPresentationRequestWithRaw.verificationProcessType } returns VerificationProcessType.PROXIMITY
        every { mockPresentationRequestWithRaw.verifierAttestationTrusted } returns true

        useCase(mockPresentationRequestWithRaw, listOf(CLAIMS_PATH_POINTER)).assertOk()

        coVerify(exactly = 0) {
            mockValidateVerificationAuthorizationTrustStatement(any(), any())
        }
    }

    @Test
    fun `Processing a VerificationAuthorizationTrustStatement is correctly processed when unverified proximity`() = runTest {
        every { mockPresentationRequestWithRaw.verificationProcessType } returns VerificationProcessType.PROXIMITY
        every { mockPresentationRequestWithRaw.verifierAttestationTrusted } returns false

        useCase(mockPresentationRequestWithRaw, listOf(CLAIMS_PATH_POINTER)).assertOk()

        coVerify(exactly = 1) {
            mockValidateVerificationAuthorizationTrustStatement(any(), any())
        }
    }

    @Test
    fun `Processing a VerificationAuthorizationTrustStatement is correctly processed when proximity has null trust`() = runTest {
        every { mockPresentationRequestWithRaw.verificationProcessType } returns VerificationProcessType.PROXIMITY
        every { mockPresentationRequestWithRaw.verifierAttestationTrusted } returns null

        useCase(mockPresentationRequestWithRaw, listOf(CLAIMS_PATH_POINTER)).assertOk()

        coVerify(exactly = 1) {
            mockValidateVerificationAuthorizationTrustStatement(any(), any())
        }
    }

    @Test
    fun `Processing a VerificationAuthorizationTrustStatement without idTS just returns`() = runTest {
        val verifierInfoEntryPvaTS = mockk<VerifierInfo>()
        every { verifierInfoEntryPvaTS.data } returns mockPvaTSJwt
        every { mockPvaTSJwt.type } returns VerificationAuthorizationTrustStatement.TYPE
        every { mockAuthorizationRequest.verifierInfo } returns listOf(verifierInfoEntryPvaTS)

        useCase(mockPresentationRequestWithRaw, listOf(CLAIMS_PATH_POINTER)).assertOk()

        coVerify(exactly = 0) {
            mockProtectedVerificationClaims.claims
            mockValidateVerificationAuthorizationTrustStatement(any(), any())
        }
    }

    @Test
    fun `Processing a VerificationAuthorizationTrustStatement where claim is protected but VerificationAuthorizationTrustStatement is missing returns an error`() = runTest {
        every { mockPvaTSJwt.type } returns "otherType"

        useCase(mockPresentationRequestWithRaw, listOf(CLAIMS_PATH_POINTER))
            .assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `Processing a VerificationAuthorizationTrustStatement where claim is protected but VerificationAuthorizationTrustStatement has other protected fields returns an error`() = runTest {
        every { mockPvaTS.authorizedFields } returns setOf("other", "andAnother")

        useCase(mockPresentationRequestWithRaw, listOf(CLAIMS_PATH_POINTER))
            .assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `Processing a VerificationAuthorizationTrustStatement maps validation errors`() = runTest {
        val exception = IllegalStateException()
        coEvery {
            mockValidateVerificationAuthorizationTrustStatement(any(), any())
        } returns Err(TrustRegistryError.Unexpected(exception))

        useCase(mockPresentationRequestWithRaw, listOf(CLAIMS_PATH_POINTER))
            .assertErrorType(TrustRegistryError.Unexpected::class)
    }

    private fun setupMocks() {
        every { mockClientIdentifier.clientId } returns CLIENT_ID
        every { mockAuthorizationRequest.clientIdentifier } returns mockClientIdentifier
        every { mockAuthorizationRequest.responseUri } returns RESPONSE_URI
        every { mockPresentationRequestWithRaw.authorizationRequest } returns mockAuthorizationRequest
        every { mockPresentationRequestWithRaw.verificationProcessType } returns VerificationProcessType.NETWORK
        every { mockPresentationRequestWithRaw.verifierAttestationTrusted } returns null

        coEvery {
            mockValidateVerificationAuthorizationTrustStatement(mockPvaTSJwt, CLIENT_ID)
        } returns Ok(mockPvaTS)

        every { mockProtectedVerificationClaims.claims } returns setOf(PROTECTED_CLAIM_NAME)
        every { mockPvaTS.authorizedFields } returns setOf(PROTECTED_CLAIM_NAME)

        val verifierInfoEntryIdTS = mockk<VerifierInfo>()
        every { verifierInfoEntryIdTS.data } returns mockIdTSJwt
        every { mockIdTSJwt.type } returns IdentityV2TrustStatement.TYPE
        val verifierInfoEntryPvaTS = mockk<VerifierInfo>()
        every { verifierInfoEntryPvaTS.data } returns mockPvaTSJwt
        every { mockPvaTSJwt.type } returns VerificationAuthorizationTrustStatement.TYPE
        every { mockAuthorizationRequest.verifierInfo } returns listOf(verifierInfoEntryIdTS, verifierInfoEntryPvaTS)
    }

    private companion object {
        const val CLIENT_ID = "client_id"
        const val PROTECTED_CLAIM_NAME = "protected_claim_name"
        const val RESPONSE_URI = "response_uri"
        val CLAIMS_PATH_POINTER = listOf(ClaimsPathPointerComponent.String(PROTECTED_CLAIM_NAME))
    }
}

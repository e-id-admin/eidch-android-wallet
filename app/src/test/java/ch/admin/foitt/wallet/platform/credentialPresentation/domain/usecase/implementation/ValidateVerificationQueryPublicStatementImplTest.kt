package ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.SignatureAlgorithm
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.ValidateVerificationQueryPublicStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustRegistryError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VerificationQueryPublicStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VerificationQueryPublicStatement.Companion.TYPE
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VqPsRequest
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.IsTrustedDid
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateTrustStatementJwt
import ch.admin.foitt.wallet.util.SafeJsonTestInstance
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class ValidateVerificationQueryPublicStatementImplTest {

    private val safeJson = SafeJsonTestInstance.safeJson

    @MockK
    private lateinit var mockValidateTrustStatementJwt: ValidateTrustStatementJwt

    @MockK
    private lateinit var mockIsTrustedDid: IsTrustedDid

    @MockK
    private lateinit var mockVqPsJwt: Jwt

    private lateinit var useCase: ValidateVerificationQueryPublicStatement

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = ValidateVerificationQueryPublicStatementImpl(
            validateTrustStatementJwt = mockValidateTrustStatementJwt,
            isTrustedDid = mockIsTrustedDid,
            safeJson = safeJson,
        )
        setupDefaultMocks()
    }

    @Test
    fun `Successful validation returns Ok`() = runTest {
        val vqPsRequest = VqPsRequest(type = VqPsRequest.TYPE_DCQL, scope = "test-scope", query = mockk())
        val payload = mapOf(
            VerificationQueryPublicStatement.CLAIM_NAME_PURPOSE_NAME to JsonPrimitive("Purpose"),
            VerificationQueryPublicStatement.CLAIM_NAME_PURPOSE_DESCRIPTION to JsonPrimitive("Description"),
            VerificationQueryPublicStatement.CLAIM_NAME_REQUEST to safeJson.json.encodeToJsonElement(VqPsRequest.serializer(), vqPsRequest)
        )
        every { mockVqPsJwt.payloadJson } returns JsonObject(payload)

        val result = useCase(mockVqPsJwt, ACTOR_DID).assertOk()
        assertEquals(ACTOR_DID, result.sub)
        assertEquals(KEY_ID, result.kid)
        assertEquals("1.0", result.profileVersion)
        assertEquals("jti-123", result.jti)
        assertEquals("test-scope", result.request.scope)
        assertEquals("Purpose", result.purposeName["fallback"])
        assertEquals("Description", result.purposeDescription["fallback"])
    }

    @Test
    fun `Successful validation with localized purpose returns Ok`() = runTest {
        val vqPsRequest = VqPsRequest(type = VqPsRequest.TYPE_DCQL, scope = "test-scope", query = mockk())
        val payload = mapOf(
            "${VerificationQueryPublicStatement.CLAIM_NAME_PURPOSE_NAME}#de" to JsonPrimitive("Zweck"),
            "${VerificationQueryPublicStatement.CLAIM_NAME_PURPOSE_DESCRIPTION}#de" to JsonPrimitive("Beschreibung"),
            VerificationQueryPublicStatement.CLAIM_NAME_REQUEST to safeJson.json.encodeToJsonElement(VqPsRequest.serializer(), vqPsRequest)
        )
        every { mockVqPsJwt.payloadJson } returns JsonObject(payload)

        val result = useCase(mockVqPsJwt, ACTOR_DID).assertOk()
        assertEquals("Zweck", result.purposeName["de"])
        assertEquals("Beschreibung", result.purposeDescription["de"])
    }

    @Test
    fun `JWT validation error is mapped`() = runTest {
        coEvery { mockValidateTrustStatementJwt(mockVqPsJwt) } returns Err(TrustRegistryError.Unexpected(null))

        useCase(mockVqPsJwt, ACTOR_DID).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `Invalid vqPS type returns error`() = runTest {
        every { mockVqPsJwt.type } returns "wrong-type"

        useCase(mockVqPsJwt, ACTOR_DID).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `Mismatching actorDid returns error`() = runTest {
        every { mockVqPsJwt.subject } returns "other-did"

        useCase(mockVqPsJwt, ACTOR_DID).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `Missing keyId returns error`() = runTest {
        every { mockVqPsJwt.keyId } returns null

        useCase(mockVqPsJwt, ACTOR_DID).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `isTrustedDid errors are mapped`() = runTest {
        val exception = IllegalStateException("did error")
        coEvery {
            mockIsTrustedDid(any(), any())
        } returns Err(TrustRegistryError.Unexpected(exception))

        useCase(mockVqPsJwt, ACTOR_DID).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `Invalid vqPsRequest type returns error`() = runTest {
        val vqPsRequest = VqPsRequest(type = "wrong-type", scope = "test-scope", query = mockk())
        val payload = mapOf(
            VerificationQueryPublicStatement.CLAIM_NAME_REQUEST to safeJson.json.encodeToJsonElement(VqPsRequest.serializer(), vqPsRequest)
        )
        every { mockVqPsJwt.payloadJson } returns JsonObject(payload)

        useCase(mockVqPsJwt, ACTOR_DID).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    private fun setupDefaultMocks() {
        val mockSignedJwt = mockk<SignedJWT>(relaxed = true)
        val mockClaimsSet = mockk<JWTClaimsSet>(relaxed = true)
        every { mockVqPsJwt.signedJwt } returns mockSignedJwt
        every { mockSignedJwt.jwtClaimsSet } returns mockClaimsSet
        every { mockSignedJwt.header.getCustomParam(VerificationQueryPublicStatement.CLAIM_NAME_PROFILE_VERSION) } returns "1.0"
        every { mockClaimsSet.getStringClaim(VerificationQueryPublicStatement.CLAIM_NAME_JTI) } returns "jti-123"
        every { mockVqPsJwt.issuedAt } returns Instant.now()
        every { mockVqPsJwt.expInstant } returns Instant.now().plusSeconds(3600)

        every { mockVqPsJwt.type } returns TYPE
        every { mockVqPsJwt.subject } returns ACTOR_DID
        every { mockVqPsJwt.keyId } returns KEY_ID
        every { mockVqPsJwt.algorithm } returns SignatureAlgorithm.ES256.stdName

        coEvery { mockValidateTrustStatementJwt(mockVqPsJwt) } returns Ok(Unit)
        coEvery { mockIsTrustedDid(KEY_ID, TYPE) } returns Ok(Unit)
    }

    private companion object {
        const val ACTOR_DID = "did:example:123"
        const val KEY_ID = "keyId"
    }
}

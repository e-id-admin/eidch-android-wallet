package ch.admin.foitt.wallet.platform.trustRegistry

import ch.admin.foitt.openid4vc.domain.model.SignatureAlgorithm
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.TokenStatusListProperties
import ch.admin.foitt.wallet.platform.database.domain.model.DisplayLanguage
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ProtectedIssuanceAuthorizationObject
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ProtectedIssuanceAuthorizationTrustStatement.Companion.CLAIM_NAME_CAN_ISSUE
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ProtectedIssuanceAuthorizationTrustStatement.Companion.TYPE
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustRegistryError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatementStatusResult
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatementV2.Companion.CLAIM_NAME_JTI
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatementV2.Companion.CLAIM_NAME_PROFILE_VERSION
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.IsTrustedDid
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateProtectedIssuanceAuthorizationTrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateTrustStatementJwt
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateTrustStatementStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation.ValidateProtectedIssuanceAuthorizationTrustStatementImpl
import ch.admin.foitt.wallet.util.SafeJsonTestInstance
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class ValidateProtectedIssuanceAuthorizationTrustStatementImplTest {
    @MockK
    private lateinit var mockValidateTrustStatementJwt: ValidateTrustStatementJwt

    @MockK
    private lateinit var mockValidateTrustStatementStatus: ValidateTrustStatementStatus

    @MockK
    private lateinit var mockIsTrustedDid: IsTrustedDid

    @MockK
    private lateinit var mockTrustStatement: Jwt

    @MockK
    private lateinit var mockSignedJWT: SignedJWT

    @MockK
    private lateinit var mockJWSHeader: JWSHeader

    @MockK
    private lateinit var mockJWTClaimsSet: JWTClaimsSet

    private val safeJson = SafeJsonTestInstance.safeJson

    private lateinit var useCase: ValidateProtectedIssuanceAuthorizationTrustStatement

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        useCase = ValidateProtectedIssuanceAuthorizationTrustStatementImpl(
            validateTrustStatementJwt = mockValidateTrustStatementJwt,
            validateTrustStatementStatus = mockValidateTrustStatementStatus,
            isTrustedDid = mockIsTrustedDid,
            safeJson = safeJson,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `A valid ProtectedIssuanceAuthorizationTrustStatement passes validation`() = runTest {
        val result = useCase(
            trustStatement = mockTrustStatement,
            actorDid = ACTOR_DID
        ).assertOk()

        assertEquals(TYPE, result.typ)
        assertEquals(SignatureAlgorithm.ES256, result.alg)
        assertEquals(KEY_ID, result.kid)
        assertEquals(PROFILE_VERSION, result.profileVersion)
        assertEquals(JTI, result.jti)
        assertEquals(IAT_SECONDS, result.iat)
        assertEquals(EXP_SECONDS, result.exp)
        assertEquals(ACTOR_DID, result.sub)
        assertEquals(credentialStatusProperties, result.status)
        assertEquals(protectedIssuanceAuthorizationObject, result.protectedIssuanceAuthorizationObject)
    }

    @Test
    fun `Errors from validating jwt are mapped`() = runTest {
        val exception = IllegalStateException("jwt error")
        coEvery {
            mockValidateTrustStatementJwt(any())
        } returns Err(TrustRegistryError.Unexpected(exception))

        useCase(
            trustStatement = mockTrustStatement,
            actorDid = ACTOR_DID
        ).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `A jwt missing the type returns an error`() = runTest {
        every { mockTrustStatement.type } returns null

        useCase(
            trustStatement = mockTrustStatement,
            actorDid = ACTOR_DID
        ).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `A jwt with invalid type returns an error`() = runTest {
        every { mockTrustStatement.type } returns "other type"

        useCase(
            trustStatement = mockTrustStatement,
            actorDid = ACTOR_DID
        ).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `A jwt missing the sub returns an error`() = runTest {
        every { mockTrustStatement.subject } returns null

        useCase(
            trustStatement = mockTrustStatement,
            actorDid = ACTOR_DID
        ).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `A jwt with invalid sub returns an error`() = runTest {
        every { mockTrustStatement.subject } returns "other subject"

        useCase(
            trustStatement = mockTrustStatement,
            actorDid = ACTOR_DID
        ).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `A jwt with other actor did returns an error`() = runTest {
        useCase(
            trustStatement = mockTrustStatement,
            actorDid = "other did"
        ).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `Validation maps errors from status check`() = runTest {
        val exception = IllegalStateException("did error")
        coEvery {
            mockValidateTrustStatementStatus(mockTrustStatement)
        } returns Err(TrustRegistryError.Unexpected(exception))

        useCase(
            trustStatement = mockTrustStatement,
            actorDid = ACTOR_DID
        ).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `Validation maps errors from isTrustedDid check`() = runTest {
        val exception = IllegalStateException("did error")
        coEvery {
            mockIsTrustedDid(any(), any())
        } returns Err(TrustRegistryError.Unexpected(exception))

        useCase(
            trustStatement = mockTrustStatement,
            actorDid = ACTOR_DID,
        ).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `A jwt without can_issue returns an error`() = runTest {
        every { mockTrustStatement.payloadJson } returns buildJsonObject {}

        useCase(
            trustStatement = mockTrustStatement,
            actorDid = ACTOR_DID
        ).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `A jwt with invalid can_issue returns an error`() = runTest {
        every { mockTrustStatement.payloadJson } returns buildJsonObject {
            put(CLAIM_NAME_CAN_ISSUE, JsonPrimitive("invalid"))
        }

        useCase(
            trustStatement = mockTrustStatement,
            actorDid = ACTOR_DID
        ).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    private fun setupDefaultMocks() {
        every { mockTrustStatement.type } returns TYPE
        every { mockTrustStatement.keyId } returns KEY_ID
        every { mockTrustStatement.algorithm } returns "ES256"
        every { mockTrustStatement.subject } returns ACTOR_DID
        every { mockTrustStatement.issuedAt } returns iat
        every { mockTrustStatement.expInstant } returns exp
        every { mockTrustStatement.payloadJson } returns payload

        every { mockTrustStatement.signedJwt } returns mockSignedJWT
        every { mockSignedJWT.header } returns mockJWSHeader
        every { mockJWSHeader.getCustomParam(CLAIM_NAME_PROFILE_VERSION) } returns PROFILE_VERSION
        every { mockSignedJWT.jwtClaimsSet } returns mockJWTClaimsSet
        every { mockJWTClaimsSet.getStringClaim(CLAIM_NAME_JTI) } returns JTI

        coEvery {
            mockValidateTrustStatementJwt(mockTrustStatement)
        } returns Ok(Unit)

        coEvery {
            mockValidateTrustStatementStatus(mockTrustStatement)
        } returns Ok(TrustStatementStatusResult(status = credentialStatusProperties, kid = KEY_ID))

        coEvery { mockIsTrustedDid(KEY_ID, TYPE) } returns Ok(Unit)
    }

    private companion object {
        const val ACTOR_DID = "actor did"
        const val KEY_ID = "keyId"
        const val PROFILE_VERSION = "profile version"
        const val JTI = "jti"
        const val IAT_SECONDS = 1L
        val iat: Instant = Instant.ofEpochSecond(IAT_SECONDS)
        const val EXP_SECONDS = 2L
        val exp: Instant = Instant.ofEpochSecond(EXP_SECONDS)
        val credentialStatusProperties = TokenStatusListProperties(
            statusList = TokenStatusListProperties.StatusList(
                index = 0,
                uri = "https://example.com/status"
            )
        )
        const val VCT = "vct"
        const val VCT_NAME = "vct name"
        const val REASON = "reason"
        const val REASON_DE = "reason de"
        const val LOCALE_DE = "de"
        val protectedIssuanceAuthorizationObject = ProtectedIssuanceAuthorizationObject(
            vct = VCT,
            vctName = VCT_NAME,
            reason = mapOf(
                DisplayLanguage.FALLBACK to REASON,
                LOCALE_DE to REASON_DE,
            )
        )
        val payload = buildJsonObject {
            put(CLAIM_NAME_CAN_ISSUE, protectedIssuanceAuthorizationObject.toJsonObject())
        }

        private fun ProtectedIssuanceAuthorizationObject.toJsonObject(): JsonObject =
            SafeJsonTestInstance.json.encodeToJsonElement(value = this).jsonObject
    }
}

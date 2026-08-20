package ch.admin.foitt.wallet.platform.trustRegistry

import ch.admin.foitt.openid4vc.domain.model.SignatureAlgorithm
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.TokenStatusListProperties
import ch.admin.foitt.wallet.platform.database.domain.model.DisplayLanguage
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IdentityV2TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IdentityV2TrustStatement.Companion.CLAIM_NAME_ENTITY_NAME
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IdentityV2TrustStatement.Companion.CLAIM_NAME_REGISTRY_IDS
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IdentityV2TrustStatement.Companion.TYPE
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.RegistryId
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustRegistryError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatementStatusResult
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatementV2.Companion.CLAIM_NAME_JTI
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatementV2.Companion.CLAIM_NAME_PROFILE_VERSION
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatementV2.Companion.CLAIM_NAME_STATE_ACTOR
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.IsTrustedDid
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateIdentityTrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateTrustStatementJwt
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateTrustStatementStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation.ValidateIdentityTrustStatementImpl
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
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class ValidateIdentityTrustStatementImplTest {
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

    private lateinit var useCase: ValidateIdentityTrustStatement

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        useCase = ValidateIdentityTrustStatementImpl(
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
    fun `A valid identity trust statement passes validation`() = runTest {
        val result = useCase(
            trustStatement = mockTrustStatement,
            actorDid = ACTOR_DID,
        ).assertOk()

        assertEquals(IdentityV2TrustStatement.TYPE, result.typ)
        assertEquals(SignatureAlgorithm.ES256, result.alg)
        assertEquals(KEY_ID, result.kid)
        assertEquals(PROFILE_VERSION, result.profileVersion)
        assertEquals(JTI, result.jti)
        assertEquals(IAT_SECONDS, result.iat)
        assertEquals(EXP_SECONDS, result.exp)
        assertEquals(ACTOR_DID, result.sub)
        assertEquals(credentialStatusProperties, result.status)
        assertEquals(ENTITY_NAME_MAP, result.entityName)
        assertEquals(IS_STATE_ACTOR, result.isStateActor)
        assertEquals(registryIds, result.registryIds)
    }

    @Test
    fun `Validation maps error from jwt validation`() = runTest {
        val exception = IllegalStateException("jwt error")
        coEvery {
            mockValidateTrustStatementJwt(any())
        } returns Err(TrustRegistryError.Unexpected(exception))

        useCase(
            trustStatement = mockTrustStatement,
            actorDid = ACTOR_DID,
        ).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `A jwt missing the type returns an error`() = runTest {
        every { mockTrustStatement.type } returns null

        useCase(
            trustStatement = mockTrustStatement,
            actorDid = ACTOR_DID,
        ).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `A jwt with invalid type returns an error`() = runTest {
        every { mockTrustStatement.type } returns "other type"

        useCase(
            trustStatement = mockTrustStatement,
            actorDid = ACTOR_DID,
        ).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `A jwt missing the subject returns an error`() = runTest {
        every { mockTrustStatement.subject } returns null

        useCase(
            trustStatement = mockTrustStatement,
            actorDid = ACTOR_DID,
        ).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `A jwt with invalid subject returns an error`() = runTest {
        every { mockTrustStatement.subject } returns "other subject"

        useCase(
            trustStatement = mockTrustStatement,
            actorDid = ACTOR_DID,
        ).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `Validation maps errors from status check`() = runTest {
        val exception = IllegalStateException("did error")
        coEvery {
            mockValidateTrustStatementStatus(
                trustStatement = mockTrustStatement,
            )
        } returns Err(TrustRegistryError.Unexpected(exception))

        useCase(
            trustStatement = mockTrustStatement,
            actorDid = ACTOR_DID,
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
    fun `A jwt without entity names returns an empty map`() = runTest {
        every { mockTrustStatement.payloadJson } returns buildJsonObject {}

        val result = useCase(
            trustStatement = mockTrustStatement,
            actorDid = ACTOR_DID,
        ).assertOk()

        assertEquals(emptyMap<String, String>(), result.entityName)
    }

    @Test
    fun `A jwt where entity name without language exists returns the fallback language`() = runTest {
        every { mockTrustStatement.payloadJson } returns buildJsonObject {
            put(CLAIM_NAME_ENTITY_NAME, JsonPrimitive(ENTITY_NAME_EN))
        }

        val result = useCase(
            trustStatement = mockTrustStatement,
            actorDid = ACTOR_DID,
        ).assertOk()

        assertEquals(mapOf(DisplayLanguage.FALLBACK to ENTITY_NAME_EN), result.entityName)
    }

    @Test
    fun `A jwt missing the isStateActor claim returns an error`() = runTest {
        every { mockJWTClaimsSet.getBooleanClaim(CLAIM_NAME_STATE_ACTOR) } returns null

        useCase(
            trustStatement = mockTrustStatement,
            actorDid = ACTOR_DID,
        ).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    private fun setupDefaultMocks() {
        every { mockTrustStatement.type } returns TYPE
        every { mockTrustStatement.subject } returns ACTOR_DID
        every { mockTrustStatement.keyId } returns KEY_ID
        every { mockTrustStatement.algorithm } returns "ES256"
        every { mockTrustStatement.issuedAt } returns iat
        every { mockTrustStatement.expInstant } returns exp
        every { mockTrustStatement.payloadJson } returns payload

        every { mockTrustStatement.signedJwt } returns mockSignedJWT
        every { mockSignedJWT.header } returns mockJWSHeader
        every { mockJWSHeader.getCustomParam(CLAIM_NAME_PROFILE_VERSION) } returns PROFILE_VERSION
        every { mockSignedJWT.jwtClaimsSet } returns mockJWTClaimsSet
        every { mockJWTClaimsSet.getBooleanClaim(CLAIM_NAME_STATE_ACTOR) } returns IS_STATE_ACTOR
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
        const val ENTITY_LANGUAGE_EN = "en"
        const val ENTITY_NAME_EN = "entity name en"
        val ENTITY_NAME_MAP = mapOf(
            ENTITY_LANGUAGE_EN to ENTITY_NAME_EN
        )
        const val IS_STATE_ACTOR = true
        val registryIds = listOf(
            RegistryId(
                type = "registry type",
                value = "registry value",
            )
        )
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

        val payload = buildJsonObject {
            put("$CLAIM_NAME_ENTITY_NAME#$ENTITY_LANGUAGE_EN", JsonPrimitive(ENTITY_NAME_EN))
            put(CLAIM_NAME_REGISTRY_IDS, registryIds.toJsonArray())
        }

        private fun List<RegistryId>.toJsonArray(): JsonArray = SafeJsonTestInstance.json.encodeToJsonElement(value = this).jsonArray
    }
}

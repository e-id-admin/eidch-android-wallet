package ch.admin.foitt.wallet.platform.trustRegistry

import ch.admin.foitt.openid4vc.domain.model.SignatureAlgorithm
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.TokenStatusListProperties
import ch.admin.foitt.wallet.platform.database.domain.model.DisplayLanguage
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.NonComplianceTrustListStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.NonComplianceTrustListStatement.Companion.CLAIM_NAME_ACTOR
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.NonComplianceTrustListStatement.Companion.CLAIM_NAME_NON_COMPLIANT_ACTORS
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.NonComplianceTrustListStatement.Companion.CLAIM_NAME_REASON
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.NonComplianceTrustListStatement.Companion.TYPE
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustRegistryError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatementStatusResult
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatementV2.Companion.CLAIM_NAME_JTI
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatementV2.Companion.CLAIM_NAME_PROFILE_VERSION
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.IsTrustedDid
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateNonComplianceTrustListStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateTrustStatementJwt
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateTrustStatementStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation.ValidateNonComplianceTrustListStatementImpl
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
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class ValidateNonComplianceTrustListStatementImplTest {
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

    private lateinit var useCase: ValidateNonComplianceTrustListStatement

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        useCase = ValidateNonComplianceTrustListStatementImpl(
            validateTrustStatementJwt = mockValidateTrustStatementJwt,
            validateTrustStatementStatus = mockValidateTrustStatementStatus,
            isTrustedDid = mockIsTrustedDid,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `A valid NonComplianceTrustListStatement passes validation`() = runTest {
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
        assertEquals(credentialStatusProperties, result.status)
        assertEquals(nonCompliantActors, result.nonCompliantActors)
    }

    @Test
    fun `Validation maps jwt validation errors`() = runTest {
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
    fun `A jwt without non compliant actors returns an error`() = runTest {
        every { mockTrustStatement.payloadJson } returns buildJsonObject {}

        useCase(
            trustStatement = mockTrustStatement,
            actorDid = ACTOR_DID
        ).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `A jwt without actor returns an error`() = runTest {
        every { mockTrustStatement.payloadJson } returns payloadWithActor(
            actor = null,
            reason = JsonPrimitive(REASON),
        )

        useCase(
            trustStatement = mockTrustStatement,
            actorDid = ACTOR_DID
        ).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `A jwt without reason returns an error`() = runTest {
        every { mockTrustStatement.payloadJson } returns payloadWithActor(
            actor = ACTOR_DID,
            reason = null,
        )

        useCase(
            trustStatement = mockTrustStatement,
            actorDid = ACTOR_DID
        ).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `A jwt with null reason returns an error`() = runTest {
        every { mockTrustStatement.payloadJson } returns payloadWithActor(
            actor = ACTOR_DID,
            reason = JsonNull,
        )

        useCase(
            trustStatement = mockTrustStatement,
            actorDid = ACTOR_DID
        ).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `A jwt with object reason returns an error`() = runTest {
        every { mockTrustStatement.payloadJson } returns payloadWithActor(
            actor = ACTOR_DID,
            reason = buildJsonObject {},
        )

        useCase(
            trustStatement = mockTrustStatement,
            actorDid = ACTOR_DID
        ).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    private fun setupDefaultMocks() {
        every { mockTrustStatement.type } returns TYPE
        every { mockTrustStatement.keyId } returns KEY_ID
        every { mockTrustStatement.algorithm } returns "ES256"
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
        const val REASON = "reason"
        const val REASON_DE = "reason de"
        const val LOCALE_DE = "de"
        val nonCompliantActors = listOf(
            NonComplianceTrustListStatement.NonCompliantActor(
                actor = ACTOR_DID,
                reason = mapOf(
                    DisplayLanguage.FALLBACK to REASON,
                    LOCALE_DE to REASON_DE,
                )
            )
        )
        val payload = payloadWithActor(
            actor = ACTOR_DID,
            reason = JsonPrimitive(REASON),
            localizedReasons = mapOf(LOCALE_DE to REASON_DE),
        )

        private fun payloadWithActor(
            actor: String?,
            reason: JsonElement?,
            localizedReasons: Map<String, String> = emptyMap(),
        ): JsonObject = buildJsonObject {
            put(
                CLAIM_NAME_NON_COMPLIANT_ACTORS,
                buildJsonArray {
                    add(
                        buildJsonObject {
                            actor?.let { put(CLAIM_NAME_ACTOR, JsonPrimitive(it)) }
                            reason?.let { put(CLAIM_NAME_REASON, it) }
                            localizedReasons.forEach { (locale, translation) ->
                                put("$CLAIM_NAME_REASON#$locale", JsonPrimitive(translation))
                            }
                        }
                    )
                }
            )
        }
    }
}

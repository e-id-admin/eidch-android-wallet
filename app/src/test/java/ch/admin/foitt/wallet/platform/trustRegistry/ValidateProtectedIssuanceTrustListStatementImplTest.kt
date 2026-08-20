package ch.admin.foitt.wallet.platform.trustRegistry

import ch.admin.foitt.openid4vc.domain.model.SignatureAlgorithm
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.TokenStatusListProperties
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ProtectedIssuanceTrustListStatement.Companion.CLAIM_NAME_VCT_VALUES
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ProtectedIssuanceTrustListStatement.Companion.TYPE
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustRegistryError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatementStatusResult
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatementV2.Companion.CLAIM_NAME_JTI
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatementV2.Companion.CLAIM_NAME_PROFILE_VERSION
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.IsTrustedDid
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateProtectedIssuanceTrustListStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateTrustStatementJwt
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateTrustStatementStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation.ValidateProtectedIssuanceTrustListStatementImpl
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

class ValidateProtectedIssuanceTrustListStatementImplTest {
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

    private lateinit var useCase: ValidateProtectedIssuanceTrustListStatement

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        useCase = ValidateProtectedIssuanceTrustListStatementImpl(
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
    fun `A valid ProtectedIssuanceTrustListStatement passes validation`() = runTest {
        val result = useCase(mockTrustStatement).assertOk()

        assertEquals(TYPE, result.typ)
        assertEquals(SignatureAlgorithm.ES256, result.alg)
        assertEquals(KEY_ID, result.kid)
        assertEquals(PROFILE_VERSION, result.profileVersion)
        assertEquals(JTI, result.jti)
        assertEquals(IAT_SECONDS, result.iat)
        assertEquals(EXP_SECONDS, result.exp)
        assertEquals(credentialStatusProperties, result.status)
        assertEquals(vctValues, result.vctValues)
    }

    @Test
    fun `Validation maps jwt validation errors`() = runTest {
        val exception = IllegalStateException("jwt error")
        coEvery {
            mockValidateTrustStatementJwt(any())
        } returns Err(TrustRegistryError.Unexpected(exception))

        useCase(mockTrustStatement).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `A jwt missing the type returns an error`() = runTest {
        every { mockTrustStatement.type } returns null

        useCase(mockTrustStatement).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `A jwt with invalid type returns an error`() = runTest {
        every { mockTrustStatement.type } returns "other type"

        useCase(mockTrustStatement).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `Validation maps errors from status check`() = runTest {
        val exception = IllegalStateException("did error")
        coEvery {
            mockValidateTrustStatementStatus(mockTrustStatement)
        } returns Err(TrustRegistryError.Unexpected(exception))

        useCase(mockTrustStatement).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `Validation maps errors from isTrustedDid check`() = runTest {
        val exception = IllegalStateException("did error")
        coEvery {
            mockIsTrustedDid(any(), any())
        } returns Err(TrustRegistryError.Unexpected(exception))

        useCase(
            trustStatement = mockTrustStatement,
        ).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `A jwt without vct_values returns an error`() = runTest {
        every { mockTrustStatement.payloadJson } returns buildJsonObject {}

        useCase(mockTrustStatement).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `A jwt with invalid vct_values returns an error`() = runTest {
        every { mockTrustStatement.payloadJson } returns buildJsonObject {
            put(CLAIM_NAME_VCT_VALUES, JsonPrimitive("invalid"))
        }

        useCase(mockTrustStatement).assertErrorType(TrustRegistryError.Unexpected::class)
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
        const val VCT = "VCT"
        val vctValues = listOf(VCT)
        val payload = buildJsonObject {
            put(CLAIM_NAME_VCT_VALUES, vctValues.toJsonArray())
        }

        private fun List<String>.toJsonArray(): JsonArray = SafeJsonTestInstance.json.encodeToJsonElement(value = this).jsonArray
    }
}

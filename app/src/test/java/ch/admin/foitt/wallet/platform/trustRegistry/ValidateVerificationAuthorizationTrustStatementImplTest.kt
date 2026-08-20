package ch.admin.foitt.wallet.platform.trustRegistry

import ch.admin.foitt.openid4vc.domain.model.SignatureAlgorithm
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.TokenStatusListProperties
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustRegistryError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatementStatusResult
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatementV2.Companion.CLAIM_NAME_JTI
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatementV2.Companion.CLAIM_NAME_PROFILE_VERSION
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VerificationAuthorizationTrustStatement.Companion.CLAIM_NAME_AUTHORIZED_FIELDS
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VerificationAuthorizationTrustStatement.Companion.TYPE
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.IsTrustedDid
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateTrustStatementJwt
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateTrustStatementStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateVerificationAuthorizationTrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation.ValidateVerificationAuthorizationTrustStatementImpl
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
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant

class ValidateVerificationAuthorizationTrustStatementImplTest {

    @MockK
    private lateinit var mockValidateTrustStatementJwt: ValidateTrustStatementJwt

    @MockK
    private lateinit var mockValidateTrustStatementStatus: ValidateTrustStatementStatus

    @MockK
    private lateinit var mockIsTrustedDid: IsTrustedDid

    @MockK
    private lateinit var mockJwt: Jwt

    @MockK
    private lateinit var mockSignedJwt: SignedJWT

    @MockK
    private lateinit var mockClaimsSet: JWTClaimsSet

    private lateinit var useCase: ValidateVerificationAuthorizationTrustStatement

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        useCase = ValidateVerificationAuthorizationTrustStatementImpl(
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
    fun `A valid VerificationAuthorizationTrustStatement passes validation`() = runTest {
        val result = useCase(mockJwt, ACTOR_DID).assertOk()

        assertEquals(TYPE, result.typ)
        assertEquals(SignatureAlgorithm.ES256, result.alg)
        assertEquals(KEY_ID, result.kid)
        assertEquals(PROFILE_VERSION, result.profileVersion)
        assertEquals(JTI, result.jti)
        assertEquals(IAT_SECONDS, result.iat)
        assertEquals(EXP_SECONDS, result.exp)
        assertEquals(ACTOR_DID, result.sub)
        assertEquals(credentialStatusProperties, result.status)
        assertEquals(AUTHORIZED_FIELDS, result.authorizedFields)
    }

    @Test
    fun `Errors from validating jwt are mapped`() = runTest {
        val exception = IllegalStateException("jwt error")
        coEvery {
            mockValidateTrustStatementJwt(any())
        } returns Err(TrustRegistryError.Unexpected(exception))

        useCase(mockJwt, ACTOR_DID)
            .assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `A jwt missing the type returns an error`() = runTest {
        every { mockJwt.type } returns null

        useCase(mockJwt, ACTOR_DID)
            .assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `A jwt with invalid type returns an error`() = runTest {
        every { mockJwt.type } returns "other type"

        useCase(mockJwt, ACTOR_DID)
            .assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `A jwt missing the sub returns an error`() = runTest {
        every { mockJwt.subject } returns null

        useCase(mockJwt, ACTOR_DID)
            .assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `A jwt with invalid sub returns an error`() = runTest {
        every { mockJwt.subject } returns "other"

        useCase(mockJwt, ACTOR_DID)
            .assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `Validation maps errors from status check`() = runTest {
        coEvery { mockValidateTrustStatementStatus(any()) } returns Err(TrustRegistryError.Unexpected(Throwable()))

        useCase(mockJwt, ACTOR_DID)
            .assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `Validation maps errors from isTrustedDid check`() = runTest {
        val exception = IllegalStateException("did error")
        coEvery {
            mockIsTrustedDid(any(), any())
        } returns Err(TrustRegistryError.Unexpected(exception))

        useCase(
            trustStatement = mockJwt,
            actorDid = ACTOR_DID,
        ).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `A jwt without authorized fields returns an error`() = runTest {
        every { mockClaimsSet.getListClaim(any()) } returns emptyList()

        useCase(mockJwt, ACTOR_DID)
            .assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `A jwt with invalid authorized_fields type returns an error`() = runTest {
        every { mockClaimsSet.getListClaim(any()) } returns listOf(123)

        useCase(mockJwt, ACTOR_DID)
            .assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `A jwt without authorized_fields claim returns an error`() = runTest {
        every { mockClaimsSet.getListClaim(any()) } returns null

        useCase(mockJwt, ACTOR_DID)
            .assertErrorType(TrustRegistryError.Unexpected::class)
    }

    private fun setupDefaultMocks() {
        every { mockJwt.type } returns TYPE
        every { mockJwt.algorithm } returns "ES256"
        every { mockJwt.subject } returns ACTOR_DID
        every { mockJwt.issuedAt } returns iat
        every { mockJwt.expInstant } returns exp

        every { mockJwt.signedJwt } returns mockSignedJwt
        every { mockSignedJwt.jwtClaimsSet } returns mockClaimsSet
        every { mockSignedJwt.header.getCustomParam(CLAIM_NAME_PROFILE_VERSION) } returns PROFILE_VERSION

        every { mockClaimsSet.getStringClaim(CLAIM_NAME_JTI) } returns JTI
        every { mockClaimsSet.getListClaim(CLAIM_NAME_AUTHORIZED_FIELDS) } returns AUTHORIZED_FIELDS.toList()

        coEvery { mockValidateTrustStatementJwt(mockJwt) } returns Ok(Unit)
        coEvery {
            mockValidateTrustStatementStatus(mockJwt)
        } returns Ok(
            TrustStatementStatusResult(status = credentialStatusProperties, kid = KEY_ID)
        )
        coEvery { mockIsTrustedDid(KEY_ID, TYPE) } returns Ok(Unit)
    }

    private companion object {
        const val ACTOR_DID = "actorDid"
        const val KEY_ID = "kid"
        const val PROFILE_VERSION = "profile"
        const val JTI = "jti"
        const val IAT_SECONDS = 1L
        const val EXP_SECONDS = 2L
        val iat: Instant = Instant.ofEpochSecond(IAT_SECONDS)
        val exp: Instant = Instant.ofEpochSecond(EXP_SECONDS)

        val credentialStatusProperties = TokenStatusListProperties(
            statusList = TokenStatusListProperties.StatusList(
                index = 0,
                uri = "https://example.com/status"
            )
        )
        val AUTHORIZED_FIELDS = setOf("claim1")
    }
}

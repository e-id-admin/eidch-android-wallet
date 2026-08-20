package ch.admin.foitt.wallet.platform.trustRegistry

import ch.admin.foitt.openid4vc.domain.model.anycredential.Validity
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.jwt.JwtError
import ch.admin.foitt.openid4vc.domain.usecase.jwt.VerifyJwtSignatureFromDid
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustRegistryError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatementV2.Companion.CLAIM_NAME_JTI
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatementV2.Companion.CLAIM_NAME_PROFILE_VERSION
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ValidateTrustStatementJwt
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation.ValidateTrustStatementJwtImpl
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import com.squareup.wire.Instant
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ValidateTrustStatementJwtImplTest {
    @MockK
    private lateinit var mockVerifyJwtSignatureFromDid: VerifyJwtSignatureFromDid

    @MockK
    private lateinit var mockTrustStatement: Jwt

    @MockK
    private lateinit var mockSignedJWT: SignedJWT

    @MockK
    private lateinit var mockJWSHeader: JWSHeader

    @MockK
    private lateinit var mockJWTClaimsSet: JWTClaimsSet

    private lateinit var useCase: ValidateTrustStatementJwt

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        useCase = ValidateTrustStatementJwtImpl(
            verifyJwtSignatureFromDid = mockVerifyJwtSignatureFromDid,
        )

        every { mockTrustStatement.algorithm } returns "ES256"
        every { mockTrustStatement.keyId } returns KEY_ID
        every { mockTrustStatement.issuedAt } returns Instant.now()
        every { mockTrustStatement.expInstant } returns Instant.now()
        every { mockTrustStatement.jwtValidity } returns Validity.Valid
        every { mockTrustStatement.signedJwt } returns mockSignedJWT
        every { mockSignedJWT.header } returns mockJWSHeader
        every { mockJWSHeader.getCustomParam(CLAIM_NAME_PROFILE_VERSION) } returns PROFILE_VERSION
        every { mockSignedJWT.jwtClaimsSet } returns mockJWTClaimsSet
        every { mockJWTClaimsSet.getStringClaim(CLAIM_NAME_JTI) } returns JTI

        coEvery {
            mockVerifyJwtSignatureFromDid(
                kid = KEY_ID,
                jwt = mockTrustStatement,
            )
        } returns Ok(Unit)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `A trust statement jwt is validated correctly`() = runTest {
        useCase(mockTrustStatement).assertOk()
    }

    @Test
    fun `A trust statement with invalid algorithm returns an error`() = runTest {
        every { mockTrustStatement.algorithm } returns "invalid alg"

        useCase(mockTrustStatement).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `A trust statement with missing kid returns an error`() = runTest {
        every { mockTrustStatement.keyId } returns "missing kid"

        useCase(mockTrustStatement).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `A trust statement where jwt signature validation fails returns an error`() = runTest {
        val exception = IllegalStateException("jwt error")
        coEvery {
            mockVerifyJwtSignatureFromDid(any(), any())
        } returns Err(JwtError.Unexpected(exception))

        useCase(mockTrustStatement).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `A trust statement missing the profile version returns an error`() = runTest {
        every { mockJWSHeader.getCustomParam(CLAIM_NAME_PROFILE_VERSION) } returns null

        useCase(mockTrustStatement).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `A trust statement with invalid profile version returns an error`() = runTest {
        every { mockJWSHeader.getCustomParam(CLAIM_NAME_PROFILE_VERSION) } returns "invalid profile version"

        useCase(mockTrustStatement).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `A trust statement missing jti returns an error`() = runTest {
        every { mockJWTClaimsSet.getStringClaim(CLAIM_NAME_JTI) } returns null

        useCase(mockTrustStatement).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `A trust statement with invalid jti returns an error`() = runTest {
        every { mockJWTClaimsSet.getStringClaim(CLAIM_NAME_JTI) } returns "invalid jti"

        useCase(mockTrustStatement).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `A trust statement missing iat returns an error`() = runTest {
        every { mockTrustStatement.issuedAt } returns null

        useCase(mockTrustStatement).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `A trust statement missing exp returns an error`() = runTest {
        every { mockTrustStatement.expInstant } returns null

        useCase(mockTrustStatement).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `A trust statement with invalid validity returns an error`() = runTest {
        every { mockTrustStatement.jwtValidity } returns Validity.Expired(Instant.now())

        useCase(mockTrustStatement).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    private companion object {
        const val KEY_ID = "key id"
        const val PROFILE_VERSION = "swiss-profile-trust:versionNumber"
        const val JTI = "12345678-1234-4123-a123-123456789012"
    }
}

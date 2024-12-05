package ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.implementation

import ch.admin.eid.didresolver.didtoolbox.DidDoc
import ch.admin.eid.didresolver.didtoolbox.Jwk
import ch.admin.eid.didresolver.didtoolbox.VerificationMethod
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtError
import ch.admin.foitt.openid4vc.domain.usecase.ResolveDid
import ch.admin.foitt.openid4vc.domain.usecase.implementation.VerifyJwtSignatureImpl
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.PublicKeyVerifier
import ch.admin.foitt.openid4vc.util.assertErrorType
import ch.admin.foitt.openid4vc.util.assertOk
import com.github.michaelbull.result.Ok
import com.nimbusds.jwt.SignedJWT
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class VerifyJwtImplTest {
    @MockK
    private lateinit var mockPublicKeyVerifier: PublicKeyVerifier

    @MockK
    private lateinit var mockResolveDid: ResolveDid

    @MockK
    private lateinit var mockSignedJWT: SignedJWT

    @MockK
    private lateinit var mockDidDoc: DidDoc

    private lateinit var useCase: VerifyJwtSignatureImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery { mockResolveDid.invoke(any()) } returns Ok(mockDidDoc)
        every { mockDidDoc.getVerificationMethod() } returns mockVerificationMethods
        every { mockPublicKeyVerifier.matchSignature(any(), any()) } returns true

        useCase = VerifyJwtSignatureImpl(
            publicKeyVerifier = mockPublicKeyVerifier,
            resolveDid = mockResolveDid,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Verifying jwt which is valid returns Ok`(): Unit = runTest {
        val result = useCase(ISSUER, mockSignedJWT)

        result.assertOk()
    }

    @Test
    fun `Verifying jwt which has one matching public key from list returns Ok`(): Unit = runTest {
        every { mockPublicKeyVerifier.matchSignature(any(), any()) } returns false
        every { mockPublicKeyVerifier.matchSignature(mockJwk2, mockSignedJWT) } returns true
        every { mockDidDoc.getVerificationMethod() } returns listOf(mockVerificationMethod1, mockVerificationMethod2)

        val result = useCase(ISSUER, mockSignedJWT)

        result.assertOk()
    }

    @Test
    fun `Verifying jwt which has no matching public key from list returns InvalidJwt`(): Unit = runTest {
        every { mockPublicKeyVerifier.matchSignature(any(), any()) } returns false
        every { mockPublicKeyVerifier.matchSignature(mockJwk1, mockSignedJWT) } returns true
        every { mockDidDoc.getVerificationMethod() } returns listOf(mockVerificationMethod2, mockVerificationMethod3)

        val result = useCase(ISSUER, mockSignedJWT)

        result.assertErrorType(VcSdJwtError.InvalidJwt::class)
    }

    @Test
    fun `Verifying jwt with only null jwk keys returns InvalidJwt`(): Unit = runTest {
        every { mockDidDoc.getVerificationMethod() } returns listOf(mockVerificationMethod3)

        val result = useCase(ISSUER, mockSignedJWT)

        result.assertErrorType(VcSdJwtError.InvalidJwt::class)
    }

    @Test
    fun `Verifying jwt with empty public key list returns InvalidJwt`(): Unit = runTest {
        every { mockDidDoc.getVerificationMethod() } returns listOf()

        val result = useCase(ISSUER, mockSignedJWT)

        result.assertErrorType(VcSdJwtError.InvalidJwt::class)
    }

    companion object {
        const val ISSUER = "issuer"
        private val mockJwk1 = Jwk(
            alg = "alg",
            kid = "kid",
            kty = "kty",
            crv = "crv",
            x = "x",
            y = "y",
        )

        private val mockJwk2 = mockJwk1.copy(crv = "crv2", x = "x2", y = "y2")

        private val mockVerificationMethod1 = VerificationMethod(
            id = "id1",
            verificationType = "type",
            controller = "controller",
            publicKeyJwk = mockJwk1,
            publicKeyMultibase = "multibase",
        )
        private val mockVerificationMethod2 = mockVerificationMethod1.copy(id = "id2", publicKeyJwk = mockJwk2)

        private val mockVerificationMethod3 = mockVerificationMethod1.copy(id = "id3", publicKeyJwk = null)

        private val mockVerificationMethods = listOf(
            mockVerificationMethod1,
            mockVerificationMethod1,
            mockVerificationMethod1,
            mockVerificationMethod1,
        )
    }
}

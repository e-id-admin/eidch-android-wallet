package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.openid4vc.domain.usecase.CreateDidJwk
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockCredentialOffer.CREDENTIAL_ISSUER
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockCredentialOffer.C_NONCE
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockKeyPairs.INVALID_KEY_PAIR
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockKeyPairs.VALID_KEY_PAIR
import ch.admin.foitt.openid4vc.util.assertErrorType
import ch.admin.foitt.openid4vc.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.get
import com.nimbusds.jose.crypto.ECDSAVerifier
import com.nimbusds.jwt.SignedJWT
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.security.interfaces.ECPublicKey

class CreateCredentialRequestProofJwtImplTest {

    private val testDispatcher = StandardTestDispatcher()

    @MockK
    private lateinit var mockCreateDidJwk: CreateDidJwk

    private lateinit var createCredentialRequestProofJwtUseCase: CreateCredentialRequestProofJwtImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        coEvery {
            mockCreateDidJwk(any(), any(), false)
        } returns Ok(jwk)

        createCredentialRequestProofJwtUseCase = CreateCredentialRequestProofJwtImpl(createDidJwk = mockCreateDidJwk)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `a created proof jwt should have a valid signature`() = runTest(testDispatcher) {
        val keyPair = VALID_KEY_PAIR
        val proofJwt = createCredentialRequestProofJwtUseCase(
            keyPair = keyPair,
            issuer = CREDENTIAL_ISSUER,
            cNonce = C_NONCE
        )

        proofJwt.assertOk()
        val jwt = proofJwt.get()?.jwt
        val publicKey = keyPair.keyPair.public as ECPublicKey
        val verifier = ECDSAVerifier(publicKey)
        assertTrue(SignedJWT.parse(jwt).verify(verifier), "")
    }

    @Test
    fun `a created proof jwt without nonce should have a valid signature`() = runTest(testDispatcher) {
        val keyPair = VALID_KEY_PAIR
        val proofJwt = createCredentialRequestProofJwtUseCase(
            keyPair = keyPair,
            issuer = CREDENTIAL_ISSUER,
            cNonce = null
        )

        proofJwt.assertOk()
        val jwt = proofJwt.get()?.jwt
        val publicKey = keyPair.keyPair.public as ECPublicKey
        val verifier = ECDSAVerifier(publicKey)
        assertTrue(SignedJWT.parse(jwt).verify(verifier), "")
    }

    @Test
    fun `creating a proof jwt with an invalid private key should return an unexpected error`() = runTest(testDispatcher) {
        val keyPair = INVALID_KEY_PAIR
        val proofJwt = createCredentialRequestProofJwtUseCase(
            keyPair = keyPair,
            issuer = CREDENTIAL_ISSUER,
            cNonce = C_NONCE
        )

        proofJwt.assertErrorType(CredentialOfferError.Unexpected::class)
    }

    @Test
    fun `should return an unexpected error when header jwk creation fails`() = runTest(testDispatcher) {
        coEvery {
            mockCreateDidJwk(any(), any(), false)
        } returns Err(CredentialOfferError.Unexpected(null))

        val proofJwt = createCredentialRequestProofJwtUseCase(
            keyPair = VALID_KEY_PAIR,
            issuer = CREDENTIAL_ISSUER,
            cNonce = C_NONCE
        )

        proofJwt.assertErrorType(CredentialOfferError.Unexpected::class)
    }

    private val jwk = """
    {
        "crv": "P-256",
        "kty": "EC",
        "x": "Q7HpY9d8GlvGqfHtw-9jLLPZaIX9Lc91Q-Hfsz_WbBo",
        "y": "647ttGFFCBoy17NspJszfIW2pEwuzqdep69Av5Mprb8"
    }
    """
}

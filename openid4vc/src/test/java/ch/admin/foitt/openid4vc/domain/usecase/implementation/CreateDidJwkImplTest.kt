package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.openid4vc.domain.usecase.CreateDidJwk
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockKeyPairs.UNSUPPORTED_KEY_PAIR
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockKeyPairs.VALID_KEY_PAIR
import ch.admin.foitt.openid4vc.util.assertErrorType
import ch.admin.foitt.openid4vc.util.assertOk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CreateDidJwkImplTest {

    private lateinit var createDidJwk: CreateDidJwk

    @BeforeEach
    fun setUp() {
        createDidJwk = CreateDidJwkImpl()
    }

    @Test
    fun `creating a did jwk successfully returns the did jwk`() = runTest {
        val keyPair = VALID_KEY_PAIR

        val result = createDidJwk(
            algorithm = keyPair.algorithm,
            keyPair = keyPair.keyPair,
        ).assertOk()

        assertTrue(result.startsWith("did:jwk:"))
    }

    @Test
    fun `creating a did jwk with an unsupported key pair should return an invalid cryptographic suite error`() = runTest {
        val keyPair = UNSUPPORTED_KEY_PAIR

        createDidJwk(
            algorithm = keyPair.algorithm,
            keyPair = keyPair.keyPair,
        ).assertErrorType(
            CredentialOfferError.UnsupportedCryptographicSuite::class
        )
    }
}

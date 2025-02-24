package ch.admin.foitt.openid4vc.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.KeyPairError
import ch.admin.foitt.openid4vc.domain.usecase.GetKeyPair
import ch.admin.foitt.openid4vc.util.assertErrorType
import ch.admin.foitt.openid4vc.util.assertOk
import io.mockk.MockKAnnotations
import io.mockk.Runs
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockkStatic
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import net.bytebuddy.matcher.ElementMatchers.any
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.IOException
import java.security.KeyStore
import java.security.PrivateKey
import java.security.PublicKey
import java.security.cert.Certificate

class GetKeyPairImplTest {

    private val testDispatcher = StandardTestDispatcher()

    @MockK
    private lateinit var mockKeyStore: KeyStore

    @MockK
    private lateinit var mockKeyStoreEntry: KeyStore.PrivateKeyEntry

    @MockK
    private lateinit var mockCertificate: Certificate

    @MockK
    private lateinit var mockPrivateKey: PrivateKey

    @MockK
    private lateinit var mockPublicKey: PublicKey

    private lateinit var useCase: GetKeyPair

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        mockkStatic(KeyStore::class)
        mockkStatic(KeyStore.Entry::class)

        every { KeyStore.getInstance(any()) } returns mockKeyStore
        every { mockKeyStore.load(any()) } just Runs
        every { mockKeyStoreEntry.certificate } returns mockCertificate
        every { mockKeyStoreEntry.privateKey } returns mockPrivateKey

        every { mockCertificate.publicKey } returns mockPublicKey

        useCase = GetKeyPairImpl(
            defaultDispatcher = testDispatcher
        )
    }

    @Test
    fun `get existing key returns ok`() = runTest(testDispatcher) {
        val alias = "alias"
        every { mockKeyStore.getEntry(alias, any()) } returns mockKeyStoreEntry
        useCase(keyId = alias, provider = "some provider").assertOk()
    }

    @Test
    fun `get non-existing key returns KeyPairError_NotFound`() = runTest(testDispatcher) {
        every { mockKeyStore.getEntry("alias", any()) } returns null
        useCase(keyId = "alias", provider = "some provider").assertErrorType(KeyPairError.NotFound::class)
    }

    @Test
    fun `loading non-existing key store returns KeyPairError_Unexpected`() = runTest(testDispatcher) {
        every { mockKeyStore.load(any()) } throws (IOException())
        useCase(keyId = "alias", provider = "some provider").assertErrorType(KeyPairError.Unexpected::class)
    }
}

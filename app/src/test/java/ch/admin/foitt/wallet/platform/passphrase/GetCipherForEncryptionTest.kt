package ch.admin.foitt.wallet.platform.passphrase

import android.annotation.SuppressLint
import ch.admin.foitt.wallet.platform.keystoreCrypto.domain.model.GetCipherForEncryptionError
import ch.admin.foitt.wallet.platform.keystoreCrypto.domain.model.GetOrCreateSecretKeyError
import ch.admin.foitt.wallet.platform.keystoreCrypto.domain.model.KeystoreKeyConfig
import ch.admin.foitt.wallet.platform.keystoreCrypto.domain.usecase.GetCipherForEncryption
import ch.admin.foitt.wallet.platform.keystoreCrypto.domain.usecase.GetOrCreateSecretKey
import ch.admin.foitt.wallet.platform.keystoreCrypto.domain.usecase.implementation.GetCipherForEncryptionImpl
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.mockkStatic
import io.mockk.runs
import io.mockk.unmockkAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.security.spec.AlgorithmParameterSpec
import javax.crypto.Cipher
import javax.crypto.SecretKey

class GetCipherForEncryptionTest {

    @MockK
    private lateinit var mockGetOrCreateSecretKey: GetOrCreateSecretKey

    @MockK
    private lateinit var mockCipher: Cipher

    @MockK
    private lateinit var mockSecretKey: SecretKey

    @MockK
    private lateinit var mockPassConfig: KeystoreKeyConfig

    private lateinit var testedUseCase: GetCipherForEncryption

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        mockkStatic(Cipher::class)

        coEvery { mockGetOrCreateSecretKey(any()) } returns Ok(mockSecretKey)

        coEvery { mockSecretKey.format } returns "AES"

        coEvery { Cipher.getInstance(any()) } returns mockCipher

        coEvery { mockCipher.init(any(), any<SecretKey>()) } just runs
        coEvery { mockCipher.init(any(), any<SecretKey>(), any<AlgorithmParameterSpec>()) } just runs

        coEvery { mockPassConfig.encryptionTransformation } returns "AES/GCM/NoPadding"
        coEvery { mockPassConfig.gcmAuthTagLength } returns 128
        coEvery { mockPassConfig.randomizedEncryptionRequired } returns false

        testedUseCase = GetCipherForEncryptionImpl(
            getOrCreateSecretKey = mockGetOrCreateSecretKey,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @SuppressLint("CheckResult")
    @Test
    fun `Getting an initialized Cipher with existing IV follow specific steps`() {
        val cipherResult = testedUseCase(
            keystoreKeyConfig = mockPassConfig,
            initializationVector = byteArrayOf(0, 1),
        )

        cipherResult.assertOk()

        coVerify(exactly = 1) {
            mockGetOrCreateSecretKey(any())
            mockCipher.init(any(), any<SecretKey>(), any<AlgorithmParameterSpec>())
        }
    }

    @SuppressLint("CheckResult")
    @Test
    fun `Getting an initialized Cipher without existing IV follow specific steps`() {
        coEvery { mockPassConfig.randomizedEncryptionRequired } returns true

        val cipherResult = testedUseCase(
            keystoreKeyConfig = mockPassConfig,
            initializationVector = null,
        )

        cipherResult.assertOk()

        coVerify(exactly = 1) {
            mockGetOrCreateSecretKey(any())
            mockCipher.init(any(), any<SecretKey>())
        }
    }

    @Test
    fun `Getting an initialized Cipher with existing IV but randomizedEncryptionRequired should fail`() {
        coEvery { mockPassConfig.randomizedEncryptionRequired } returns true

        val cipherResult = testedUseCase(
            keystoreKeyConfig = mockPassConfig,
            initializationVector = byteArrayOf(0, 1),
        )

        cipherResult.assertErrorType(GetCipherForEncryptionError.Unexpected::class)
    }

    @Test
    fun `A Cipher exception returns a failure`() {
        val exception = NoSuchAlgorithmException("Algo exception")
        coEvery { Cipher.getInstance(any()) } throws exception
        val cipherResult = testedUseCase(
            keystoreKeyConfig = mockPassConfig,
            initializationVector = byteArrayOf(0, 1),
        )

        cipherResult.assertErrorType(GetCipherForEncryptionError.Unexpected::class)
    }

    @Test
    fun `A SecretKey exception returns a failure`() {
        val keyError = GetOrCreateSecretKeyError.Unexpected(KeyStoreException("SecretKey exception"))
        coEvery { mockGetOrCreateSecretKey(any()) } returns Err(keyError)
        val cipherResult = testedUseCase(
            keystoreKeyConfig = mockPassConfig,
            initializationVector = byteArrayOf(0, 1),
        )

        cipherResult.assertErrorType(GetCipherForEncryptionError.Unexpected::class)
    }
}

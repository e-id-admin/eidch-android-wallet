package ch.admin.foitt.wallet.platform.payloadEncryption.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialRequestEncryption
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialResponseEncryption
import ch.admin.foitt.openid4vc.domain.model.payloadEncryption.PayloadEncryption
import ch.admin.foitt.openid4vc.domain.model.payloadEncryption.PayloadEncryptionKeyPair
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.model.PayloadEncryptionError
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.usecase.CreatePayloadEncryptionKeyPair
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.usecase.GetPayloadEncryption
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetPayloadEncryptionImplTest {
    @MockK
    private lateinit var mockCreatePayloadEncryptionKeyPair: CreatePayloadEncryptionKeyPair

    @MockK
    private lateinit var mockPayloadEncryptionKeyPair: PayloadEncryptionKeyPair

    @MockK
    private lateinit var mockRequestEncryption: CredentialRequestEncryption

    @MockK
    private lateinit var mockResponseEncryption: CredentialResponseEncryption

    private lateinit var useCase: GetPayloadEncryption

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = GetPayloadEncryptionImpl(
            createPayloadEncryptionKeyPair = mockCreatePayloadEncryptionKeyPair,
        )

        coEvery {
            mockCreatePayloadEncryptionKeyPair(mockResponseEncryption)
        } returns Ok(mockPayloadEncryptionKeyPair)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `With both requestEncryption and responseEncryption provided return payload encryption`() = runTest {
        val result = useCase(mockRequestEncryption, mockResponseEncryption).assertOk()

        val expected = PayloadEncryption(
            requestEncryption = mockRequestEncryption,
            responseEncryption = mockResponseEncryption,
            responseEncryptionKeyPair = mockPayloadEncryptionKeyPair,
        )

        assertEquals(expected, result)
    }

    @Test
    fun `Error during key pair generation is mapped`() = runTest {
        val exception = IllegalStateException("key pair generate error")
        coEvery {
            mockCreatePayloadEncryptionKeyPair(mockResponseEncryption)
        } returns Err(PayloadEncryptionError.Unexpected(exception))

        useCase(mockRequestEncryption, mockResponseEncryption).assertErrorType(PayloadEncryptionError.Unexpected::class)
    }
}

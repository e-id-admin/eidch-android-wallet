package ch.admin.foitt.wallet.platform.eIdApplicationProcess

import ch.admin.foitt.openid4vc.domain.model.KeyPairError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.openid4vc.domain.usecase.CreateDPoPProofJwt
import ch.admin.foitt.openid4vc.domain.usecase.GetHardwareKeyPair
import ch.admin.foitt.openid4vc.utils.Constants
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestation
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation.CreateAutoVerificationDPoPImpl
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URL
import java.security.KeyPair

class CreateAutoVerificationDPoPImplTest {
    @MockK
    private lateinit var mockGetHardwareKeyPair: GetHardwareKeyPair

    @MockK
    private lateinit var mockCreateDPoPProofJwt: CreateDPoPProofJwt

    private val requestUrl = URL("https://example.com")
    private val accessToken = "accessToken"
    private val requestBody = byteArrayOf(1, 2, 3)

    @MockK
    private lateinit var mockKeyPair: KeyPair

    private val testDPoP = "testDPoP"

    private lateinit var useCase: CreateAutoVerificationDPoPImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = CreateAutoVerificationDPoPImpl(
            getHardwareKeyPair = mockGetHardwareKeyPair,
            createDPoPProofJwt = mockCreateDPoPProofJwt,
        )

        coEvery {
            mockGetHardwareKeyPair(any(), any())
        } returns Ok(mockKeyPair)
        coEvery {
            mockCreateDPoPProofJwt(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
            )
        } returns Ok(testDPoP)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Creating an AutoVerification DPoP follows specific steps`() = runTest {
        val result = useCase(
            url = requestUrl,
            accessToken = accessToken,
            requestBody = requestBody,
        ).assertOk()
        assertEquals(testDPoP, result)

        coVerifyOrder {
            mockGetHardwareKeyPair(ClientAttestation.KEY_ALIAS, Constants.ANDROID_KEY_STORE)
            mockCreateDPoPProofJwt(
                method = "POST",
                url = requestUrl,
                keyPair = any(),
                nonce = null,
                accessToken = accessToken,
                requestBody = requestBody,
                keyAttestationJwt = any(),
            )
        }
    }

    @Test
    fun `A GetHardwareKeypair error is propagated`() = runTest {
        val exception = Exception("HardwareKeypair error")
        coEvery {
            mockGetHardwareKeyPair(any(), any())
        } returns Err(KeyPairError.Unexpected(exception))

        val result = useCase(
            url = requestUrl,
            accessToken = accessToken,
            requestBody = requestBody,
        ).assertErrorType(EIdRequestError.Unexpected::class)

        assertEquals(exception, result.cause)
    }

    @Test
    fun `A CreateDPoPProofJwt error is propagated`() = runTest {
        val exception = Exception("DPoP error")
        coEvery {
            mockCreateDPoPProofJwt(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
            )
        } returns Err(CredentialOfferError.Unexpected(exception))

        val result = useCase(
            url = requestUrl,
            accessToken = accessToken,
            requestBody = requestBody,
        ).assertErrorType(EIdRequestError.Unexpected::class)

        assertEquals(exception, result.cause)
    }
}

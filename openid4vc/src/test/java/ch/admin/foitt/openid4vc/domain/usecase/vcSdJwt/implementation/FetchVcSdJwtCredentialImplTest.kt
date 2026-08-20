package ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.implementation

import ch.admin.foitt.openid4vc.domain.model.BatchCredential
import ch.admin.foitt.openid4vc.domain.model.BatchCredentialItem
import ch.admin.foitt.openid4vc.domain.model.VerifiableCredential
import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyVerifiedBatchCredential
import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyVerifiedCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.openid4vc.domain.model.payloadEncryption.PayloadEncryption
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtError
import ch.admin.foitt.openid4vc.domain.usecase.FetchVerifiableCredential
import ch.admin.foitt.openid4vc.domain.usecase.implementation.mock.MockCredentialOffer
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.FetchVcSdJwtCredential
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.VerifyVcSdJwtBatchConsistency
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.VerifyVcSdJwtSignature
import ch.admin.foitt.openid4vc.util.assertErrorType
import ch.admin.foitt.openid4vc.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
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

class FetchVcSdJwtCredentialImplTest {
    @MockK
    private lateinit var mockFetchVerifiableCredential: FetchVerifiableCredential

    @MockK
    private lateinit var mockVerifyVcSdJwtSignature: VerifyVcSdJwtSignature

    @MockK
    private lateinit var mockVerifyVcSdJwtBatchConsistency: VerifyVcSdJwtBatchConsistency

    @MockK
    private lateinit var mockPayloadEncryption: PayloadEncryption

    private lateinit var useCase: FetchVcSdJwtCredential

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = FetchVcSdJwtCredentialImpl(
            fetchVerifiableCredential = mockFetchVerifiableCredential,
            verifyVcSdJwtSignature = mockVerifyVcSdJwtSignature,
            verifyVcSdJwtBatchConsistency = mockVerifyVcSdJwtBatchConsistency,
        )

        initDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Fetching a vc sd jwt credential with valid params returns a VcSdJwtCredential`() = runTest {
        val credential = useCase(
            verifiableCredentialParams = MockCredentialOffer.verifiableCredentialParamsWithoutBinding,
            bindingKeyPairs = null,
            payloadEncryption = mockPayloadEncryption,
        ).assertOk() as AnyVerifiedCredential

        assertEquals(null, credential.vcSdJwtCredential.keyBinding)
        assertEquals(VALID_JWT, credential.vcSdJwtCredential.payload)
    }

    @Test
    fun `Fetching a vc sd jwt credential where the jwt signature validation fails returns an error`() = runTest {
        coEvery {
            mockVerifyVcSdJwtSignature(any(), any(), any())
        } returns Err(VcSdJwtError.InvalidJwt)

        useCase(
            verifiableCredentialParams = MockCredentialOffer.verifiableCredentialParamsWithoutBinding,
            bindingKeyPairs = null,
            payloadEncryption = mockPayloadEncryption,
        ).assertErrorType(CredentialOfferError.IntegrityCheckFailed::class)
    }

    @Test
    fun `Fetching a deferred vc sd jwt credential returns a deferred credential`() = runTest {
        coEvery {
            mockFetchVerifiableCredential.invoke(
                verifiableCredentialParams = MockCredentialOffer.verifiableCredentialParamsWithoutBinding,
                credentialBindingKeyPairs = null,
                payloadEncryption = mockPayloadEncryption
            )
        } returns Ok(MockCredentialOffer.validDeferredCredential)

        val deferredCredential = useCase(
            verifiableCredentialParams = MockCredentialOffer.verifiableCredentialParamsWithoutBinding,
            bindingKeyPairs = null,
            payloadEncryption = mockPayloadEncryption
        ).assertOk()

        assertEquals(MockCredentialOffer.validDeferredCredential, deferredCredential)
    }

    @Test
    fun `Fetching a consistent vc sd jwt batch returns a verified batch credential`() = runTest {
        coEvery {
            mockFetchVerifiableCredential.invoke(
                verifiableCredentialParams = MockCredentialOffer.verifiableCredentialParamsWithoutBinding,
                credentialBindingKeyPairs = null,
                payloadEncryption = mockPayloadEncryption
            )
        } returns Ok(createBatchCredential(listOf(VALID_JWT, VALID_JWT)))

        val batchCredential = useCase(
            verifiableCredentialParams = MockCredentialOffer.verifiableCredentialParamsWithoutBinding,
            bindingKeyPairs = null,
            payloadEncryption = mockPayloadEncryption,
        ).assertOk() as AnyVerifiedBatchCredential

        assertEquals(2, batchCredential.vcSdJwtCredentials.size)
    }

    @Test
    fun `Fetching a vc sd jwt batch that fails the consistency check returns an error`() = runTest {
        coEvery {
            mockFetchVerifiableCredential.invoke(
                verifiableCredentialParams = MockCredentialOffer.verifiableCredentialParamsWithoutBinding,
                credentialBindingKeyPairs = null,
                payloadEncryption = mockPayloadEncryption
            )
        } returns Ok(createBatchCredential(listOf(VALID_JWT, VALID_JWT)))

        every {
            mockVerifyVcSdJwtBatchConsistency(any())
        } returns Err(VcSdJwtError.BatchConsistencyValidationFailed)

        useCase(
            verifiableCredentialParams = MockCredentialOffer.verifiableCredentialParamsWithoutBinding,
            bindingKeyPairs = null,
            payloadEncryption = mockPayloadEncryption,
        ).assertErrorType(CredentialOfferError.IntegrityCheckFailed::class)
    }

    private fun initDefaultMocks() {
        coEvery {
            mockFetchVerifiableCredential.invoke(
                verifiableCredentialParams = MockCredentialOffer.verifiableCredentialParamsWithoutBinding,
                credentialBindingKeyPairs = null,
                payloadEncryption = mockPayloadEncryption
            )
        } returns Ok(createVerifiableCredential(VALID_JWT))

        coEvery {
            mockVerifyVcSdJwtSignature(any(), any(), any())
        } returns Ok(validSdJwt)

        every {
            mockVerifyVcSdJwtBatchConsistency(any())
        } returns Ok(Unit)
    }

    private fun createVerifiableCredential(jwt: String) = VerifiableCredential(
        accessToken = "accessToken",
        refreshToken = "refreshToken",
        dpopKeyBinding = null,
        credential = jwt,
        keyBinding = null
    )

    private fun createBatchCredential(jwts: List<String>) = BatchCredential(
        accessToken = "accessToken",
        refreshToken = null,
        dpopKeyBinding = null,
        credentials = jwts.map { jwt ->
            BatchCredentialItem(
                credential = jwt,
                keyBinding = null
            )
        },
    )

    private val validSdJwt = createVcSdJwtCredential(VALID_JWT)

    private fun createVcSdJwtCredential(jwt: String) = VcSdJwtCredential(
        1L,
        keyBinding = null,
        payload = jwt,
    )

    private companion object {
        const val VALID_JWT =
            "eyJhbGciOiJFUzI1NiIsInR5cCI6InR5cGUiLCJraWQiOiJrZXlJZCJ9.eyJpc3MiOiJpc3N1ZXIiLCJleHAiOjE5MjQ5ODgzOTksImlhdCI6MCwibmJmIjoxLCJ2Y3QiOiJ2Y3QifQ.XV3KwWV9EKACPBsD7IGuTIXcEp6ur0rNmzd5WAXD6FrRLB0iAnEXKNWt3pSYL_MQHx3t-W1splQzBGwjgZePdQ~"
    }
}

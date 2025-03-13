package ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.InputDescriptor
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.InputDescriptorFormat
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GetAnyCredentials
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CredentialPresentationError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.PresentationRequestField
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.GetRequestedFields
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetCompatibleCredentialsImplTest {

    @MockK
    private lateinit var mockGetAnyCredentials: GetAnyCredentials

    @MockK
    private lateinit var mockGetRequestedFields: GetRequestedFields

    @MockK
    private lateinit var mockCredential: AnyCredential

    @MockK
    private lateinit var mockCredential2: AnyCredential

    private lateinit var useCase: GetCompatibleCredentialsImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = GetCompatibleCredentialsImpl(mockGetAnyCredentials, mockGetRequestedFields)

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Getting compatible credentials with no credential returns empty list`() = runTest {
        setupDefaultMocks(credentials = emptyList())

        val result = useCase(inputDescriptors).assertOk()

        assertEquals(0, result.size)
    }

    @Test
    fun `Getting compatible credentials with two matching credentials returns both ids and their fields`() = runTest {
        setupDefaultMocks(
            credentials = listOf(mockCredential, mockCredential2),
            requestedFields = requestedFields,
            requestedFields2 = requestedFields2,
        )

        val result = useCase(inputDescriptors).assertOk()

        assertEquals(2, result.size)
        assertEquals(CREDENTIAL_ID, result[0].credentialId)
        assertEquals(requestedFields, result[0].requestedFields)

        assertEquals(CREDENTIAL_ID_2, result[1].credentialId)
        assertEquals(requestedFields2, result[1].requestedFields)
    }

    @Test
    fun `Getting compatible credentials with two credentials where one matches returns the matched credential id and the fields`() = runTest {
        setupDefaultMocks(
            credentials = listOf(mockCredential, mockCredential2),
            requestedFields = emptyList(),
            requestedFields2 = requestedFields2,
        )

        val result = useCase(inputDescriptors).assertOk()

        assertEquals(1, result.size)
        assertEquals(CREDENTIAL_ID_2, result[0].credentialId)
        assertEquals(requestedFields2, result[0].requestedFields)
    }

    @Test
    fun `Getting compatible credentials where the credential has a non-matching format returns an empty list`() = runTest {
        setupDefaultMocks(credentials = listOf(mockCredential))
        every { mockCredential.format } returns CredentialFormat.UNKNOWN

        val result = useCase(inputDescriptors).assertOk()
        assertEquals(0, result.size)
    }

    @Test
    fun `Getting compatible credentials with two credentials where none matches returns empty list`() = runTest {
        setupDefaultMocks(
            credentials = listOf(mockCredential, mockCredential2),
            requestedFields = emptyList(),
            requestedFields2 = emptyList(),
        )

        val result = useCase(inputDescriptors).assertOk()

        assertEquals(0, result.size)
    }

    @Test
    fun `Getting compatible credentials maps errors from getting any credentials`() = runTest {
        val exception = IllegalStateException()
        coEvery { mockGetAnyCredentials() } returns Err(CredentialError.Unexpected(exception))

        val result = useCase(inputDescriptors)

        val error = result.assertErrorType(CredentialPresentationError.Unexpected::class)
        assertEquals(exception, error.cause)
    }

    @Test
    fun `Getting compatible credentials maps errors from getting requested fields`() = runTest {
        val exception = IllegalStateException()
        coEvery { mockGetRequestedFields(any(), any()) } returns Err(CredentialPresentationError.Unexpected(exception))

        val result = useCase(inputDescriptors)

        val error = result.assertErrorType(CredentialPresentationError.Unexpected::class)
        assertEquals(exception, error.cause)
    }

    @Test
    fun `Getting compatible credentials maps errors from json parsing`() = runTest {
        val exception = IllegalStateException()
        coEvery { mockCredential.getClaimsForPresentation().toString() } throws exception

        val result = useCase(inputDescriptors)

        val error = result.assertErrorType(CredentialPresentationError.Unexpected::class)
        assertEquals(exception, error.cause)
    }

    @Test
    fun `Getting compatible credentials where one misses an id returns an error`() = runTest {
        setupDefaultMocks(requestedFields = requestedFields)
        every { mockCredential.id } returns null
        coEvery { mockGetAnyCredentials() } returns Ok(listOf(mockCredential))

        val result = useCase(inputDescriptors)

        val error = result.assertErrorType(CredentialPresentationError.Unexpected::class)
        assertTrue(error.cause is IllegalArgumentException)
    }

    @Test
    fun `Getting compatible credentials without key binding with two matching credentials returns both ids and their fields`() = runTest {
        setupDefaultMocks(
            credentials = listOf(mockCredential, mockCredential2),
            requestedFields = requestedFields,
            requestedFields2 = requestedFields2,
        )

        every { mockCredential.keyBindingAlgorithm } returns null
        every { mockCredential2.keyBindingAlgorithm } returns null
        every { inputDescriptor.formats } returns listOf(inputDescriptorFormatVcSdJwtEmpty)

        val result = useCase(inputDescriptors).assertOk()

        assertEquals(2, result.size)
        assertEquals(CREDENTIAL_ID, result[0].credentialId)
        assertEquals(requestedFields, result[0].requestedFields)

        assertEquals(CREDENTIAL_ID_2, result[1].credentialId)
        assertEquals(requestedFields2, result[1].requestedFields)
    }

    @Test
    fun `Getting compatible key bound credentials returns the credential with matching key binding algorithm`() = runTest {
        setupDefaultMocks(
            credentials = listOf(mockCredential, mockCredential2),
            requestedFields = requestedFields,
            requestedFields2 = requestedFields2,
        )

        every { mockCredential2.keyBindingAlgorithm } returns SigningAlgorithm.ES512

        val result = useCase(inputDescriptors).assertOk()

        assertEquals(1, result.size)

        assertEquals(CREDENTIAL_ID, result[0].credentialId)
        assertEquals(requestedFields, result[0].requestedFields)
    }

    @Test
    fun `Getting compatible key bound credentials returns the credential with key binding`() = runTest {
        setupDefaultMocks(
            credentials = listOf(mockCredential, mockCredential2),
            requestedFields = requestedFields,
            requestedFields2 = requestedFields2,
        )

        every { mockCredential.keyBindingAlgorithm } returns null

        val result = useCase(inputDescriptors).assertOk()

        assertEquals(1, result.size)

        assertEquals(CREDENTIAL_ID_2, result[0].credentialId)
        assertEquals(requestedFields2, result[0].requestedFields)
    }

    private fun setupDefaultMocks(
        credentials: List<AnyCredential> = listOf(mockCredential),
        requestedFields: List<PresentationRequestField> = emptyList(),
        requestedFields2: List<PresentationRequestField> = emptyList(),
    ) {
        every { mockCredential.id } returns CREDENTIAL_ID
        every { mockCredential.getClaimsForPresentation().toString() } returns CREDENTIAL_JSON
        every { mockCredential.format } returns CredentialFormat.VC_SD_JWT
        every { mockCredential.payload } returns CREDENTIAL_PAYLOAD
        every { mockCredential.keyBindingAlgorithm } returns SigningAlgorithm.ES256
        every { mockCredential2.id } returns CREDENTIAL_ID_2
        every { mockCredential2.getClaimsForPresentation().toString() } returns CREDENTIAL_JSON_2
        every { mockCredential2.format } returns CredentialFormat.VC_SD_JWT
        every { mockCredential2.payload } returns CREDENTIAL_PAYLOAD
        every { mockCredential2.keyBindingAlgorithm } returns SigningAlgorithm.ES256
        every { inputDescriptor.formats } returns listOf(inputDescriptorFormatVcSdJwt)
        coEvery { mockGetAnyCredentials() } returns Ok(credentials)
        coEvery { mockGetRequestedFields(CREDENTIAL_JSON, inputDescriptors) } returns Ok(requestedFields)
        coEvery { mockGetRequestedFields(CREDENTIAL_JSON_2, inputDescriptors) } returns Ok(requestedFields2)
    }

    private companion object {
        const val CREDENTIAL_ID = 1L
        const val CREDENTIAL_ID_2 = 2L
        const val CREDENTIAL_JSON = "credentialJson"
        const val CREDENTIAL_JSON_2 = "credentialJson2"
        const val PRESENTABLE_CLAIMS = "presentableClaims"

        val inputDescriptor: InputDescriptor = mockk()
        val inputDescriptorFormatVcSdJwt = InputDescriptorFormat.VcSdJwt(
            sdJwtAlgorithms = listOf(SigningAlgorithm.ES256),
            kbJwtAlgorithms = listOf(SigningAlgorithm.ES256),
        )
        val inputDescriptorFormatVcSdJwtEmpty = InputDescriptorFormat.VcSdJwt(
            sdJwtAlgorithms = listOf(),
            kbJwtAlgorithms = listOf(),
        )
        val inputDescriptors: List<InputDescriptor> = listOf(inputDescriptor)
        val requestedFields: List<PresentationRequestField> = listOf(mockk())
        val requestedFields2: List<PresentationRequestField> = listOf(mockk())

        const val CREDENTIAL_PAYLOAD = """{"proof":{"cryptosuite":"ES256"}}"""
    }
}

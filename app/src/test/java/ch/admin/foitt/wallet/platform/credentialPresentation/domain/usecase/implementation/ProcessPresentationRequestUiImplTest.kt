package ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.InputDescriptor
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequest
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CompatibleCredential
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CredentialPresentationError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.ProcessPresentationRequestResult
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.GetCompatibleCredentials
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.ValidatePresentationRequest
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialRepo
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ProcessPresentationRequestUiImplTest {

    @MockK
    private lateinit var mockValidatePresentationRequest: ValidatePresentationRequest

    @MockK
    private lateinit var mockCredentialRepository: CredentialRepo

    @MockK
    private lateinit var mockGetCompatibleCredentials: GetCompatibleCredentials

    @MockK
    private lateinit var mockPresentationRequest: PresentationRequest

    @MockK
    private lateinit var mockInputDescriptors: List<InputDescriptor>

    private lateinit var useCase: ProcessPresentationRequestImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = ProcessPresentationRequestImpl(
            mockValidatePresentationRequest,
            mockGetCompatibleCredentials,
            mockCredentialRepository
        )

        success()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Processing presentation request which matches one credential returns the credential`() = runTest {
        val credential = mockk<CompatibleCredential>()
        coEvery { mockGetCompatibleCredentials(mockInputDescriptors) } returns Ok(listOf(credential))

        val result = useCase(mockPresentationRequest)

        val expected = ProcessPresentationRequestResult.Credential(credential)
        assertEquals(expected, result.assertOk())
    }

    @Test
    fun `Processing presentation request which matches multiple credentials returns the credentials`() = runTest {
        val credentials = listOf(mockk<CompatibleCredential>(), mockk<CompatibleCredential>())
        coEvery { mockGetCompatibleCredentials(mockInputDescriptors) } returns Ok(credentials)

        val result = useCase(mockPresentationRequest)

        val expected = ProcessPresentationRequestResult.CredentialList(credentials)
        assertEquals(expected, result.assertOk())
    }

    @Test
    fun `Processing presentation request which matches no credential returns no compatible credential error`() = runTest {
        coEvery { mockGetCompatibleCredentials(mockInputDescriptors) } returns Ok(emptyList())

        val result = useCase(mockPresentationRequest)

        result.assertErrorType(CredentialPresentationError.NoCompatibleCredential::class)
    }

    @Test
    fun `Processing presentation request with empty wallet returns empty wallet error`() = runTest {
        coEvery { mockCredentialRepository.getAll() } returns Ok(emptyList())

        val result = useCase(mockPresentationRequest)

        result.assertErrorType(CredentialPresentationError.EmptyWallet::class)
    }

    @Test
    fun `Processing presentation request maps errors from request validation`() = runTest {
        coEvery {
            mockValidatePresentationRequest(mockPresentationRequest)
        } returns Err(CredentialPresentationError.InvalidPresentation(RESPONSE_URI))

        useCase(mockPresentationRequest).assertErrorType(CredentialPresentationError.InvalidPresentation::class)
    }

    @Test
    fun `Processing presentation request maps errors from getting all credentials`() = runTest {
        val exception = IllegalStateException()
        coEvery { mockCredentialRepository.getAll() } returns Err(SsiError.Unexpected(exception))

        val result = useCase(mockPresentationRequest)

        val error = result.assertErrorType(CredentialPresentationError.Unexpected::class)
        assertEquals(exception, error.cause)
    }

    @Test
    fun `Processing presentation request maps errors from getting compatible credentials`() = runTest {
        val exception = IllegalStateException()
        coEvery {
            mockGetCompatibleCredentials(mockInputDescriptors)
        } returns Err(CredentialPresentationError.Unexpected(exception))

        val result = useCase(mockPresentationRequest)

        val error = result.assertErrorType(CredentialPresentationError.Unexpected::class)
        assertEquals(exception, error.cause)
    }

    private fun success() {
        every { mockPresentationRequest.presentationDefinition } returns mockk {
            every { inputDescriptors } returns mockInputDescriptors
        }
        coEvery { mockValidatePresentationRequest(mockPresentationRequest) } returns Ok(Unit)
        coEvery { mockCredentialRepository.getAll() } returns Ok(listOf(mockk()))
        coEvery { mockGetCompatibleCredentials(mockInputDescriptors) } returns Ok(emptyList())
    }

    private companion object {
        const val RESPONSE_URI = "response uri"
    }
}

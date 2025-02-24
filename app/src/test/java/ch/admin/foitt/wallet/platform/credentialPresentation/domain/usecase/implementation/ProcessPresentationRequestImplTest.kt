package ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.InputDescriptor
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestContainer
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CompatibleCredential
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CredentialPresentationError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.ProcessPresentationRequestResult
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.GetCompatibleCredentials
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.ValidatePresentationRequest
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialRepo
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertSuccessType
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

class ProcessPresentationRequestImplTest {

    @MockK
    private lateinit var mockValidatePresentationRequest: ValidatePresentationRequest

    @MockK
    private lateinit var mockCredentialRepository: CredentialRepo

    @MockK
    private lateinit var mockGetCompatibleCredentials: GetCompatibleCredentials

    @MockK
    private lateinit var mockPresentationRequest: PresentationRequest

    @MockK
    private lateinit var mockJwtPresentationContainer: PresentationRequestContainer.Jwt

    @MockK
    private lateinit var mockJsonPresentationContainer: PresentationRequestContainer.Json

    @MockK
    private lateinit var mockJwt: Jwt

    @MockK
    private lateinit var mockInputDescriptors: List<InputDescriptor>

    @MockK
    private lateinit var mockCompatibleCredential: CompatibleCredential

    private lateinit var useCase: ProcessPresentationRequestImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = ProcessPresentationRequestImpl(
            mockValidatePresentationRequest,
            mockGetCompatibleCredentials,
            mockCredentialRepository
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Processing presentation request which matches one credential returns the credential`() = runTest {
        coEvery { mockGetCompatibleCredentials(mockInputDescriptors) } returns Ok(listOf(mockCompatibleCredential))

        val result = useCase(mockJwtPresentationContainer)

        val expected = ProcessPresentationRequestResult.Credential(
            mockCompatibleCredential,
            mockPresentationRequest,
            true,
        )
        val processPresentationResult = result.assertSuccessType(ProcessPresentationRequestResult.Credential::class)
        assertEquals(expected, processPresentationResult)
    }

    @Test
    fun `Processing presentation request which matches multiple credentials returns the credentials`() = runTest {
        val credentials = listOf(mockCompatibleCredential, mockCompatibleCredential)
        coEvery { mockGetCompatibleCredentials(mockInputDescriptors) } returns Ok(credentials)

        val result = useCase(mockJwtPresentationContainer)

        val expected = ProcessPresentationRequestResult.CredentialList(
            credentials,
            mockPresentationRequest,
            true,
        )
        val processPresentationResult = result.assertSuccessType(ProcessPresentationRequestResult.CredentialList::class)
        assertEquals(expected, processPresentationResult)
    }

    @Test
    fun `Processing presentation request which matches no credential returns no compatible credential error`() = runTest {
        coEvery { mockGetCompatibleCredentials(mockInputDescriptors) } returns Ok(emptyList())

        val result = useCase(mockJwtPresentationContainer)

        result.assertErrorType(CredentialPresentationError.NoCompatibleCredential::class)
    }

    @Test
    fun `Processing presentation request with empty wallet returns empty wallet error`() = runTest {
        coEvery { mockCredentialRepository.getAll() } returns Ok(emptyList())

        val result = useCase(mockJwtPresentationContainer)

        result.assertErrorType(CredentialPresentationError.EmptyWallet::class)
    }

    @Test
    fun `Processing presentation request maps errors from request validation`() = runTest {
        coEvery {
            mockValidatePresentationRequest(mockJwtPresentationContainer)
        } returns Err(CredentialPresentationError.InvalidPresentation(RESPONSE_URI))

        useCase(mockJwtPresentationContainer).assertErrorType(CredentialPresentationError.InvalidPresentation::class)
    }

    @Test
    fun `Processing presentation request maps errors from getting all credentials`() = runTest {
        val exception = IllegalStateException()
        coEvery { mockCredentialRepository.getAll() } returns Err(SsiError.Unexpected(exception))

        val result = useCase(mockJwtPresentationContainer)

        val error = result.assertErrorType(CredentialPresentationError.Unexpected::class)
        assertEquals(exception, error.cause)
    }

    @Test
    fun `Processing presentation request maps errors from getting compatible credentials`() = runTest {
        val exception = IllegalStateException()
        coEvery {
            mockGetCompatibleCredentials(mockInputDescriptors)
        } returns Err(CredentialPresentationError.Unexpected(exception))

        val result = useCase(mockJwtPresentationContainer)

        val error = result.assertErrorType(CredentialPresentationError.Unexpected::class)
        assertEquals(exception, error.cause)
    }

    @Test
    fun `Processing a presentation wrapped in a JWT should return a 'true' for the TrustStatement flag`(): Unit = runTest {
        val result = useCase(mockJwtPresentationContainer)
        val processPresentationResult = result.assertSuccessType(ProcessPresentationRequestResult.Credential::class)

        assert(processPresentationResult.shouldFetchTrustStatements)
    }

    @Test
    fun `Processing a presentation wrapped in a JSON should return a 'false' for the TrustStatement flag`(): Unit = runTest {
        val result = useCase(mockJsonPresentationContainer)
        val processPresentationResult = result.assertSuccessType(ProcessPresentationRequestResult.Credential::class)

        assert(!processPresentationResult.shouldFetchTrustStatements)
    }

    private fun setupDefaultMocks() {
        every { mockPresentationRequest.presentationDefinition } returns mockk {
            every { inputDescriptors } returns mockInputDescriptors
        }
        coEvery { mockValidatePresentationRequest(any()) } returns Ok(mockPresentationRequest)
        coEvery { mockCredentialRepository.getAll() } returns Ok(listOf(mockk()))
        coEvery { mockGetCompatibleCredentials(mockInputDescriptors) } returns Ok(listOf(mockCompatibleCredential))
        coEvery { mockJwtPresentationContainer.jwt } returns mockJwt
    }

    private companion object {
        const val RESPONSE_URI = "response uri"
    }
}

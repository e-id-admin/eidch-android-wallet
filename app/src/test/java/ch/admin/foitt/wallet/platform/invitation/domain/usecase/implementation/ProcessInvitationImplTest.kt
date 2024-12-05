package ch.admin.foitt.wallet.platform.invitation.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOffer
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequest
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.FetchCredential
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CompatibleCredential
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CredentialPresentationError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.ProcessPresentationRequestResult
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.ProcessPresentationRequest
import ch.admin.foitt.wallet.platform.invitation.domain.model.InvitationError
import ch.admin.foitt.wallet.platform.invitation.domain.model.ProcessInvitationResult
import ch.admin.foitt.wallet.platform.invitation.domain.usecase.ValidateInvitation
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ProcessInvitationImplTest {

    @MockK
    private lateinit var mockValidateInvitation: ValidateInvitation

    @MockK
    private lateinit var mockFetchCredential: FetchCredential

    @MockK
    private lateinit var mockProcessPresentationRequest: ProcessPresentationRequest

    private lateinit var useCase: ProcessInvitationImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = ProcessInvitationImpl(
            validateInvitation = mockValidateInvitation,
            fetchCredential = mockFetchCredential,
            processPresentationRequest = mockProcessPresentationRequest,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Processing a valid credential offer returns a credential id`() = runTest {
        val offer = mockk<CredentialOffer>()
        coEvery { mockValidateInvitation(INVITATION_URI) } returns Ok(offer)
        coEvery { mockFetchCredential(offer) } returns Ok(CREDENTIAL_ID)

        val result = useCase(INVITATION_URI)

        val expected = ProcessInvitationResult.CredentialOffer(CREDENTIAL_ID)
        assertEquals(expected, result.assertOk())
    }

    @Test
    fun `Processing a valid credential maps errors from fetching credential`() = runTest {
        val offer = mockk<CredentialOffer>()
        val exception = IllegalStateException()
        coEvery { mockValidateInvitation(INVITATION_URI) } returns Ok(offer)
        coEvery { mockFetchCredential(offer) } returns Err(CredentialError.Unexpected(exception))

        val result = useCase(INVITATION_URI)

        result.assertErrorType(InvitationError.Unexpected::class)
    }

    @Test
    fun `Processing a valid presentation request matching one credential returns the credential and request`() = runTest {
        val request = mockk<PresentationRequest>()
        val credential = mockk<CompatibleCredential>()
        val requestResult = ProcessPresentationRequestResult.Credential(credential)
        coEvery { mockValidateInvitation(INVITATION_URI) } returns Ok(request)
        coEvery { mockProcessPresentationRequest(request) } returns Ok(requestResult)

        val result = useCase(INVITATION_URI)

        val expected = ProcessInvitationResult.PresentationRequest(credential, request)
        assertEquals(expected, result.assertOk())
    }

    @Test
    fun `Processing a valid presentation request matching multiple credential returns the credentials and request`() = runTest {
        val request = mockk<PresentationRequest>()
        val credentials = listOf(mockk<CompatibleCredential>())
        val requestResult = ProcessPresentationRequestResult.CredentialList(credentials)
        coEvery { mockValidateInvitation(INVITATION_URI) } returns Ok(request)
        coEvery { mockProcessPresentationRequest(request) } returns Ok(requestResult)

        val result = useCase(INVITATION_URI)

        val expected = ProcessInvitationResult.PresentationRequestCredentialList(credentials, request)
        assertEquals(expected, result.assertOk())
    }

    @Test
    fun `Processing a valid presentation request maps errors from processing presentation request`() = runTest {
        val request = mockk<PresentationRequest>()
        val exception = IllegalStateException()
        coEvery { mockValidateInvitation(INVITATION_URI) } returns Ok(request)
        coEvery { mockProcessPresentationRequest(request) } returns Err(CredentialPresentationError.Unexpected(exception))

        val result = useCase(INVITATION_URI)

        result.assertErrorType(InvitationError.Unexpected::class)
    }

    @Test
    fun `Processing an invalid invitation maps errors from validating invitation`() = runTest {
        coEvery { mockValidateInvitation(INVITATION_URI) } returns Err(InvitationError.Unexpected)

        val result = useCase(INVITATION_URI)

        result.assertErrorType(InvitationError.Unexpected::class)
    }

    @Test
    fun `Processing an unsupported invitation returns an error`() = runTest {
        coEvery { mockValidateInvitation(INVITATION_URI) } returns Ok(mockk())

        val result = useCase(INVITATION_URI)

        result.assertErrorType(InvitationError.Unexpected::class)
    }

    private companion object {
        const val INVITATION_URI = "invitationUri"
        const val CREDENTIAL_ID = 1L
    }
}

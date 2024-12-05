package ch.admin.foitt.wallet.feature.presentationRequest

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.JsonPresentationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.JwtPresentationRequest
import ch.admin.foitt.openid4vc.domain.model.sdjwt.SdJwt
import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.PresentationRequestError
import ch.admin.foitt.wallet.feature.presentationRequest.domain.repository.PresentationRequestRepository
import ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase.GetPresentationRequestCredentialListFlow
import ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase.implementation.GetPresentationRequestCredentialListFlowImpl
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.IsCredentialFromBetaIssuerImpl
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CompatibleCredential
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialWithDisplays
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedDisplay
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.FetchTrustStatementFromDid
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.getError
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetPresentationRequestCredentialListFlowImplTest {

    @MockK
    lateinit var mockPresentationRequestRepository: PresentationRequestRepository

    @MockK
    lateinit var mockGetLocalizedDisplay: GetLocalizedDisplay

    @MockK
    lateinit var mockIsCredentialFromBetaIssuerImpl: IsCredentialFromBetaIssuerImpl

    @MockK
    lateinit var mockFetchTrustStatementFromDid: FetchTrustStatementFromDid

    @MockK
    lateinit var mockCredentialWithDisplays1: CredentialWithDisplays

    @MockK
    lateinit var mockCredentialWithDisplays2: CredentialWithDisplays

    @MockK
    lateinit var mockCompatibleCredential: CompatibleCredential

    @MockK
    lateinit var mockJsonPresentationRequest: JsonPresentationRequest

    @MockK
    lateinit var mockJwtPresentationRequest: JwtPresentationRequest

    private lateinit var getPresentationRequestCredentialListFlow: GetPresentationRequestCredentialListFlow

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        getPresentationRequestCredentialListFlow = GetPresentationRequestCredentialListFlowImpl(
            mockPresentationRequestRepository,
            mockGetLocalizedDisplay,
            mockIsCredentialFromBetaIssuerImpl,
            mockFetchTrustStatementFromDid,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Getting the presentation request credential list flow returns a flow with one credential preview`() = runTest {
        val result = getPresentationRequestCredentialListFlow(
            compatibleCredentials = arrayOf(mockCompatibleCredential),
            presentationRequest = mockJwtPresentationRequest,
        ).firstOrNull()

        assertNotNull(result)
        val credentialPreviews = result?.assertOk()
        assertEquals(1, credentialPreviews?.size)
        assertEquals(COMPATIBLE_CREDENTIAL_ID, credentialPreviews?.first()?.credentialId)
    }

    @Test
    fun `Getting the presentation request credential list flow with a jwt presentation request does fetch the trust statement`() = runTest {
        val result = getPresentationRequestCredentialListFlow(
            compatibleCredentials = arrayOf(mockCompatibleCredential),
            presentationRequest = mockJwtPresentationRequest,
        ).firstOrNull()

        assertNotNull(result)
        result?.assertOk()

        coVerify(exactly = 2) {
            mockFetchTrustStatementFromDid(any())
        }
    }

    @Test
    fun `Getting the presentation request credential list flow with a jwt presentation request but invalid clientIdScheme does not fetch the trust statement`() = runTest {
        coEvery { mockJwtPresentationRequest.clientIdScheme } returns null

        val result = getPresentationRequestCredentialListFlow(
            compatibleCredentials = arrayOf(mockCompatibleCredential),
            presentationRequest = mockJwtPresentationRequest,
        ).firstOrNull()

        assertNotNull(result)
        result?.assertOk()

        coVerify(exactly = 0) {
            mockFetchTrustStatementFromDid(any())
        }
    }

    @Test
    fun `Getting the presentation request flow credential list with a jwt presentation request but invalid clientIdScheme (not 'did') does not fetch the trust statement`() = runTest {
        coEvery { mockJwtPresentationRequest.clientIdScheme } returns "somethingThatIsNotDid"

        val result = getPresentationRequestCredentialListFlow(
            compatibleCredentials = arrayOf(mockCompatibleCredential),
            presentationRequest = mockJwtPresentationRequest,
        ).firstOrNull()

        assertNotNull(result)
        result?.assertOk()

        coVerify(exactly = 0) {
            mockFetchTrustStatementFromDid(any())
        }
    }

    @Test
    fun `Getting the presentation request credential list flow with a json presentation request does not fetch the trust statement`() = runTest {
        val result = getPresentationRequestCredentialListFlow(
            compatibleCredentials = arrayOf(mockCompatibleCredential),
            presentationRequest = mockJsonPresentationRequest,
        ).firstOrNull()

        assertNotNull(result)
        result?.assertOk()

        coVerify(exactly = 0) {
            mockFetchTrustStatementFromDid(any())
        }
    }

    @Test
    fun `Getting the presentation request credential list flow maps errors from the repository`() = runTest {
        val exception = IllegalStateException("db error")
        coEvery {
            mockPresentationRequestRepository.getPresentationCredentialListFlow()
        } returns flowOf(Err(PresentationRequestError.Unexpected(exception)))

        val result = getPresentationRequestCredentialListFlow(
            compatibleCredentials = arrayOf(mockCompatibleCredential),
            presentationRequest = mockJwtPresentationRequest,
        ).firstOrNull()

        assertNotNull(result)
        result?.assertErrorType(PresentationRequestError.Unexpected::class)
        val error = result?.getError() as PresentationRequestError.Unexpected
        assertEquals(exception, error.throwable)
    }

    @Test
    fun `Getting the presentation request credential list flow maps errors from the GetLocalizedDisplay use case`() = runTest {
        coEvery { mockGetLocalizedDisplay(listOf(credentialDisplay2)) } returns null

        val result = getPresentationRequestCredentialListFlow(
            compatibleCredentials = arrayOf(mockCompatibleCredential),
            presentationRequest = mockJwtPresentationRequest,
        ).firstOrNull()

        assertNotNull(result)
        result?.assertErrorType(PresentationRequestError.Unexpected::class)
    }

    private fun setupDefaultMocks() {
        coEvery {
            mockPresentationRequestRepository.getPresentationCredentialListFlow()
        } returns flowOf(Ok(listOf(mockCredentialWithDisplays1, mockCredentialWithDisplays2)))
        coEvery { mockCredentialWithDisplays1.credential } returns credential1
        coEvery { mockCredentialWithDisplays2.credential } returns credential2
        coEvery { mockCredentialWithDisplays1.displays } returns listOf(credentialDisplay1)
        coEvery { mockCredentialWithDisplays2.displays } returns listOf(credentialDisplay2)

        coEvery { mockGetLocalizedDisplay(listOf(credentialDisplay1)) } returns credentialDisplay1
        coEvery { mockGetLocalizedDisplay(listOf(credentialDisplay2)) } returns credentialDisplay2

        coEvery { mockIsCredentialFromBetaIssuerImpl(any()) } returns false

        coEvery { mockFetchTrustStatementFromDid(CLIENT_ID) } returns Ok(TrustStatement(SdJwt(PAYLOAD)))

        coEvery { mockCompatibleCredential.credentialId } returns COMPATIBLE_CREDENTIAL_ID
        coEvery { mockJwtPresentationRequest.clientIdScheme } returns CLIENT_ID_SCHEME
        coEvery { mockJwtPresentationRequest.clientId } returns CLIENT_ID
    }

    private companion object {
        const val CLIENT_ID = "clientId"
        const val CLIENT_ID_SCHEME = "did"
        const val PAYLOAD = "payload"
        const val COMPATIBLE_CREDENTIAL_ID = 2L

        val credential1 = Credential(
            id = 1,
            status = CredentialStatus.VALID,
            privateKeyIdentifier = "privateKeyIdentifier",
            signingAlgorithm = "signingAlgo",
            payload = "payload",
            format = CredentialFormat.VC_SD_JWT,
        )

        val credential2 = Credential(
            id = 2,
            status = CredentialStatus.VALID,
            privateKeyIdentifier = "privateKeyIdentifier",
            signingAlgorithm = "signingAlgo",
            payload = "payload",
            format = CredentialFormat.VC_SD_JWT,
        )

        val credentialDisplay1 = CredentialDisplay(
            credentialId = 1,
            locale = "locale",
            name = "name",
        )

        val credentialDisplay2 = CredentialDisplay(
            credentialId = 2,
            locale = "locale",
            name = "name",
        )
    }
}

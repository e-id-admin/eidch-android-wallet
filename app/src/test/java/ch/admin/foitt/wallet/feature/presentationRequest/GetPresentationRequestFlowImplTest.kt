package ch.admin.foitt.wallet.feature.presentationRequest

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.JsonPresentationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.JwtPresentationRequest
import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.PresentationRequestError
import ch.admin.foitt.wallet.feature.presentationRequest.domain.repository.PresentationRequestRepository
import ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase.implementation.GetPresentationRequestFlowImpl
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.IsCredentialFromBetaIssuerImpl
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.PresentationRequestField
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimWithDisplays
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialWithDisplaysAndClaims
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedDisplay
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimData
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.MapToCredentialClaimData
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.getError
import io.mockk.MockKAnnotations
import io.mockk.coEvery
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

class GetPresentationRequestFlowImplTest {

    @MockK
    lateinit var mockPresentationRequestRepository: PresentationRequestRepository

    @MockK
    lateinit var mockGetLocalizedDisplay: GetLocalizedDisplay

    @MockK
    lateinit var mockMapToCredentialClaimData: MapToCredentialClaimData

    @MockK
    lateinit var mockIsCredentialFromBetaIssuerImpl: IsCredentialFromBetaIssuerImpl

    @MockK
    lateinit var mockCredentialWithDisplaysAndClaims: CredentialWithDisplaysAndClaims

    @MockK
    lateinit var mockCredentialClaimWithDisplays: CredentialClaimWithDisplays

    @MockK
    lateinit var mockCredentialClaim: CredentialClaim

    @MockK
    lateinit var mockCredentialClaimDisplay: CredentialClaimDisplay

    @MockK
    lateinit var mockClaimData: CredentialClaimData

    @MockK
    lateinit var mockRequestedField: PresentationRequestField

    @MockK
    lateinit var mockJsonPresentationRequest: JsonPresentationRequest

    @MockK
    lateinit var mockJwtPresentationRequest: JwtPresentationRequest

    private lateinit var getPresentationRequestFlow: GetPresentationRequestFlowImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        getPresentationRequestFlow = GetPresentationRequestFlowImpl(
            mockPresentationRequestRepository,
            mockGetLocalizedDisplay,
            mockMapToCredentialClaimData,
            mockIsCredentialFromBetaIssuerImpl,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Getting the presentation request flow returns a flow with one presentation request ui`() = runTest {
        val result = getPresentationRequestFlow(
            id = CREDENTIAL_ID1,
            requestedFields = listOf(mockRequestedField),
            presentationRequest = mockJwtPresentationRequest,
        ).firstOrNull()

        assertNotNull(result)
        result?.assertOk()
    }

    @Test
    fun `Getting the presentation request flow maps errors from the repository`() = runTest {
        val exception = IllegalStateException("db error")
        coEvery {
            mockPresentationRequestRepository.getPresentationCredentialFlow(CREDENTIAL_ID1)
        } returns flowOf(Err(PresentationRequestError.Unexpected(exception)))

        val result = getPresentationRequestFlow(
            id = CREDENTIAL_ID1,
            requestedFields = listOf(mockRequestedField),
            presentationRequest = mockJwtPresentationRequest,
        ).firstOrNull()

        assertNotNull(result)
        result?.assertErrorType(PresentationRequestError.Unexpected::class)
        val error = result?.getError() as PresentationRequestError.Unexpected
        assertEquals(exception, error.throwable)
    }

    @Test
    fun `Getting the presentation request flow maps errors from the GetLocalizedDisplay use case`() = runTest {
        coEvery { mockGetLocalizedDisplay(listOf(credentialDisplay1)) } returns null

        val result = getPresentationRequestFlow(
            id = CREDENTIAL_ID1,
            requestedFields = listOf(mockRequestedField),
            presentationRequest = mockJwtPresentationRequest,
        ).firstOrNull()

        assertNotNull(result)
        result?.assertErrorType(PresentationRequestError.Unexpected::class)
    }

    @Test
    fun `Getting the presentation request flow maps from the MapToCredentialClaimData use case`() = runTest {
        val exception = IllegalStateException("no claim displays found")
        coEvery {
            mockMapToCredentialClaimData(any<CredentialClaim>(), any<List<CredentialClaimDisplay>>())
        } returns Err(SsiError.Unexpected(exception))

        val result = getPresentationRequestFlow(
            id = CREDENTIAL_ID1,
            requestedFields = listOf(mockRequestedField),
            presentationRequest = mockJwtPresentationRequest,
        ).firstOrNull()

        assertNotNull(result)
        result?.assertErrorType(PresentationRequestError.Unexpected::class)
        val error = result?.getError() as PresentationRequestError.Unexpected
        assertEquals(exception, error.throwable)
    }

    private fun setupDefaultMocks() {
        coEvery {
            mockPresentationRequestRepository.getPresentationCredentialFlow(CREDENTIAL_ID1)
        } returns flowOf(Ok(mockCredentialWithDisplaysAndClaims))
        coEvery { mockCredentialWithDisplaysAndClaims.credential } returns credential1
        coEvery { mockCredentialWithDisplaysAndClaims.credentialDisplays } returns listOf(credentialDisplay1)
        coEvery { mockCredentialWithDisplaysAndClaims.claims } returns listOf(mockCredentialClaimWithDisplays)

        coEvery { mockCredentialClaimWithDisplays.claim } returns mockCredentialClaim
        coEvery { mockCredentialClaim.key } returns CLAIM_KEY
        coEvery { mockCredentialClaim.order } returns CLAIM_ORDER
        coEvery { mockCredentialClaimWithDisplays.displays } returns listOf(mockCredentialClaimDisplay)

        coEvery { mockGetLocalizedDisplay(listOf(credentialDisplay1)) } returns credentialDisplay1

        coEvery {
            mockMapToCredentialClaimData(mockCredentialClaim, listOf(mockCredentialClaimDisplay))
        } returns Ok(mockClaimData)
        coEvery { mockIsCredentialFromBetaIssuerImpl(CREDENTIAL_ID1) } returns false

        coEvery { mockRequestedField.key } returns CLAIM_KEY
        coEvery { mockJwtPresentationRequest.clientIdScheme } returns CLIENT_ID_SCHEME
        coEvery { mockJwtPresentationRequest.clientId } returns CLIENT_ID
    }

    private companion object {
        const val PAYLOAD = "payload"
        const val CLAIM_KEY = "claimKey"
        const val CLAIM_ORDER = 1
        const val CLIENT_ID = "clientId"
        const val CLIENT_ID_SCHEME = "did"

        const val CREDENTIAL_ID1 = 1L

        val credential1 = Credential(
            id = CREDENTIAL_ID1,
            status = CredentialStatus.VALID,
            privateKeyIdentifier = "privateKeyIdentifier",
            signingAlgorithm = "signingAlgo",
            payload = "payload",
            format = CredentialFormat.VC_SD_JWT,
        )

        val credentialDisplay1 = CredentialDisplay(
            credentialId = CREDENTIAL_ID1,
            locale = "locale",
            name = "name"
        )
    }
}

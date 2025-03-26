package ch.admin.foitt.wallet.feature.presentationRequest

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequest
import ch.admin.foitt.wallet.feature.credentialOffer.mock.MockCredentialOffer.credentialDisplayData
import ch.admin.foitt.wallet.feature.credentialOffer.mock.MockCredentialOffer.credentialDisplays
import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.PresentationRequestError
import ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase.implementation.GetPresentationRequestFlowImpl
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialDisplayData
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.toDisplayStatus
import ch.admin.foitt.wallet.platform.credential.domain.usecase.MapToCredentialDisplayData
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.PresentationRequestField
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimWithDisplays
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialWithDisplaysAndClaims
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimData
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialWithDisplaysAndClaimsRepository
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
    lateinit var mockCredentialWithDisplaysAndClaimsRepository: CredentialWithDisplaysAndClaimsRepository

    @MockK
    lateinit var mockMapToCredentialDisplayData: MapToCredentialDisplayData

    @MockK
    lateinit var mockMapToCredentialClaimData: MapToCredentialClaimData

    @MockK
    lateinit var mockCredentialWithDisplaysAndClaims: CredentialWithDisplaysAndClaims

    @MockK
    lateinit var mockCredentialClaimWithDisplays: CredentialClaimWithDisplays

    @MockK
    lateinit var mockCredentialClaim: CredentialClaim

    @MockK
    lateinit var mockClaimData: CredentialClaimData

    @MockK
    lateinit var mockRequestedField: PresentationRequestField

    @MockK
    lateinit var mockPresentationRequest: PresentationRequest

    @MockK
    lateinit var mockCredential: Credential

    private lateinit var getPresentationRequestFlow: GetPresentationRequestFlowImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        getPresentationRequestFlow = GetPresentationRequestFlowImpl(
            mockCredentialWithDisplaysAndClaimsRepository,
            mockMapToCredentialDisplayData,
            mockMapToCredentialClaimData,
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
        ).firstOrNull()

        assertNotNull(result)
        result?.assertOk()
    }

    @Test
    fun `Getting the presentation request flow maps errors from the repository`() = runTest {
        val exception = IllegalStateException("db error")
        coEvery {
            mockCredentialWithDisplaysAndClaimsRepository.getCredentialWithDisplaysAndClaimsFlowById(CREDENTIAL_ID1)
        } returns flowOf(Err(SsiError.Unexpected(exception)))

        val result = getPresentationRequestFlow(
            id = CREDENTIAL_ID1,
            requestedFields = listOf(mockRequestedField),
        ).firstOrNull()

        assertNotNull(result)
        result?.assertErrorType(PresentationRequestError.Unexpected::class)
        val error = result?.getError() as PresentationRequestError.Unexpected
        assertEquals(exception, error.throwable)
    }

    @Test
    fun `Getting the presentation request flow maps errors from the MapToCredentialDisplayData use case`() = runTest {
        val exception = IllegalStateException("map to credential claim display data error")
        coEvery {
            mockMapToCredentialDisplayData(mockCredential, credentialDisplays)
        } returns Err(CredentialError.Unexpected(exception))

        val result = getPresentationRequestFlow(
            id = CREDENTIAL_ID1,
            requestedFields = listOf(mockRequestedField),
        ).firstOrNull()

        assertNotNull(result)
        result?.assertErrorType(PresentationRequestError.Unexpected::class)
    }

    @Test
    fun `Getting the presentation request flow maps from the MapToCredentialClaimData use case`() = runTest {
        val exception = IllegalStateException("no claim displays found")
        coEvery {
            mockMapToCredentialClaimData(any<CredentialClaimWithDisplays>())
        } returns Err(SsiError.Unexpected(exception))

        val result = getPresentationRequestFlow(
            id = CREDENTIAL_ID1,
            requestedFields = listOf(mockRequestedField),
        ).firstOrNull()

        assertNotNull(result)
        result?.assertErrorType(PresentationRequestError.Unexpected::class)
        val error = result?.getError() as PresentationRequestError.Unexpected
        assertEquals(exception, error.throwable)
    }

    private fun setupDefaultMocks() {
        coEvery {
            mockCredentialWithDisplaysAndClaimsRepository.getCredentialWithDisplaysAndClaimsFlowById(CREDENTIAL_ID1)
        } returns flowOf(Ok(mockCredentialWithDisplaysAndClaims))
        coEvery { mockCredentialWithDisplaysAndClaims.credential } returns mockCredential
        coEvery { mockCredentialWithDisplaysAndClaims.credentialDisplays } returns credentialDisplays
        coEvery { mockCredentialWithDisplaysAndClaims.claims } returns listOf(mockCredentialClaimWithDisplays)

        coEvery { mockCredentialClaimWithDisplays.claim } returns mockCredentialClaim
        coEvery { mockCredentialClaim.key } returns CLAIM_KEY
        coEvery { mockCredentialClaim.order } returns CLAIM_ORDER

        coEvery {
            mockMapToCredentialDisplayData(mockCredential, credentialDisplays)
        } returns Ok(credentialDisplayData)

        coEvery {
            mockMapToCredentialClaimData(mockCredentialClaimWithDisplays)
        } returns Ok(mockClaimData)

        coEvery { mockRequestedField.key } returns CLAIM_KEY
        coEvery { mockPresentationRequest.clientIdScheme } returns CLIENT_ID_SCHEME
        coEvery { mockPresentationRequest.clientId } returns CLIENT_ID
    }

    private companion object {
        const val CLAIM_KEY = "claimKey"
        const val CLAIM_ORDER = 1
        const val CLIENT_ID = "clientId"
        const val CLIENT_ID_SCHEME = "did"

        const val CREDENTIAL_ID1 = 1L

        val credentialDisplay1 = CredentialDisplay(
            credentialId = CREDENTIAL_ID1,
            locale = "locale",
            name = "name"
        )

        val credentialDisplays = listOf(credentialDisplay1)

        val credentialDisplayData = CredentialDisplayData(
            credentialId = CREDENTIAL_ID1,
            status = CredentialStatus.VALID.toDisplayStatus(),
            credentialDisplay = credentialDisplay1,
            isCredentialFromBetaIssuer = false,
        )
    }
}

package ch.admin.foitt.wallet.feature.presentationRequest

import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.PresentationCredentialDisplayData
import ch.admin.foitt.wallet.feature.presentationRequest.domain.model.PresentationRequestError
import ch.admin.foitt.wallet.feature.presentationRequest.domain.repository.PresentationRequestRepository
import ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase.GetPresentationRequestCredentialListFlow
import ch.admin.foitt.wallet.feature.presentationRequest.domain.usecase.implementation.GetPresentationRequestCredentialListFlowImpl
import ch.admin.foitt.wallet.platform.credential.domain.usecase.IsCredentialFromBetaIssuer
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CompatibleCredential
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialDisplay
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialWithDisplays
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedDisplay
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

class GetPresentationRequestCredentialListFlowImplTest {

    @MockK
    lateinit var mockPresentationRequestRepository: PresentationRequestRepository

    @MockK
    lateinit var mockGetLocalizedDisplay: GetLocalizedDisplay

    @MockK
    lateinit var mockIsCredentialFromBetaIssuer: IsCredentialFromBetaIssuer

    @MockK
    lateinit var mockCredentialWithDisplays1: CredentialWithDisplays

    @MockK
    lateinit var mockCredentialWithDisplays2: CredentialWithDisplays

    @MockK
    lateinit var mockCompatibleCredential: CompatibleCredential

    private lateinit var getPresentationRequestCredentialListFlow: GetPresentationRequestCredentialListFlow

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        getPresentationRequestCredentialListFlow = GetPresentationRequestCredentialListFlowImpl(
            mockPresentationRequestRepository,
            mockGetLocalizedDisplay,
            mockIsCredentialFromBetaIssuer,
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
        ).firstOrNull()

        assertNotNull(result)
        val displayData: PresentationCredentialDisplayData? = result?.assertOk()
        val credentialPreviews = displayData?.credentials
        assertEquals(1, credentialPreviews?.size)
        assertEquals(COMPATIBLE_CREDENTIAL_ID, credentialPreviews?.first()?.credentialId)
    }

    @Test
    fun `A beta issuer credential is indicated in the result`(): Unit = runTest {
        coEvery { mockIsCredentialFromBetaIssuer.invoke(credentialId = any()) } returns true

        val result = getPresentationRequestCredentialListFlow(
            compatibleCredentials = arrayOf(mockCompatibleCredential),
        ).firstOrNull()

        assertNotNull(result)
        val displayData: PresentationCredentialDisplayData? = result?.assertOk()
        val credentialPreviews = displayData?.credentials
        assert(credentialPreviews?.firstOrNull()?.isCredentialFromBetaIssuer == true)
    }

    @Test
    fun `Getting the presentation request credential list flow maps errors from the repository`() = runTest {
        val exception = IllegalStateException("db error")
        coEvery {
            mockPresentationRequestRepository.getPresentationCredentialListFlow()
        } returns flowOf(Err(PresentationRequestError.Unexpected(exception)))

        val result = getPresentationRequestCredentialListFlow(
            compatibleCredentials = arrayOf(mockCompatibleCredential),
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

        coEvery { mockIsCredentialFromBetaIssuer(any()) } returns false

        coEvery { mockCompatibleCredential.credentialId } returns COMPATIBLE_CREDENTIAL_ID
    }

    private companion object {
        const val CLIENT_ID = "clientId"
        const val CLIENT_ID_SCHEME = "did"
        const val PAYLOAD = "payload"
        const val COMPATIBLE_CREDENTIAL_ID = 2L

        val credential1 = Credential(
            id = 1,
            status = CredentialStatus.VALID,
            keyBindingIdentifier = "privateKeyIdentifier",
            keyBindingAlgorithm = "signingAlgo",
            payload = "payload",
            format = CredentialFormat.VC_SD_JWT,
            issuer = "issuer"
        )

        val credential2 = Credential(
            id = 2,
            status = CredentialStatus.VALID,
            keyBindingIdentifier = "privateKeyIdentifier",
            keyBindingAlgorithm = "signingAlgo",
            payload = "payload",
            format = CredentialFormat.VC_SD_JWT,
            issuer = "issuer"
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

package ch.admin.foitt.wallet.platform.eIdApplicationProcess

import ch.admin.foitt.wallet.platform.appAttestation.domain.model.AttestationError
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestation
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.ClientAttestationPoP
import ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.GenerateProofOfPossession
import ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.RequestClientAttestation
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestError
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.PairWalletResponse
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.SIdChallengeResponse
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.EIdRequestCaseRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.repository.SIdRepository
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.usecase.implementation.PairWalletImpl
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
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

class PairWalletImplTest {

    @MockK
    private lateinit var mockSIdRepository: SIdRepository

    @MockK
    private lateinit var mockEIdRequestCaseRepository: EIdRequestCaseRepository

    @MockK
    private lateinit var mockRequestClientAttestation: RequestClientAttestation

    @MockK
    private lateinit var mockGenerateProofOfPossession: GenerateProofOfPossession

    @MockK
    private lateinit var mockEnvironmentSetupRepository: EnvironmentSetupRepository

    @MockK
    private lateinit var mockPairWalletResponse: PairWalletResponse

    private val mockSIdUrl = "https://example.com"
    private val mockSIdChallenge = SIdChallengeResponse("someChallenge")
    private val testCaseId = "testCaseId"

    @MockK
    private lateinit var mockClientAttestation: ClientAttestation

    @MockK
    private lateinit var mockClientAttestationPoP: ClientAttestationPoP

    private lateinit var useCase: PairWalletImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = PairWalletImpl(
            sIdRepository = mockSIdRepository,
            eIdRequestCaseRepository = mockEIdRequestCaseRepository,
            requestClientAttestation = mockRequestClientAttestation,
            generateProofOfPossession = mockGenerateProofOfPossession,
            environmentSetupRepository = mockEnvironmentSetupRepository,
        )

        coEvery { mockRequestClientAttestation(any(), any()) } returns Ok(mockClientAttestation)
        coEvery { mockSIdRepository.fetchChallenge() } returns Ok(mockSIdChallenge)
        coEvery {
            mockGenerateProofOfPossession.invoke(
                clientAttestation = any(),
                challenge = any(),
                audience = any(),
                requestBody = any()
            )
        } returns Ok(mockClientAttestationPoP)
        coEvery { mockEnvironmentSetupRepository.sidBackendUrl } returns mockSIdUrl
        coEvery {
            mockSIdRepository.pairWallet(
                caseId = any(),
                clientAttestation = any(),
                clientAttestationPoP = any()
            )
        } returns Ok(mockPairWalletResponse)
        coEvery { mockPairWalletResponse.walletPairingId } returns "pairingId"
        coEvery { mockEIdRequestCaseRepository.addPairingId(any(), any()) } returns Ok(Unit)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `A success follow specific steps`() = runTest {
        val result = useCase.invoke(testCaseId)
        result.assertOk()

        coVerifyOrder {
            mockRequestClientAttestation(any(), any())
            mockSIdRepository.fetchChallenge()
            mockGenerateProofOfPossession(
                clientAttestation = mockClientAttestation,
                challenge = mockSIdChallenge.challenge,
                audience = mockSIdUrl,
                requestBody = any(),
            )
            mockSIdRepository.pairWallet(
                caseId = testCaseId,
                clientAttestation = mockClientAttestation,
                clientAttestationPoP = mockClientAttestationPoP
            )
        }
    }

    @Test
    fun `A client attestation error is propagated`() = runTest {
        val exception = Exception("testException")
        coEvery { mockRequestClientAttestation(any(), any()) } returns Err(AttestationError.Unexpected(exception))

        val result = useCase("caseId")
        val error = result.assertErrorType(EIdRequestError.Unexpected::class)
        assertEquals(exception, error.cause)
    }

    @Test
    fun `A challenge fetching failure is propagated`() = runTest {
        coEvery {
            mockSIdRepository.fetchChallenge()
        } returns Err(EIdRequestError.NetworkError)

        useCase(testCaseId).assertErrorType(EIdRequestError.NetworkError::class)
    }

    @Test
    fun `A client attestation PoP generation failure is propagated`() = runTest {
        val exception = Exception("my exception")
        coEvery {
            mockGenerateProofOfPossession(any(), any(), any(), any())
        } returns Err(AttestationError.Unexpected(exception))

        val error = useCase(testCaseId).assertErrorType(EIdRequestError.Unexpected::class)
        assertEquals(exception, error.cause)
    }

    @Test
    fun `A pair wallet failure is propagated`() = runTest {
        val exception = Exception("my exception")
        coEvery {
            mockSIdRepository.pairWallet(
                caseId = any(),
                clientAttestation = any(),
                clientAttestationPoP = any(),
            )
        } returns Err(EIdRequestError.Unexpected(exception))

        val error = useCase(testCaseId).assertErrorType(EIdRequestError.Unexpected::class)
        assertEquals(exception, error.cause)
    }
}

package ch.admin.foitt.wallet.platform.appAttestation

import ch.admin.foitt.didResolver.domain.DidResolverHelper
import ch.admin.foitt.wallet.platform.appAttestation.domain.model.AttestationError
import ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.GetAttestationUrlFromDid
import ch.admin.foitt.wallet.platform.appAttestation.domain.usecase.implementation.GetAttestationUrlFromDidImpl
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
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
import java.net.URL

class GetAttestationUrlFromDidImplTest {
    @MockK
    private lateinit var mockDidResolverHelper: DidResolverHelper

    @MockK
    private lateinit var mockEnvironmentSetupRepository: EnvironmentSetupRepository

    private lateinit var useCase: GetAttestationUrlFromDid

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = GetAttestationUrlFromDidImpl(
            didResolverHelper = mockDidResolverHelper,
            environmentSetupRepository = mockEnvironmentSetupRepository,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Getting the attestation url succeeds`() = runTest {
        val result = useCase(ACTOR_DID).assertOk()

        assertEquals(ATTESTATION_SERVICE_URL, result)
    }

    @Test
    fun `Getting the attestation url with missing did returns default`() = runTest {
        val result = useCase(null).assertOk()

        assertEquals(ATTESTATION_SERVICE_URL, result)
    }

    @Test
    fun `Getting the attestation url maps did resolver error`() = runTest {
        val exception = IllegalStateException("did resolver error")
        coEvery { mockDidResolverHelper.getHttpsUrl(ACTOR_DID) } returns Err(exception)

        useCase(ACTOR_DID).assertErrorType(AttestationError.Unexpected::class)
    }

    @Test
    fun `Getting the attestation url with invalid mapping returns default`() = runTest {
        coEvery { mockEnvironmentSetupRepository.attestationServiceMapping } returns emptyMap()

        val result = useCase(ACTOR_DID).assertOk()

        assertEquals(ATTESTATION_SERVICE_URL, result)
    }

    private fun setupDefaultMocks() {
        every { mockDidResolverHelper.getHttpsUrl(ACTOR_DID) } returns Ok(attestationServiceIdentifierUrl)
        coEvery { mockEnvironmentSetupRepository.defaultAttestationServiceUrl } returns ATTESTATION_SERVICE_URL
        coEvery { mockEnvironmentSetupRepository.attestationServiceMapping } returns attestationMapping
    }

    private companion object {
        const val ACTOR_DID = "actor did"
        const val ATTESTATION_SERVICE_IDENTIFIER = "identifier.attestation.com"
        val attestationServiceIdentifierUrl = URL("https://$ATTESTATION_SERVICE_IDENTIFIER")
        const val ATTESTATION_SERVICE_URL = "https://service.example.com"
        val attestationMapping = mapOf(
            ATTESTATION_SERVICE_IDENTIFIER to ATTESTATION_SERVICE_URL
        )
    }
}

package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.TokenType
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialResponse
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.AnyCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.RawAndParsedIssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyCredentialDisplay
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyDisplays
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyIssuerDisplay
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GenerateAnyDisplays
import ch.admin.foitt.wallet.platform.credential.domain.usecase.UpdateDeferredCredential
import ch.admin.foitt.wallet.platform.database.domain.model.Cluster
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialAuthenticationEntity
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialAuthenticationWithDpopBinding
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredCredentialEntity
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredCredentialWithAuthenticationAndKeyBinding
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredProgressionState
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialOfferRepository
import ch.admin.foitt.wallet.platform.ssi.domain.repository.DeferredCredentialRepository
import ch.admin.foitt.wallet.platform.utils.compress
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URL
import java.time.Instant

class UpdateDeferredCredentialImplTest {

    @MockK
    private lateinit var mockDeferredCredentialRepository: DeferredCredentialRepository

    @MockK
    private lateinit var mockGenerateAnyDisplays: GenerateAnyDisplays

    @MockK
    private lateinit var mockCredentialOfferRepository: CredentialOfferRepository

    @MockK
    private lateinit var mockRawAndParsedIssuerCredentialInfo: RawAndParsedIssuerCredentialInfo

    @MockK
    private lateinit var mockIssuerCredentialInfo: IssuerCredentialInfo

    @MockK
    private lateinit var mockAnyCredentialConfiguration: AnyCredentialConfiguration

    @MockK
    private lateinit var mockAnyDisplays: AnyDisplays

    @MockK
    private lateinit var mockAnyIssuerDisplay: AnyIssuerDisplay

    @MockK
    private lateinit var mockAnyCredentialDisplay: AnyCredentialDisplay

    @MockK
    private lateinit var mockCluster: Cluster

    @MockK
    private lateinit var mockIssuerCredentialInfoJwt: Jwt

    private lateinit var useCase: UpdateDeferredCredential

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = UpdateDeferredCredentialImpl(
            deferredCredentialRepository = mockDeferredCredentialRepository,
            generateAnyDisplays = mockGenerateAnyDisplays,
            credentialOfferRepository = mockCredentialOfferRepository,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    private fun setupDefaultMocks() {
        mockkStatic(Instant::class)
        coEvery { Instant.now().epochSecond } returns currentTime

        coEvery {
            mockDeferredCredentialRepository.updateStatus(
                credentialId = any(),
                progressionState = any(),
                polledAt = any(),
                pollInterval = any(),
            )
        } returns Ok(1)

        coEvery {
            mockGenerateAnyDisplays(any(), any(), any(), any(), any())
        } returns Ok(mockAnyDisplays)

        coEvery {
            mockCredentialOfferRepository.updateDeferredCredentialMetaData(
                any(),
                any(),
                any(),
                any(),
            )
        } returns Ok(Unit)

        coEvery { mockRawAndParsedIssuerCredentialInfo.issuerCredentialInfo } returns mockIssuerCredentialInfo
        coEvery { mockRawAndParsedIssuerCredentialInfo.rawIssuerCredentialInfo } returns mockIssuerCredentialInfoJwt
        coEvery { mockIssuerCredentialInfo.credentialConfigurations } returns listOf(mockAnyCredentialConfiguration)
        coEvery { mockAnyCredentialConfiguration.identifier } returns SELECTED_CONFIG_ID_1
        coEvery { mockAnyDisplays.issuerDisplays } returns listOf(mockAnyIssuerDisplay)
        coEvery { mockAnyDisplays.credentialDisplays } returns listOf(mockAnyCredentialDisplay)
        coEvery { mockAnyDisplays.clusters } returns listOf(mockCluster)

        every { mockIssuerCredentialInfoJwt.payloadString } returns RAW_ISSUER_CREDENTIAL_INFO
    }

    @Test
    fun `An update of the deferred credential runs specific things`() = runTest {
        useCase(
            deferredCredentialEntity = deferredCredentialWithBinding01,
            credentialResponse = deferredCredentialResponse01,
            rawAndParsedIssuerCredentialInfo = mockRawAndParsedIssuerCredentialInfo,
        ).assertOk()

        coVerify {
            mockDeferredCredentialRepository.updateStatus(
                credentialId = credentialEntity01.id,
                progressionState = DeferredProgressionState.IN_PROGRESS,
                polledAt = currentTime,
                pollInterval = POLL_INTERVAL_1,
            )
            mockGenerateAnyDisplays(
                anyCredential = null,
                issuerInfo = mockIssuerCredentialInfo,
                trustStatement = null,
                credentialConfiguration = mockAnyCredentialConfiguration,
                ocaBundle = null,
            )
            mockCredentialOfferRepository.updateDeferredCredentialMetaData(
                credentialId = credentialEntity01.id,
                issuerDisplays = listOf(mockAnyIssuerDisplay),
                credentialDisplays = listOf(mockAnyCredentialDisplay),
                rawMetadata = RAW_ISSUER_CREDENTIAL_INFO_COMPRESSED,
            )
        }
    }

    @Test
    fun `A credential response containing a different transaction id returns an early error`() = runTest {
        val result = useCase(
            deferredCredentialEntity = deferredCredentialWithBinding01,
            credentialResponse = deferredCredentialResponse01.copy(transactionId = "otherId"),
            rawAndParsedIssuerCredentialInfo = mockRawAndParsedIssuerCredentialInfo,
        )

        result.assertErrorType(CredentialError.Unexpected::class)

        coVerify(exactly = 0) {
            mockDeferredCredentialRepository.updateStatus(
                credentialId = any(),
                progressionState = any(),
                polledAt = any(),
                pollInterval = any(),
            )
            mockCredentialOfferRepository.updateDeferredCredentialMetaData(
                any(),
                any(),
                any(),
                any()
            )
        }
    }

    @Test
    fun `New metadata containing a different credential configuration id does not update the credential`() = runTest {
        coEvery { mockAnyCredentialConfiguration.identifier } returns "other id"

        useCase(
            deferredCredentialEntity = deferredCredentialWithBinding01,
            credentialResponse = deferredCredentialResponse01,
            rawAndParsedIssuerCredentialInfo = mockRawAndParsedIssuerCredentialInfo,
        )

        coVerify(exactly = 0) {
            mockCredentialOfferRepository.updateDeferredCredentialMetaData(
                credentialId = any(),
                issuerDisplays = any(),
                credentialDisplays = any(),
                rawMetadata = any(),
            )
        }
    }

    @Test
    fun `Error during generating new displays from metadata does not update the credential`() = runTest {
        coEvery {
            mockGenerateAnyDisplays(any(), any(), any(), any(), any())
        } returns Err(CredentialError.Unexpected(IllegalStateException("display generation error")))

        useCase(
            deferredCredentialEntity = deferredCredentialWithBinding01,
            credentialResponse = deferredCredentialResponse01,
            rawAndParsedIssuerCredentialInfo = mockRawAndParsedIssuerCredentialInfo,
        )

        coVerify(exactly = 0) {
            mockCredentialOfferRepository.updateDeferredCredentialMetaData(
                credentialId = any(),
                issuerDisplays = any(),
                credentialDisplays = any(),
                rawMetadata = any(),
            )
        }
    }

    @Suppress("MayBeConst")
    private companion object {
        val currentTime = Instant.ofEpochSecond(15L).epochSecond
        const val ACCESS_TOKEN = "accessToken01"
        const val REFRESH_TOKEN = "refreshToken01"
        const val TRX_ID_1 = "transactionId01"
        const val ISSUER_ENDPOINT_1 = "https://example"
        const val POLLED_AT_1 = 5L
        const val POLL_INTERVAL_1 = 10
        const val SELECTED_CONFIG_ID_1 = "selectedConfigurationId01"
        const val ISSUER01_URL = "https://example.com/issuer"
        const val RAW_ISSUER_CREDENTIAL_INFO = "rawIssuerCredentialInfo"
        val RAW_ISSUER_CREDENTIAL_INFO_COMPRESSED = RAW_ISSUER_CREDENTIAL_INFO.toByteArray().compress()
        val deferredCredentialEntity01 = DeferredCredentialEntity(
            credentialId = 1L,
            progressionState = DeferredProgressionState.IN_PROGRESS,
            transactionId = TRX_ID_1,
            endpoint = ISSUER_ENDPOINT_1,
            pollInterval = POLL_INTERVAL_1,
            createdAt = 4L,
            polledAt = POLLED_AT_1,
        )

        val credentialEntity01 = Credential(
            id = 1L,
            format = CredentialFormat.VC_SD_JWT,
            createdAt = 1L,
            selectedConfigurationId = SELECTED_CONFIG_ID_1,
            issuerUrl = URL(ISSUER01_URL)
        )

        val credentialAuthenticationWithDpopBinding = CredentialAuthenticationWithDpopBinding(
            credentialAuthentication = CredentialAuthenticationEntity(
                credentialId = credentialEntity01.id,
                tokenType = TokenType.BEARER,
                accessToken = ACCESS_TOKEN,
                refreshToken = REFRESH_TOKEN,
            ),
            dpopBinding = null,
        )

        val deferredCredentialWithBinding01 = DeferredCredentialWithAuthenticationAndKeyBinding(
            deferredCredential = deferredCredentialEntity01,
            credential = credentialEntity01,
            keyBindings = listOf(),
            authentication = credentialAuthenticationWithDpopBinding,
        )

        val deferredCredentialResponse01 = CredentialResponse.DeferredCredential(
            transactionId = TRX_ID_1,
            interval = POLL_INTERVAL_1,
        )
    }
}

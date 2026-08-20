package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.TokenType
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialResponse
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.AnyCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.RawAndParsedIssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtError
import ch.admin.foitt.openid4vc.domain.usecase.GetSignedMetadataDid
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.VerifyVcSdJwtSignature
import ch.admin.foitt.wallet.platform.actorEnvironment.domain.model.ActorEnvironment
import ch.admin.foitt.wallet.platform.actorEnvironment.domain.usecase.GetActorEnvironment
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyCredentialDisplay
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyDisplays
import ch.admin.foitt.wallet.platform.credential.domain.model.AnyIssuerDisplay
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.FetchTrustForIssuance
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GenerateAnyDisplays
import ch.admin.foitt.wallet.platform.credential.domain.usecase.SaveCredentialFromDeferred
import ch.admin.foitt.wallet.platform.database.domain.model.Cluster
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialAuthenticationEntity
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialAuthenticationWithDpopBinding
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredCredentialEntity
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredCredentialWithAuthenticationAndKeyBinding
import ch.admin.foitt.wallet.platform.database.domain.model.DeferredProgressionState
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaBundle
import ch.admin.foitt.wallet.platform.oca.domain.model.OcaError
import ch.admin.foitt.wallet.platform.oca.domain.model.RawOcaBundle
import ch.admin.foitt.wallet.platform.oca.domain.model.VcMetadata
import ch.admin.foitt.wallet.platform.oca.domain.usecase.FetchVcMetadataByFormat
import ch.admin.foitt.wallet.platform.oca.domain.usecase.OcaBundler
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialOfferRepository
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IdentityV2TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustCheckResult
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URL

class SaveCredentialFromDeferredImplTest {

    @MockK
    private lateinit var mockVerifyVcSdJwtSignature: VerifyVcSdJwtSignature

    @MockK
    private lateinit var mockGetSignedMetadataDid: GetSignedMetadataDid

    @MockK
    private lateinit var mockFetchVcMetadataByFormat: FetchVcMetadataByFormat

    @MockK
    private lateinit var mockFetchTrustForIssuance: FetchTrustForIssuance

    @MockK
    private lateinit var mockOcaBundler: OcaBundler

    @MockK
    private lateinit var mockGenerateAnyDisplays: GenerateAnyDisplays

    @MockK
    private lateinit var mockCredentialOfferRepository: CredentialOfferRepository

    @MockK
    private lateinit var mockGetActorEnvironment: GetActorEnvironment

    @MockK
    private lateinit var mockRawAndParsedIssuerCredentialInfo: RawAndParsedIssuerCredentialInfo

    @MockK
    private lateinit var mockIssuerCredentialInfo: IssuerCredentialInfo

    @MockK
    private lateinit var mockAnyCredentialConfiguration: AnyCredentialConfiguration

    @MockK
    private lateinit var mockVcSdJwtCredential: VcSdJwtCredential

    @MockK
    private lateinit var mockVcMetadata: VcMetadata

    @MockK
    private lateinit var mockIssuerCredentialInfoJwt: Jwt

    @MockK
    private lateinit var mockTrustStatement: IdentityV2TrustStatement

    @MockK
    private lateinit var mockOcaBundle: OcaBundle

    @MockK
    private lateinit var mockTrustCheckResult: TrustCheckResult

    @MockK
    private lateinit var mockAnyDisplays: AnyDisplays

    @MockK
    private lateinit var mockAnyIssuerDisplay: AnyIssuerDisplay

    @MockK
    private lateinit var mockAnyCredentialDisplay: AnyCredentialDisplay

    @MockK
    private lateinit var mockCluster: Cluster

    private lateinit var useCase: SaveCredentialFromDeferred

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = SaveCredentialFromDeferredImpl(
            verifyVcSdJwtSignature = mockVerifyVcSdJwtSignature,
            getSignedMetadataDid = mockGetSignedMetadataDid,
            getActorEnvironment = mockGetActorEnvironment,
            fetchVcMetadataByFormat = mockFetchVcMetadataByFormat,
            fetchTrustForIssuance = mockFetchTrustForIssuance,
            ocaBundler = mockOcaBundler,
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
        coEvery {
            mockVerifyVcSdJwtSignature(any(), any(), any())
        } returns Ok(mockVcSdJwtCredential)

        coEvery { mockGetSignedMetadataDid(mockIssuerCredentialInfoJwt) } returns Ok(vcIssuer01)

        coEvery { mockGetActorEnvironment(any()) } returns ActorEnvironment.PRODUCTION

        coEvery {
            mockFetchVcMetadataByFormat(any())
        } returns Ok(mockVcMetadata)

        coEvery {
            mockFetchTrustForIssuance(any(), any(), any(), any())
        } returns Ok(mockTrustCheckResult)

        coEvery {
            mockOcaBundler(any())
        } returns Ok(mockOcaBundle)

        coEvery {
            mockGenerateAnyDisplays(any(), any(), any(), any(), any())
        } returns Ok(mockAnyDisplays)

        coEvery {
            mockCredentialOfferRepository.saveCredentialFromDeferred(
                any(), any(), any(), any(), any(), any(), any(), any(), any(),
            )
        } returns Ok(1L)

        // Result mocks
        every { mockVcSdJwtCredential.issuer } returns vcIssuer01
        every { mockVcSdJwtCredential.vcSchemaId } returns vcSchemaId01
        every { mockVcSdJwtCredential.payload } returns vcPayload01
        every { mockVcSdJwtCredential.validFromInstant } returns null
        every { mockVcSdJwtCredential.validUntilInstant } returns null
        every { mockVcSdJwtCredential.businessExpiryDate } returns null
        every { mockVcMetadata.rawOcaBundle } returns vcRawOcaBundle01
        every { mockIssuerCredentialInfoJwt.payloadString } returns RAW_ISSUER_CREDENTIAL_INFO
        every { mockRawAndParsedIssuerCredentialInfo.issuerCredentialInfo } returns mockIssuerCredentialInfo
        every { mockRawAndParsedIssuerCredentialInfo.rawIssuerCredentialInfo } returns mockIssuerCredentialInfoJwt
        every { mockIssuerCredentialInfo.credentialConfigurations } returns listOf(mockAnyCredentialConfiguration)
        every { mockAnyCredentialConfiguration.identifier } returns selectedConfigurationId01
        every { mockTrustCheckResult.identityTrustStatement } returns null
        every { mockAnyDisplays.issuerDisplays } returns listOf(mockAnyIssuerDisplay)
        every { mockAnyDisplays.credentialDisplays } returns listOf(mockAnyCredentialDisplay)
        every { mockAnyDisplays.clusters } returns listOf(mockCluster)
    }

    @Test
    fun `A received credential that is supported is properly saved`() = runTest {
        useCase(
            deferredCredentialEntity = deferredCredentialWithBinding01,
            credentialResponse = credentialResponse01,
            rawAndParsedIssuerCredentialInfo = mockRawAndParsedIssuerCredentialInfo,
            identityV2TrustStatement = mockTrustStatement,
        ).assertOk()

        coVerify {
            mockVerifyVcSdJwtSignature(
                keyBinding = null,
                payload = vcPayload01,
                format = deferredCredentialWithBinding01.credential.format,
            )
            mockGetSignedMetadataDid(mockIssuerCredentialInfoJwt)
            mockGetActorEnvironment(vcIssuer01)
            mockFetchVcMetadataByFormat(mockVcSdJwtCredential)
            mockFetchTrustForIssuance(
                identityTrustStatement = mockTrustStatement,
                protectedIssuanceAuthorizationTrustStatement = null,
                issuerDid = vcIssuer01,
                vcSchemaId = vcSchemaId01
            )
            mockOcaBundler(vcRawOcaBundle01.rawOcaBundle)
            mockGenerateAnyDisplays(
                anyCredential = mockVcSdJwtCredential,
                issuerInfo = mockIssuerCredentialInfo,
                trustStatement = null,
                credentialConfiguration = any(),
                ocaBundle = mockOcaBundle,
            )
            mockCredentialOfferRepository.saveCredentialFromDeferred(
                credentialId = deferredCredentialEntity01.credentialId,
                payloads = listOf(vcPayload01),
                validFrom = null,
                validUntil = null,
                issuer = vcIssuer01,
                issuerDisplays = listOf(mockAnyIssuerDisplay),
                credentialDisplays = listOf(mockAnyCredentialDisplay),
                clusters = listOf(mockCluster),
                rawCredentialData = any(),
            )
        }
    }

    @Test
    fun `A credential signature verification error is handled`() = runTest {
        coEvery { mockVerifyVcSdJwtSignature(any(), any(), any()) } returns Err(VcSdJwtError.InvalidJwt)

        useCase(
            deferredCredentialEntity = deferredCredentialWithBinding01,
            credentialResponse = credentialResponse01,
            rawAndParsedIssuerCredentialInfo = mockRawAndParsedIssuerCredentialInfo,
            identityV2TrustStatement = mockTrustStatement,
        ).assertErrorType(CredentialError.IntegrityCheckFailed::class)

        coVerify(exactly = 0) {
            mockCredentialOfferRepository.saveCredentialFromDeferred(any(), any(), any(), any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `Getting the metadata did error is mapped`() = runTest {
        val exception = IllegalStateException("metadata error")
        coEvery {
            mockGetSignedMetadataDid(mockIssuerCredentialInfoJwt)
        } returns Err(CredentialOfferError.Unexpected(exception))

        useCase(
            deferredCredentialEntity = deferredCredentialWithBinding01,
            credentialResponse = credentialResponse01,
            rawAndParsedIssuerCredentialInfo = mockRawAndParsedIssuerCredentialInfo,
            identityV2TrustStatement = mockTrustStatement,
        ).assertErrorType(CredentialError.Unexpected::class)

        coVerify(exactly = 0) {
            mockCredentialOfferRepository.saveCredentialFromDeferred(any(), any(), any(), any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `A credential where its issuer and the metadata issuer differ returns an unverified issuer error`() = runTest {
        coEvery { mockGetSignedMetadataDid(mockIssuerCredentialInfoJwt) } returns Ok("other issuer")

        useCase(
            deferredCredentialEntity = deferredCredentialWithBinding01,
            credentialResponse = credentialResponse01,
            rawAndParsedIssuerCredentialInfo = mockRawAndParsedIssuerCredentialInfo,
            identityV2TrustStatement = mockTrustStatement,
        ).assertErrorType(CredentialError.UnverifiedIssuer::class)

        coVerify(exactly = 0) {
            mockCredentialOfferRepository.saveCredentialFromDeferred(any(), any(), any(), any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `A credential where the metadata issuer and credential issuer differ returns an unverified issuer error`() = runTest {
        every { mockVcSdJwtCredential.issuer } returns "other issuer"

        useCase(
            deferredCredentialEntity = deferredCredentialWithBinding01,
            credentialResponse = credentialResponse01,
            rawAndParsedIssuerCredentialInfo = mockRawAndParsedIssuerCredentialInfo,
            identityV2TrustStatement = mockTrustStatement,
        ).assertErrorType(CredentialError.UnverifiedIssuer::class)

        coVerify(exactly = 0) {
            mockCredentialOfferRepository.saveCredentialFromDeferred(any(), any(), any(), any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `A deferred credential from an EXTERNAL issuer is rejected with UnknownRegistry`() = runTest {
        coEvery { mockGetActorEnvironment(any()) } returns ActorEnvironment.EXTERNAL

        useCase(
            deferredCredentialEntity = deferredCredentialWithBinding01,
            credentialResponse = credentialResponse01,
            rawAndParsedIssuerCredentialInfo = mockRawAndParsedIssuerCredentialInfo,
            identityV2TrustStatement = mockTrustStatement,
        ).assertErrorType(CredentialError.UnknownRegistry::class)

        coVerify(exactly = 0) {
            mockCredentialOfferRepository.saveCredentialFromDeferred(any(), any(), any(), any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `A VcMetadata fetching error is handled`() = runTest {
        coEvery { mockFetchVcMetadataByFormat(any()) } returns Err(OcaError.NetworkError)

        useCase(
            deferredCredentialEntity = deferredCredentialWithBinding01,
            credentialResponse = credentialResponse01,
            rawAndParsedIssuerCredentialInfo = mockRawAndParsedIssuerCredentialInfo,
            identityV2TrustStatement = mockTrustStatement,
        ).assertErrorType(CredentialError.NetworkError::class)

        coVerify(exactly = 0) {
            mockCredentialOfferRepository.saveCredentialFromDeferred(any(), any(), any(), any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `A fetchTrustForIssuance error is handled`() = runTest {
        val exception = IllegalStateException("trust error")
        coEvery {
            mockFetchTrustForIssuance(any(), any(), any(), any())
        } returns Err(CredentialError.Unexpected(exception))

        useCase(
            deferredCredentialEntity = deferredCredentialWithBinding01,
            credentialResponse = credentialResponse01,
            rawAndParsedIssuerCredentialInfo = mockRawAndParsedIssuerCredentialInfo,
            identityV2TrustStatement = mockTrustStatement,
        ).assertErrorType(CredentialError.Unexpected::class)
    }

    @Test
    fun `A credential offer repository error is handled`() = runTest {
        coEvery {
            mockCredentialOfferRepository.saveCredentialFromDeferred(any(), any(), any(), any(), any(), any(), any(), any(), any())
        } returns Err(SsiError.Unexpected(Exception("my exception")))

        useCase(
            deferredCredentialEntity = deferredCredentialWithBinding01,
            credentialResponse = credentialResponse01,
            rawAndParsedIssuerCredentialInfo = mockRawAndParsedIssuerCredentialInfo,
            identityV2TrustStatement = mockTrustStatement,
        ).assertErrorType(CredentialError.Unexpected::class)
    }

    @Suppress("MayBeConstant")
    private companion object {
        private val transactionId01 = "transactionId01"
        private val ACCESS_TOKEN = "access-token"
        private val REFRESH_TOKEN = "refresh-token"
        private val vcIssuer01 = "vcIssuer01"
        private val vcPayload01 = "vcPayload01"
        private val vcSchemaId01 = "vcSchemaId01"
        private val vcRawOcaBundle01 = RawOcaBundle("vcRawOcaBundle")
        private val issuer01_url = "https://example.com/issuer"
        private const val RAW_ISSUER_CREDENTIAL_INFO = "rawIssuerCredentialInfo"
        private val selectedConfigurationId01 = "selectedConfigurationId01"

        private val deferredCredentialEntity01 = DeferredCredentialEntity(
            credentialId = 1L,
            progressionState = DeferredProgressionState.IN_PROGRESS,
            transactionId = transactionId01,
            endpoint = "https://example",
            pollInterval = 10,
            createdAt = 4L,
            polledAt = 5L,
        )

        private val credentialEntity01 = Credential(
            id = 1L,
            format = CredentialFormat.VC_SD_JWT,
            createdAt = 1L,
            selectedConfigurationId = selectedConfigurationId01,
            issuerUrl = URL(issuer01_url)
        )

        private val credentialAuthenticationWithDpopBinding = CredentialAuthenticationWithDpopBinding(
            credentialAuthentication = CredentialAuthenticationEntity(
                credentialId = credentialEntity01.id,
                tokenType = TokenType.BEARER,
                accessToken = ACCESS_TOKEN,
                refreshToken = REFRESH_TOKEN,
            ),
            dpopBinding = null,
        )

        private val deferredCredentialWithBinding01 = DeferredCredentialWithAuthenticationAndKeyBinding(
            deferredCredential = deferredCredentialEntity01,
            credential = credentialEntity01,
            keyBindings = listOf(),
            authentication = credentialAuthenticationWithDpopBinding,
        )

        private val credential = CredentialResponse.Credential(vcPayload01)

        private val credentialResponse01 = CredentialResponse.VerifiableCredential(
            credentials = listOf(credential),
        )
    }
}

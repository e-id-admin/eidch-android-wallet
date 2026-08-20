package ch.admin.foitt.wallet.platform.batch.domain.usecase.implementation

import android.annotation.SuppressLint
import ch.admin.foitt.openid4vc.domain.model.BatchSize
import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyVerifiedBatchCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.JWSKeyPair
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.AnyCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.BatchCredentialIssuance
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialRequestEncryption
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialResponseEncryption
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.RawAndParsedIssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.keyBinding.BindingKeyPair
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBindingType
import ch.admin.foitt.openid4vc.domain.usecase.FetchCredentialByConfig
import ch.admin.foitt.openid4vc.domain.usecase.FetchRawAndParsedIssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.usecase.GetSignedMetadataDid
import ch.admin.foitt.openid4vc.domain.usecase.GetVerifiableCredentialParams
import ch.admin.foitt.wallet.platform.batch.domain.error.RefreshBatchCredentialsError
import ch.admin.foitt.wallet.platform.batch.domain.usecase.DeleteBundleItemsByAmount
import ch.admin.foitt.wallet.platform.batch.domain.usecase.RefreshBatchCredentials
import ch.admin.foitt.wallet.platform.batch.domain.usecase.implementation.mock.MockBatchRefreshData
import ch.admin.foitt.wallet.platform.batch.domain.usecase.implementation.mock.MockBatchRefreshData.ACCESS_TOKEN
import ch.admin.foitt.wallet.platform.batch.domain.usecase.implementation.mock.MockBatchRefreshData.AUTHENTICATION_ID
import ch.admin.foitt.wallet.platform.batch.domain.usecase.implementation.mock.MockBatchRefreshData.CREDENTIAL_ID
import ch.admin.foitt.wallet.platform.batch.domain.usecase.implementation.mock.MockBatchRefreshData.ISSUER_URL
import ch.admin.foitt.wallet.platform.batch.domain.usecase.implementation.mock.MockBatchRefreshData.KEY_ID
import ch.admin.foitt.wallet.platform.batch.domain.usecase.implementation.mock.MockBatchRefreshData.REFRESH_TOKEN
import ch.admin.foitt.wallet.platform.batch.domain.usecase.implementation.mock.MockBatchRefreshData.SELECTED_CONFIG_ID
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchCredentialResult
import ch.admin.foitt.wallet.platform.credential.domain.usecase.EvaluateBatchSize
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GetCredentialConfig
import ch.admin.foitt.wallet.platform.credential.domain.usecase.HandleBatchCredentialResult
import ch.admin.foitt.wallet.platform.credentialRefresh.domain.model.CredentialRefreshDataError
import ch.admin.foitt.wallet.platform.credentialRefresh.domain.repository.CredentialRefreshDataRepository
import ch.admin.foitt.wallet.platform.credentialRefresh.domain.usecase.GetBindingKeyPair
import ch.admin.foitt.wallet.platform.database.domain.model.DpopBindingEntity
import ch.admin.foitt.wallet.platform.holderBinding.domain.usecase.GenerateDPoPKeyPair
import ch.admin.foitt.wallet.platform.holderBinding.domain.usecase.GenerateProofKeyPairs
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.usecase.GetPayloadEncryption
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.BundleItemRepository
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.DeleteBundleItems
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.DeleteKeyStoreEntry
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IdentityV2TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustRegistryError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ProcessIdentityTrustStatement
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URL
import java.security.KeyPair

@SuppressLint("CheckResult")
class RefreshBatchCredentialsImplTest {

    @MockK
    private lateinit var mockBundleItemRepository: BundleItemRepository

    @MockK
    private lateinit var mockCredentialRefreshDataRepository: CredentialRefreshDataRepository

    @MockK
    private lateinit var mockGetCredentialConfig: GetCredentialConfig

    @MockK
    private lateinit var mockGetPayloadEncryption: GetPayloadEncryption

    @MockK
    private lateinit var mockCredentialRequestEncryption: CredentialRequestEncryption

    @MockK
    private lateinit var mockCredentialResponseEncryption: CredentialResponseEncryption

    @MockK
    private lateinit var mockFetchRawAndParsedIssuerCredentialInfo: FetchRawAndParsedIssuerCredentialInfo

    @MockK
    private lateinit var mockGetVerifiableCredentialParams: GetVerifiableCredentialParams

    @MockK
    private lateinit var mockDeleteBundleItemsByAmount: DeleteBundleItemsByAmount

    @MockK
    private lateinit var mockEvaluateBatchSize: EvaluateBatchSize

    @MockK
    private lateinit var mockGenerateProofKeyPairs: GenerateProofKeyPairs

    @MockK
    private lateinit var mockGetSignedMetadataDid: GetSignedMetadataDid

    @MockK
    private lateinit var mockProcessIdentityTrustStatement: ProcessIdentityTrustStatement

    @MockK
    private lateinit var mockFetchCredentialByConfig: FetchCredentialByConfig

    @MockK
    private lateinit var mockGetBindingKeyPair: GetBindingKeyPair

    @MockK
    private lateinit var mockHandleBatchCredentialResult: HandleBatchCredentialResult

    @MockK
    private lateinit var mockDeleteBundleItems: DeleteBundleItems

    @MockK
    private lateinit var mockGenerateDPoPKeyPair: GenerateDPoPKeyPair

    @MockK
    private lateinit var mockDeleteKeyStoreEntry: DeleteKeyStoreEntry

    @MockK
    private lateinit var mockAnyCredentialConfiguration: AnyCredentialConfiguration

    @MockK
    private lateinit var mockTrustStatementJwt: Jwt

    @MockK
    private lateinit var mockIssuerCredentialInfoJwt: Jwt

    @MockK
    private lateinit var mockTrustStatementV2: IdentityV2TrustStatement

    private lateinit var useCase: RefreshBatchCredentials

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = RefreshBatchCredentialsImpl(
            bundleItemRepository = mockBundleItemRepository,
            credentialRefreshDataRepository = mockCredentialRefreshDataRepository,
            getCredentialConfig = mockGetCredentialConfig,
            getPayloadEncryption = mockGetPayloadEncryption,
            fetchRawAndParsedIssuerCredentialInfo = mockFetchRawAndParsedIssuerCredentialInfo,
            getVerifiableCredentialParams = mockGetVerifiableCredentialParams,
            evaluateBatchSize = mockEvaluateBatchSize,
            deleteBundleItemsByAmount = mockDeleteBundleItemsByAmount,
            generateProofKeyPairs = mockGenerateProofKeyPairs,
            getSignedMetadataDid = mockGetSignedMetadataDid,
            processIdentityTrustStatement = mockProcessIdentityTrustStatement,
            fetchCredentialByConfig = mockFetchCredentialByConfig,
            getBindingKeyPair = mockGetBindingKeyPair,
            handleBatchCredentialResult = mockHandleBatchCredentialResult,
            deleteBundleItems = mockDeleteBundleItems,
            generateDPoPKeyPair = mockGenerateDPoPKeyPair,
            deleteKeyStoreEntry = mockDeleteKeyStoreEntry,
        )
    }

    private fun setupDefaultMocks(
        batchSize: Int = DEFAULT_BATCH_SIZE,
        credentialConfigurations: List<AnyCredentialConfiguration> = listOf(mockAnyCredentialConfiguration)
    ) {
        val batchData = listOf(MockBatchRefreshData.createBatchRefreshData(batchSize = batchSize))
        coEvery { mockCredentialRefreshDataRepository.getAllBatchCredentialRefreshData() } returns Ok(batchData)
        val presentableCount = 1
        coEvery { mockBundleItemRepository.getNeverPresentedCount(CREDENTIAL_ID) } returns Ok(presentableCount)

        val issuerInfo = IssuerCredentialInfo(
            credentialEndpoint = URL("https://issuer.example/credential"),
            nonceEndpoint = URL("https://issuer.example/nonce"),
            credentialIssuer = ISSUER_URL,
            credentialRequestEncryption = mockCredentialRequestEncryption,
            credentialResponseEncryption = mockCredentialResponseEncryption,
            credentialConfigurations = credentialConfigurations,
            display = null,
            batchCredentialIssuance = BatchCredentialIssuance(batchSize = batchSize),
            identityTrustStatement = mockTrustStatementJwt,
        )
        val rawAndParsed = RawAndParsedIssuerCredentialInfo(
            rawIssuerCredentialInfo = mockIssuerCredentialInfoJwt,
            issuerCredentialInfo = issuerInfo,
        )
        coEvery {
            mockFetchRawAndParsedIssuerCredentialInfo(
                issuerEndpoint = ISSUER_URL,
                forceRefresh = true,
            )
        } returns Ok(rawAndParsed)
        coEvery {
            mockGetVerifiableCredentialParams(issuerInfo, any(), any())
        } returns Ok(mockk(relaxed = true))

        coEvery { mockEvaluateBatchSize(any()) } answers {
            Ok(requireNotNull((args.first() as IssuerCredentialInfo).batchCredentialIssuance).batchSize)
        }
        coEvery {
            mockGetCredentialConfig(
                credentials = any(),
                credentialConfigurations = any()
            )
        } returns Ok(mockk(relaxed = true))
        coEvery { mockDeleteBundleItemsByAmount(any(), any()) } returns Ok(Unit)

        coEvery { mockDeleteBundleItems(any()) } returns Ok(0)
        coEvery { mockGenerateProofKeyPairs(any(), any(), ISSUER_DID) } returns Ok(mockk(relaxed = true))
        coEvery { mockGetSignedMetadataDid(any()) } returns Ok(ISSUER_DID)
        coEvery {
            mockProcessIdentityTrustStatement(mockTrustStatementJwt, ISSUER_DID)
        } returns Ok(mockTrustStatementV2)
        coEvery {
            mockGetPayloadEncryption(mockCredentialRequestEncryption, mockCredentialResponseEncryption)
        } returns Ok(mockk(relaxed = true))
        coEvery { mockGetBindingKeyPair(any()) } returns Ok(null)
        coEvery { mockGenerateDPoPKeyPair(any(), ISSUER_DID) } returns Ok(mockk(relaxed = true))
        coJustRun { mockDeleteKeyStoreEntry(any()) }
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Credential without never presented bundle items triggers refresh`() = runTest {
        setupDefaultMocks(credentialConfigurations = emptyList())

        // a fully presented batch has a never presented count of 0 and must trigger a refresh
        coEvery { mockBundleItemRepository.getNeverPresentedCount(CREDENTIAL_ID) } returns Ok(0)

        coEvery {
            mockFetchCredentialByConfig(any(), any(), any(), any())
        } returns Ok(
            AnyVerifiedBatchCredential(
                accessToken = ACCESS_TOKEN,
                refreshToken = REFRESH_TOKEN,
                dpopKeyBinding = null,
                vcSdJwtCredentials = emptyList(),
            )
        )
        coEvery {
            mockBundleItemRepository.getAllByCredentialId(CREDENTIAL_ID)
        } returns Ok(mockk(relaxed = true))
        coEvery {
            mockHandleBatchCredentialResult(CREDENTIAL_ID, any(), DEFAULT_BATCH_SIZE, any(), any(), any(), any())
        } returns Ok(FetchCredentialResult.Credential(CREDENTIAL_ID))

        useCase().assertOk()

        coVerify(exactly = 1) {
            mockFetchRawAndParsedIssuerCredentialInfo(issuerEndpoint = ISSUER_URL, forceRefresh = true)
            mockHandleBatchCredentialResult(any(), any(), any(), any(), any(), any(), any())
        }
    }

    @Test
    fun `Presentable count above threshold does not trigger refresh`() = runTest {
        setupDefaultMocks()

        val presentableCount = 3
        coEvery { mockBundleItemRepository.getNeverPresentedCount(CREDENTIAL_ID) } returns Ok(presentableCount)

        useCase().assertOk()

        coVerify(exactly = 0) {
            mockFetchRawAndParsedIssuerCredentialInfo(issuerEndpoint = any(), forceRefresh = any())
        }
    }

    @Test
    fun `Batch refresh without config change`() = runTest {
        setupDefaultMocks(credentialConfigurations = emptyList())

        coEvery {
            mockFetchCredentialByConfig(any(), any(), any(), any())
        } returns Ok(
            AnyVerifiedBatchCredential(
                accessToken = ACCESS_TOKEN,
                refreshToken = REFRESH_TOKEN,
                dpopKeyBinding = null,
                vcSdJwtCredentials = emptyList(),
            )
        )
        coEvery {
            mockBundleItemRepository.getAllByCredentialId(CREDENTIAL_ID)
        } returns Ok(mockk(relaxed = true))
        coEvery {
            mockHandleBatchCredentialResult(CREDENTIAL_ID, any(), DEFAULT_BATCH_SIZE, any(), any(), any(), any())
        } returns Ok(FetchCredentialResult.Credential(CREDENTIAL_ID))

        useCase().assertOk()

        coVerify(exactly = 1) {
            mockFetchRawAndParsedIssuerCredentialInfo(issuerEndpoint = ISSUER_URL, forceRefresh = true)
            mockGetCredentialConfig(
                credentials = listOf(SELECTED_CONFIG_ID),
                credentialConfigurations = emptyList()
            )
            mockGetVerifiableCredentialParams(any(), any(), any())
            mockGetSignedMetadataDid(any())
            mockGenerateProofKeyPairs(any(), any(), ISSUER_DID)
            mockProcessIdentityTrustStatement(mockTrustStatementJwt, ISSUER_DID)
            mockFetchCredentialByConfig(any(), any(), any(), any())
            mockHandleBatchCredentialResult(any(), any(), any(), any(), any(), any(), any())
            mockDeleteBundleItems(any())
        }

        coVerify(exactly = 0) {
            mockDeleteBundleItemsByAmount(any(), any())
            mockCredentialRefreshDataRepository.updateBatchSize(CREDENTIAL_ID, DEFAULT_BATCH_SIZE)
        }
    }

    @Test
    fun `Batch refresh reuses stored software dpop key binding`() = runTest {
        val batchSize: BatchSize = 5

        setupDefaultMocks(batchSize = batchSize, credentialConfigurations = emptyList())

        val presentableCount = 1
        val publicKey = byteArrayOf(1, 2, 3)
        val privateKey = byteArrayOf(4, 5, 6)
        val batchRefreshData = listOf(
            MockBatchRefreshData.createBatchRefreshData(
                batchSize = batchSize,
                dpopBinding = DpopBindingEntity(
                    id = KEY_ID,
                    credentialAuthenticationId = AUTHENTICATION_ID,
                    algorithm = SigningAlgorithm.ES256.name,
                    bindingType = KeyBindingType.SOFTWARE,
                    publicKey = publicKey,
                    privateKey = privateKey,
                ),
            )
        )

        coEvery { mockCredentialRefreshDataRepository.getAllBatchCredentialRefreshData() } returns Ok(batchRefreshData)

        val softwareKeyPair = mockk<KeyPair>(relaxed = true)

        coEvery {
            mockGetBindingKeyPair(batchRefreshData.first().authentication)
        } returns Ok(
            BindingKeyPair(
                keyPair = JWSKeyPair(
                    algorithm = SigningAlgorithm.ES256,
                    keyPair = softwareKeyPair,
                    keyId = KEY_ID,
                    bindingType = KeyBindingType.SOFTWARE,
                ),
                attestationJwt = null,
            )
        )
        coEvery {
            mockFetchCredentialByConfig(
                any(),
                any(),
                any(),
                match {
                    it.keyPair.algorithm == SigningAlgorithm.ES256 &&
                        it.keyPair.keyId == KEY_ID &&
                        it.keyPair.bindingType == KeyBindingType.SOFTWARE &&
                        it.attestationJwt == null
                }
            )
        } returns Ok(
            AnyVerifiedBatchCredential(
                accessToken = "access-token",
                refreshToken = REFRESH_TOKEN,
                dpopKeyBinding = null,
                vcSdJwtCredentials = emptyList(),
            )
        )
        coEvery { mockBundleItemRepository.getAllByCredentialId(CREDENTIAL_ID) } returns Ok(mockk(relaxed = true))
        coEvery {
            mockHandleBatchCredentialResult(CREDENTIAL_ID, any(), batchSize, any(), any(), any(), any())
        } returns Ok(FetchCredentialResult.Credential(CREDENTIAL_ID))
        coEvery { mockDeleteBundleItems(any()) } returns Ok(presentableCount)

        useCase().assertOk()

        coVerify(exactly = 1) {
            mockGetBindingKeyPair(batchRefreshData.first().authentication)
            mockFetchCredentialByConfig(any(), any(), any(), any())
        }
    }

    @Test
    fun `New batch size is lower, so presentable count is higher than new threshold, so no refresh is needed`() = runTest {
        val newBatchSize: BatchSize = 3
        setupDefaultMocks(batchSize = newBatchSize)
        val presentableCount = 1

        coEvery { mockBundleItemRepository.getNeverPresentedCount(CREDENTIAL_ID) } returns Ok(presentableCount)

        useCase().assertOk()

        coVerify(exactly = 0) {
            mockFetchRawAndParsedIssuerCredentialInfo(any(), any())
            mockGetCredentialConfig(any(), any())
            mockCredentialRefreshDataRepository.updateBatchSize(any(), any())
            mockDeleteBundleItemsByAmount(any(), any())
            mockGetVerifiableCredentialParams(any(), any(), any())
            mockGenerateProofKeyPairs(any(), any(), ISSUER_DID)
            mockFetchCredentialByConfig(any(), any(), any(), any())
            mockHandleBatchCredentialResult(any(), any(), any(), any(), any(), any(), any())
            mockDeleteBundleItems(any())
        }
    }

    @Test
    fun `New batch size is lower, so presentable count is higher than new batch size, so old bundle items are deleted`() = runTest {
        val oldBatchSize: BatchSize = 100
        val newBatchSize: BatchSize = 10
        setupDefaultMocks(batchSize = newBatchSize)
        val presentableCount = 20
        val batchRefreshData = listOf(MockBatchRefreshData.createBatchRefreshData(batchSize = oldBatchSize))

        coEvery { mockCredentialRefreshDataRepository.getAllBatchCredentialRefreshData() } returns Ok(batchRefreshData)
        coEvery { mockBundleItemRepository.getNeverPresentedCount(CREDENTIAL_ID) } returns Ok(presentableCount)
        coEvery { mockCredentialRefreshDataRepository.updateBatchSize(CREDENTIAL_ID, newBatchSize) } returns Ok(1)

        useCase().assertOk()

        coVerify(exactly = 1) {
            mockFetchRawAndParsedIssuerCredentialInfo(issuerEndpoint = ISSUER_URL, forceRefresh = true)
            mockGetCredentialConfig(
                credentials = listOf(SELECTED_CONFIG_ID),
                credentialConfigurations = listOf(mockAnyCredentialConfiguration)
            )
            mockDeleteBundleItemsByAmount(CREDENTIAL_ID, any())
            mockCredentialRefreshDataRepository.updateBatchSize(CREDENTIAL_ID, newBatchSize)
        }

        coVerify(exactly = 0) {
            mockGetVerifiableCredentialParams(any(), any(), any())
            mockGenerateProofKeyPairs(any(), any(), ISSUER_DID)
            mockFetchCredentialByConfig(any(), any(), any(), any())
            mockHandleBatchCredentialResult(any(), any(), any(), any(), any(), any(), any())
            mockDeleteBundleItems(any())
        }
    }

    @Test
    fun `Error from getAll is mapped to RefreshBatchCredentialsError`() = runTest {
        setupDefaultMocks()

        val exception = IllegalStateException("verifiable credential repo error")
        coEvery {
            mockCredentialRefreshDataRepository.getAllBatchCredentialRefreshData()
        } returns Err(CredentialRefreshDataError.Unexpected(exception))

        useCase().assertErrorType(RefreshBatchCredentialsError.Unexpected::class)
    }

    @Test
    fun `Error from getNeverPresentedCount skips the credential without failing the refresh flow`() = runTest {
        setupDefaultMocks()

        val exception = IllegalStateException("bundle item repo error")
        coEvery {
            mockBundleItemRepository.getNeverPresentedCount(any())
        } returns Err(SsiError.Unexpected(exception))

        useCase().assertOk()

        coVerify(exactly = 0) {
            mockFetchRawAndParsedIssuerCredentialInfo(issuerEndpoint = any(), forceRefresh = any())
        }
    }

    @Test
    fun `Error from getting signed metadata did is mapped`() = runTest {
        setupDefaultMocks()

        val exception = IllegalStateException("signed metadata error")
        coEvery {
            mockGetSignedMetadataDid(any())
        } returns Err(CredentialOfferError.Unexpected(exception))

        useCase().assertErrorType(RefreshBatchCredentialsError.Unexpected::class)

        coVerify(exactly = 0) {
            mockProcessIdentityTrustStatement(any(), any())
            mockBundleItemRepository.getAllByCredentialId(any())
            mockFetchCredentialByConfig(any(), any(), any(), any())
            mockHandleBatchCredentialResult(any(), any(), any(), any(), any(), any(), any())
            mockDeleteBundleItems(any())
        }
    }

    @Test
    fun `Error from processing identity trust statement is mapped`() = runTest {
        setupDefaultMocks()

        val exception = IllegalStateException("signed metadata error")
        coEvery {
            mockProcessIdentityTrustStatement(any(), any())
        } returns Err(TrustRegistryError.Unexpected(exception))

        useCase().assertErrorType(RefreshBatchCredentialsError.Unexpected::class)

        coVerify(exactly = 0) {
            mockBundleItemRepository.getAllByCredentialId(any())
            mockFetchCredentialByConfig(any(), any(), any(), any())
            mockHandleBatchCredentialResult(any(), any(), any(), any(), any(), any(), any())
            mockDeleteBundleItems(any())
        }
    }

    @Test
    fun `Generated hardware keys are cleaned up when fetching the credential fails`() = runTest {
        setupDefaultMocks()

        coEvery { mockGenerateProofKeyPairs(any(), any(), ISSUER_DID) } returns Ok(listOf(bindingKeyPair(keyId = PROOF_KEY_ID)))
        coEvery { mockGenerateDPoPKeyPair(any(), ISSUER_DID) } returns Ok(bindingKeyPair(keyId = DPOP_KEY_ID))
        coEvery {
            mockFetchCredentialByConfig(any(), any(), any(), any())
        } returns Err(CredentialOfferError.NetworkInfoError)

        useCase().assertErrorType(RefreshBatchCredentialsError.Unexpected::class)

        coVerify(exactly = 1) {
            mockDeleteKeyStoreEntry(PROOF_KEY_ID)
            mockDeleteKeyStoreEntry(DPOP_KEY_ID)
        }
        coVerify(exactly = 0) {
            mockHandleBatchCredentialResult(any(), any(), any(), any(), any(), any(), any())
            mockDeleteBundleItems(any())
        }
    }

    @Test
    fun `Generated software keys are not deleted from the key store on failure`() = runTest {
        setupDefaultMocks()

        coEvery {
            mockGenerateProofKeyPairs(any(), any(), ISSUER_DID)
        } returns Ok(listOf(bindingKeyPair(keyId = PROOF_KEY_ID, bindingType = KeyBindingType.SOFTWARE)))
        coEvery {
            mockGenerateDPoPKeyPair(any(), ISSUER_DID)
        } returns Ok(bindingKeyPair(keyId = DPOP_KEY_ID, bindingType = KeyBindingType.SOFTWARE))
        coEvery {
            mockFetchCredentialByConfig(any(), any(), any(), any())
        } returns Err(CredentialOfferError.NetworkInfoError)

        useCase().assertErrorType(RefreshBatchCredentialsError.Unexpected::class)

        coVerify(exactly = 0) {
            mockDeleteKeyStoreEntry(any())
        }
    }

    @Test
    fun `Pre-existing dpop binding key is not deleted on failure`() = runTest {
        setupDefaultMocks()

        coEvery { mockGenerateProofKeyPairs(any(), any(), ISSUER_DID) } returns Ok(listOf(bindingKeyPair(keyId = PROOF_KEY_ID)))
        coEvery { mockGetBindingKeyPair(any()) } returns Ok(bindingKeyPair(keyId = DPOP_KEY_ID))
        coEvery {
            mockFetchCredentialByConfig(any(), any(), any(), any())
        } returns Err(CredentialOfferError.NetworkInfoError)

        useCase().assertErrorType(RefreshBatchCredentialsError.Unexpected::class)

        coVerify(exactly = 1) { mockDeleteKeyStoreEntry(PROOF_KEY_ID) }
        coVerify(exactly = 0) {
            mockDeleteKeyStoreEntry(DPOP_KEY_ID)
            mockGenerateDPoPKeyPair(any(), ISSUER_DID)
        }
    }

    @Test
    fun `Keys are not deleted when a failure happens after the credential was saved`() = runTest {
        setupDefaultMocks(credentialConfigurations = emptyList())

        coEvery { mockGenerateProofKeyPairs(any(), any(), ISSUER_DID) } returns Ok(listOf(bindingKeyPair(keyId = PROOF_KEY_ID)))
        coEvery {
            mockFetchCredentialByConfig(any(), any(), any(), any())
        } returns Ok(
            AnyVerifiedBatchCredential(
                accessToken = ACCESS_TOKEN,
                refreshToken = REFRESH_TOKEN,
                dpopKeyBinding = null,
                vcSdJwtCredentials = emptyList(),
            )
        )
        coEvery {
            mockBundleItemRepository.getAllByCredentialId(CREDENTIAL_ID)
        } returns Ok(mockk(relaxed = true))
        coEvery {
            mockHandleBatchCredentialResult(CREDENTIAL_ID, any(), DEFAULT_BATCH_SIZE, any(), any(), any(), any())
        } returns Ok(FetchCredentialResult.Credential(CREDENTIAL_ID))
        coEvery {
            mockDeleteBundleItems(any())
        } returns Err(SsiError.Unexpected(IllegalStateException("db error")))

        useCase().assertErrorType(RefreshBatchCredentialsError.Unexpected::class)

        coVerify(exactly = 0) {
            mockDeleteKeyStoreEntry(any())
        }
    }

    @Test
    fun `A refresh started while another one is in flight is skipped`() = runTest {
        setupDefaultMocks()

        // Hold the first invocation inside the use case until the second one had its chance to start.
        val firstCallStarted = CompletableDeferred<Unit>()
        val releaseFirstCall = CompletableDeferred<Unit>()
        coEvery { mockCredentialRefreshDataRepository.getAllBatchCredentialRefreshData() } coAnswers {
            firstCallStarted.complete(Unit)
            releaseFirstCall.await()
            Ok(emptyList())
        }

        val firstCall = async { useCase() }
        firstCallStarted.await()

        useCase().assertOk()
        releaseFirstCall.complete(Unit)
        firstCall.await().assertOk()

        coVerify(exactly = 1) {
            mockCredentialRefreshDataRepository.getAllBatchCredentialRefreshData()
        }
    }

    @Test
    fun `A refresh can run again once the previous one completed`() = runTest {
        setupDefaultMocks()
        coEvery { mockCredentialRefreshDataRepository.getAllBatchCredentialRefreshData() } returns Ok(emptyList())

        useCase().assertOk()
        useCase().assertOk()

        coVerify(exactly = 2) {
            mockCredentialRefreshDataRepository.getAllBatchCredentialRefreshData()
        }
    }

    @Test
    fun `A failing refresh releases the lock for the next one`() = runTest {
        setupDefaultMocks()
        coEvery {
            mockCredentialRefreshDataRepository.getAllBatchCredentialRefreshData()
        } returns Err(CredentialRefreshDataError.Unexpected(IllegalStateException("db error")))

        useCase().assertErrorType(RefreshBatchCredentialsError.Unexpected::class)
        useCase().assertErrorType(RefreshBatchCredentialsError.Unexpected::class)

        coVerify(exactly = 2) {
            mockCredentialRefreshDataRepository.getAllBatchCredentialRefreshData()
        }
    }

    private fun bindingKeyPair(
        keyId: String,
        bindingType: KeyBindingType = KeyBindingType.HARDWARE,
    ) = BindingKeyPair(
        keyPair = JWSKeyPair(
            algorithm = SigningAlgorithm.ES256,
            keyPair = mockk<KeyPair>(relaxed = true),
            keyId = keyId,
            bindingType = bindingType,
        ),
        attestationJwt = null,
    )

    private companion object {
        const val ISSUER_DID = "did"
        const val DEFAULT_BATCH_SIZE = 10
        const val PROOF_KEY_ID = "proof-key-id"
        const val DPOP_KEY_ID = "dpop-key-id"
    }
}

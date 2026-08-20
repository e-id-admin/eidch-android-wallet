package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import android.annotation.SuppressLint
import ch.admin.foitt.openid4vc.domain.model.DeferredCredential
import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.TokenType
import ch.admin.foitt.openid4vc.domain.model.VerifiableCredentialParams
import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyVerifiedCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOffer
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.JWSKeyPair
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.IssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.RawAndParsedIssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.keyBinding.BindingKeyPair
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBinding
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBindingType
import ch.admin.foitt.openid4vc.domain.model.payloadEncryption.PayloadEncryption
import ch.admin.foitt.openid4vc.domain.model.payloadEncryption.PayloadEncryptionKeyPair
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.openid4vc.domain.usecase.FetchCredentialByConfig
import ch.admin.foitt.openid4vc.domain.usecase.FetchRawAndParsedIssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.usecase.GetSignedMetadataDid
import ch.admin.foitt.openid4vc.domain.usecase.GetVerifiableCredentialParams
import ch.admin.foitt.wallet.platform.actorEnvironment.domain.model.ActorEnvironment
import ch.admin.foitt.wallet.platform.actorEnvironment.domain.usecase.GetActorEnvironment
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchCredentialResult
import ch.admin.foitt.wallet.platform.credential.domain.usecase.EvaluateBatchSize
import ch.admin.foitt.wallet.platform.credential.domain.usecase.FetchAndSaveCredential
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GetCredentialConfig
import ch.admin.foitt.wallet.platform.credential.domain.usecase.HandleBatchCredentialResult
import ch.admin.foitt.wallet.platform.credential.domain.usecase.HandleCredentialResult
import ch.admin.foitt.wallet.platform.credential.domain.usecase.HandleDeferredCredentialResult
import ch.admin.foitt.wallet.platform.credential.domain.usecase.ValidateIssuerCredentialInfo
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.CREDENTIAL_ISSUER
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.credentialConfig
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.mockIdentityJwt
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.multipleConfigCredentialInformation
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.multipleIdentifiersCredentialOffer
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.noConfigCredentialInformation
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.noIdentifierCredentialOffer
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.noMatchingIdentifierCredentialOffer
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.oneConfigCredentialInformation
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.oneIdentifierCredentialOffer
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.proofTypeConfigHardwareBinding
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.proofTypeConfigSoftwareBinding
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.requestEncryption
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.responseEncryption
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.validHardwareKeyPair
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.validSoftwareKeyPair
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.verifiableCredentialParamsHardwareBinding
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.verifiableCredentialParamsNoBinding
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.verifiableCredentialParamsSoftwareBinding
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.holderBinding.domain.model.HolderBindingError
import ch.admin.foitt.wallet.platform.holderBinding.domain.usecase.GenerateDPoPKeyPair
import ch.admin.foitt.wallet.platform.holderBinding.domain.usecase.GenerateProofKeyPairs
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.model.PayloadEncryptionError
import ch.admin.foitt.wallet.platform.payloadEncryption.domain.usecase.GetPayloadEncryption
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialOfferRepository
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IdentityV2TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustRegistryError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ProcessIdentityTrustStatement
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import ch.admin.foitt.wallet.util.assertSuccessType
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json.Default.parseToJsonElement
import kotlinx.serialization.json.jsonObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URL
import java.time.Instant
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError as OpenIdCredentialOfferError

class FetchAndSaveCredentialImplTest {
    @MockK
    private lateinit var mockFetchRawAndParsedCredentialInfo: FetchRawAndParsedIssuerCredentialInfo

    @MockK
    private lateinit var mockGetSignedMetadataDid: GetSignedMetadataDid

    @MockK
    private lateinit var mockProcessIdentityTrustStatement: ProcessIdentityTrustStatement

    @MockK
    private lateinit var mockValidateIssuerCredentialInfo: ValidateIssuerCredentialInfo

    @MockK
    private lateinit var mockEnvironmentSetupRepository: EnvironmentSetupRepository

    @MockK
    private lateinit var mockGetVerifiableCredentialParams: GetVerifiableCredentialParams

    @MockK
    private lateinit var mockGenerateProofKeyPairs: GenerateProofKeyPairs

    @MockK
    private lateinit var mockFetchCredentialByConfig: FetchCredentialByConfig

    @MockK
    private lateinit var mockCredentialOfferRepository: CredentialOfferRepository

    @MockK
    private lateinit var mockVcSdJwtCredential: VcSdJwtCredential

    @MockK
    private lateinit var mockIdentityTrustStatement: IdentityV2TrustStatement

    @MockK
    private lateinit var mockGetPayloadEncryption: GetPayloadEncryption

    @MockK
    private lateinit var mockGetCredentialConfig: GetCredentialConfig

    @MockK
    private lateinit var mockEvaluateBatchSize: EvaluateBatchSize

    @MockK
    private lateinit var mockHandleCredentialResult: HandleCredentialResult

    @MockK
    private lateinit var mockHandleBatchCredentialResult: HandleBatchCredentialResult

    @MockK
    private lateinit var mockHandleDeferredCredentialResult: HandleDeferredCredentialResult

    @MockK
    private lateinit var mockGenerateDPoPKeyPair: GenerateDPoPKeyPair

    @MockK
    private lateinit var mockGetActorEnvironment: GetActorEnvironment

    @MockK
    private lateinit var mockIssuerCredentialInfoJwt: Jwt

    @MockK
    private lateinit var mockAnyVerifiedCredential: AnyVerifiedCredential

    private lateinit var useCase: FetchAndSaveCredential

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = FetchAndSaveCredentialImpl(
            fetchRawAndParsedIssuerCredentialInfo = mockFetchRawAndParsedCredentialInfo,
            getSignedMetadataDid = mockGetSignedMetadataDid,
            processIdentityTrustStatement = mockProcessIdentityTrustStatement,
            validateIssuerCredentialInfo = mockValidateIssuerCredentialInfo,
            getPayloadEncryption = mockGetPayloadEncryption,
            getVerifiableCredentialParams = mockGetVerifiableCredentialParams,
            getCredentialConfig = mockGetCredentialConfig,
            evaluateBatchSize = mockEvaluateBatchSize,
            generateProofKeyPairs = mockGenerateProofKeyPairs,
            fetchCredentialByConfig = mockFetchCredentialByConfig,
            handleCredentialResult = mockHandleCredentialResult,
            handleBatchCredentialResult = mockHandleBatchCredentialResult,
            handleDeferredCredentialResult = mockHandleDeferredCredentialResult,
            environmentSetupRepository = mockEnvironmentSetupRepository,
            generateDPoPKeyPair = mockGenerateDPoPKeyPair,
            getActorEnvironment = mockGetActorEnvironment,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    //region Credential
    @SuppressLint("CheckResult")
    @Test
    fun `Fetching and saving the credential runs specific things`() = runTest {
        val result = useCase(oneIdentifierCredentialOffer)

        val credentialId = result.assertSuccessType(FetchCredentialResult.Credential::class)
        assertEquals(CREDENTIAL_ID, credentialId.credentialId)

        coVerify {
            mockFetchRawAndParsedCredentialInfo(
                issuerEndpoint = CREDENTIAL_ISSUER,
                forceRefresh = true,
            )
            mockProcessIdentityTrustStatement(
                identityTrustStatementJwt = mockIdentityJwt,
                actorDid = ISSUER_DID,
            )
            mockValidateIssuerCredentialInfo(oneConfigCredentialInformation)
            mockGetPayloadEncryption(
                requestEncryption = requestEncryption,
                responseEncryption = responseEncryption,
            )
            mockGetVerifiableCredentialParams(
                issuerCredentialInfo = oneConfigCredentialInformation,
                credentialConfiguration = credentialConfig,
                credentialOffer = oneIdentifierCredentialOffer,
            )
            mockGetVerifiableCredentialParams(
                oneConfigCredentialInformation,
                credentialConfig,
                oneIdentifierCredentialOffer,
            )
            mockFetchCredentialByConfig(
                verifiableCredentialParams = verifiableCredentialParamsHardwareBinding,
                bindingKeyPairs = listOf(
                    BindingKeyPair(
                        validHardwareKeyPair.keyPair,
                        validHardwareKeyPair.attestationJwt,
                    ),
                ),
                payloadEncryption = PayloadEncryption(
                    requestEncryption = requestEncryption,
                    responseEncryption = responseEncryption,
                    responseEncryptionKeyPair = payloadEncryptionKeyPair,
                ),
            )
            mockHandleCredentialResult(
                issuerUrl = any(),
                anyVerifiedCredential = mockAnyVerifiedCredential,
                identityTrustStatement = mockIdentityTrustStatement,
                rawAndParsedCredentialInfo = any(),
                credentialConfig = credentialConfig,
            )
        }
    }

    @Test
    fun `Fetching and saving credential for offer with one identifier and one matching config returns a valid id`() = runTest {
        setupDefaultMocks(
            credentialOffer = oneIdentifierCredentialOffer,
            credentialInfo = oneConfigCredentialInformation,
        )

        val result = useCase(oneIdentifierCredentialOffer)

        val credentialResult = result.assertSuccessType(FetchCredentialResult.Credential::class)
        assertEquals(CREDENTIAL_ID, credentialResult.credentialId)
    }

    @Test
    fun `Fetching and saving credential for offer with multiple identifiers and multiple matching configs returns a valid id for first identifier`() = runTest {
        setupDefaultMocks(
            credentialOffer = multipleIdentifiersCredentialOffer,
            credentialInfo = multipleConfigCredentialInformation,
        )

        val result = useCase(multipleIdentifiersCredentialOffer)

        val credentialResult = result.assertSuccessType(FetchCredentialResult.Credential::class)
        assertEquals(CREDENTIAL_ID, credentialResult.credentialId)
    }

    @Test
    fun `Fetching and saving credential for offer with multiple identifiers and one matching config returns a valid id`() = runTest {
        setupDefaultMocks(
            credentialOffer = multipleIdentifiersCredentialOffer,
            credentialInfo = oneConfigCredentialInformation,
        )

        val result = useCase(multipleIdentifiersCredentialOffer)

        val credentialResult = result.assertSuccessType(FetchCredentialResult.Credential::class)
        assertEquals(CREDENTIAL_ID, credentialResult.credentialId)
    }

    @Test
    fun `Fetching and saving credential for offer with one identifier and multiple matching configs returns a valid id`() = runTest {
        setupDefaultMocks(
            credentialOffer = oneIdentifierCredentialOffer,
            credentialInfo = multipleConfigCredentialInformation,
        )

        val result = useCase(oneIdentifierCredentialOffer)

        val credentialResult = result.assertSuccessType(FetchCredentialResult.Credential::class)
        assertEquals(CREDENTIAL_ID, credentialResult.credentialId)
    }

    @Test
    fun `Fetching and saving credential for offer with no matching identifier returns an error`() = runTest {
        setupDefaultMocks(
            credentialOffer = noMatchingIdentifierCredentialOffer,
            credentialInfo = multipleConfigCredentialInformation,
        )
        coEvery {
            mockGetCredentialConfig(
                credentials = noMatchingIdentifierCredentialOffer.credentialConfigurationIds,
                credentialConfigurations = multipleConfigCredentialInformation.credentialConfigurations
            )
        } returns Err(CredentialError.UnsupportedCredentialIdentifier)

        val result = useCase(noMatchingIdentifierCredentialOffer)

        result.assertErrorType(CredentialError.UnsupportedCredentialIdentifier::class)
    }

    @Test
    fun `Fetching and saving credential for offer with no identifier returns an error`() = runTest {
        setupDefaultMocks(
            credentialOffer = noIdentifierCredentialOffer,
            credentialInfo = multipleConfigCredentialInformation,
        )
        coEvery {
            mockGetCredentialConfig(
                credentials = noIdentifierCredentialOffer.credentialConfigurationIds,
                credentialConfigurations = multipleConfigCredentialInformation.credentialConfigurations
            )
        } returns Err(CredentialError.UnsupportedCredentialIdentifier)

        val result = useCase(noIdentifierCredentialOffer)

        result.assertErrorType(CredentialError.UnsupportedCredentialIdentifier::class)
    }

    @Test
    fun `Fetching and saving credential for information with no config returns an error`() = runTest {
        setupDefaultMocks(
            credentialOffer = multipleIdentifiersCredentialOffer,
            credentialInfo = noConfigCredentialInformation,
        )
        coEvery {
            mockGetCredentialConfig(
                credentials = multipleIdentifiersCredentialOffer.credentialConfigurationIds,
                credentialConfigurations = noConfigCredentialInformation.credentialConfigurations
            )
        } returns Err(CredentialError.UnsupportedCredentialIdentifier)

        val result = useCase(multipleIdentifiersCredentialOffer)

        result.assertErrorType(CredentialError.UnsupportedCredentialIdentifier::class)
    }

    @Test
    fun `Fetching and saving credential maps errors from Fetching issuer credential information`() = runTest {
        val exception = IllegalStateException()
        coEvery {
            mockFetchRawAndParsedCredentialInfo(any(), any())
        } returns Err(OpenIdCredentialOfferError.Unexpected(exception))

        val result = useCase(oneIdentifierCredentialOffer)

        val error = result.assertErrorType(CredentialError.Unexpected::class)
        assertEquals(exception, error.cause)
    }

    @Test
    fun `Fetching and saving credential maps errors from getting the signed metadata did`() = runTest {
        val exception = IllegalStateException()
        coEvery {
            mockGetSignedMetadataDid(any())
        } returns Err(OpenIdCredentialOfferError.Unexpected(exception))

        val result = useCase(oneIdentifierCredentialOffer)

        val error = result.assertErrorType(CredentialError.Unexpected::class)
        assertEquals(exception, error.cause)
    }

    @Test
    fun `Fetching and saving credential maps errors from processing the identity trust statement to unverified verifier`() = runTest {
        val exception = IllegalStateException()
        coEvery {
            mockProcessIdentityTrustStatement(any(), any())
        } returns Err(TrustRegistryError.Unexpected(exception))

        useCase(oneIdentifierCredentialOffer).assertErrorType(CredentialError.UnverifiedIssuer::class)
    }

    @Test
    fun `Fetching and saving credential maps errors from validating issuer credential information`() = runTest {
        coEvery {
            mockValidateIssuerCredentialInfo(any())
        } returns false

        useCase(oneIdentifierCredentialOffer).assertErrorType(CredentialError.InvalidIssuerCredentialInfo::class)
    }

    @Test
    fun `Fetching and saving credential maps errors from creating payload encryption type`() = runTest {
        coEvery {
            mockGetPayloadEncryption(any(), any())
        } returns Err(PayloadEncryptionError.IncompatibleDeviceProofKeyStorage)

        useCase(oneIdentifierCredentialOffer).assertErrorType(CredentialError.IncompatibleDeviceKeyStorage::class)
    }

    @Test
    fun `Fetching and saving credential maps errors from fetching verifiable credential params`() = runTest {
        coEvery {
            mockGetVerifiableCredentialParams(any(), any(), any())
        } returns Err(OpenIdCredentialOfferError.UnsupportedProofType)

        useCase(oneIdentifierCredentialOffer).assertErrorType(CredentialError.UnsupportedProofType::class)
    }

    @Test
    fun `Fetching and saving credential generates a key pair for hardware bound credentials`() = runTest {
        setupDefaultMocks()

        useCase(oneIdentifierCredentialOffer).assertOk()

        coVerify(exactly = 1) {
            mockGenerateProofKeyPairs(1, proofTypeConfigHardwareBinding, ISSUER_DID)
        }
    }

    @Test
    fun `Fetching and saving credential generates a key pair for software bound credentials`() = runTest {
        setupDefaultMocks(verifiableCredentialParams = verifiableCredentialParamsSoftwareBinding)

        useCase(oneIdentifierCredentialOffer).assertOk()

        coVerify(exactly = 1) {
            mockGenerateProofKeyPairs(1, proofTypeConfigSoftwareBinding, ISSUER_DID)
        }
    }

    @Test
    fun `Fetching and saving credential does not generate a key pair for credentials without binding`() = runTest {
        setupDefaultMocks(verifiableCredentialParams = verifiableCredentialParamsNoBinding)

        val result = useCase(oneIdentifierCredentialOffer)

        result.assertOk()

        coVerify(exactly = 0) {
            mockGenerateProofKeyPairs(1, any(), any())
        }
    }

    @Test
    fun `Fetching and saving credential maps errors from generating the proof key pair`() = runTest {
        coEvery {
            mockGenerateProofKeyPairs(1, any(), any())
        } returns Err(HolderBindingError.IncompatibleDeviceProofKeyStorage)

        useCase(oneIdentifierCredentialOffer).assertErrorType(CredentialError.IncompatibleDeviceKeyStorage::class)
    }

    @Test
    fun `Fetching and saving credential maps errors from Fetching and saving credential by config`() = runTest {
        val exception = IllegalStateException()
        coEvery {
            mockFetchCredentialByConfig(any(), any(), any(), any())
        } returns Err(OpenIdCredentialOfferError.Unexpected(exception))

        val result = useCase(oneIdentifierCredentialOffer)

        val error = result.assertErrorType(CredentialError.Unexpected::class)
        assertEquals(exception, error.cause)
    }

    @Test
    fun `Fetching and saving credential where metadata did and credential did do not match returns an unverified verifier error`() = runTest {
        every { mockVcSdJwtCredential.issuer } returns "other did"

        useCase(oneIdentifierCredentialOffer).assertErrorType(CredentialError.UnverifiedIssuer::class)
    }

    @Test
    fun `Fetching and saving credential maps errors from handling the credential result`() = runTest {
        val exception = IllegalStateException("credential error")
        coEvery {
            mockHandleCredentialResult(any(), any(), any(), any(), any(), any())
        } returns Err(CredentialError.Unexpected(exception))

        val result = useCase(oneIdentifierCredentialOffer)

        val error = result.assertErrorType(CredentialError.Unexpected::class)
        assertEquals(exception, error.cause)
    }
    //endregion

    //region DeferredCredential
    @SuppressLint("CheckResult")
    @Test
    fun `Fetching and saving a deferred credential runs specific steps`() = runTest {
        coEvery {
            mockFetchCredentialByConfig(any(), any(), any(), any())
        } returns Ok(deferredCredential)

        val result = useCase(oneIdentifierCredentialOffer)

        val deferredResult = result.assertSuccessType(FetchCredentialResult.DeferredCredential::class)
        assertEquals(DEFERRED_CREDENTIAL_ID, deferredResult.credentialId)

        coVerifyOrder {
            mockFetchRawAndParsedCredentialInfo(
                issuerEndpoint = CREDENTIAL_ISSUER,
                forceRefresh = true,
            )
            mockGetVerifiableCredentialParams(
                issuerCredentialInfo = oneConfigCredentialInformation,
                credentialConfiguration = credentialConfig,
                credentialOffer = oneIdentifierCredentialOffer
            )
            mockGenerateProofKeyPairs(1, proofTypeConfigHardwareBinding, ISSUER_DID)
            mockFetchCredentialByConfig(
                verifiableCredentialParamsHardwareBinding,
                listOf(
                    BindingKeyPair(
                        validHardwareKeyPair.keyPair,
                        validHardwareKeyPair.attestationJwt
                    )
                ),
                PayloadEncryption(
                    requestEncryption = requestEncryption,
                    responseEncryption = responseEncryption,
                    responseEncryptionKeyPair = payloadEncryptionKeyPair,
                ),
                any(),
            )
            mockHandleDeferredCredentialResult(
                issuerUrl = CREDENTIAL_ISSUER,
                deferredCredential = deferredCredential,
                rawAndParsedCredentialInfo = RawAndParsedIssuerCredentialInfo(
                    issuerCredentialInfo = oneConfigCredentialInformation,
                    rawIssuerCredentialInfo = mockIssuerCredentialInfoJwt
                ),
                credentialConfig = credentialConfig,
            )
        }
    }

    @Test
    fun `Errors from the saveDeferredCredentialOffer() call are mapped`() = runTest {
        coEvery {
            mockFetchCredentialByConfig(any(), any(), any(), any())
        } returns Ok(deferredCredential)

        val exception = Exception("my exception")
        coEvery {
            mockHandleDeferredCredentialResult(
                issuerUrl = CREDENTIAL_ISSUER,
                deferredCredential = deferredCredential,
                rawAndParsedCredentialInfo = RawAndParsedIssuerCredentialInfo(
                    issuerCredentialInfo = oneConfigCredentialInformation,
                    rawIssuerCredentialInfo = mockIssuerCredentialInfoJwt
                ),
                credentialConfig = credentialConfig,
            )
        } returns Err(CredentialError.Unexpected(exception))

        val error = useCase(oneIdentifierCredentialOffer).assertErrorType(CredentialError.Unexpected::class)
        assertEquals(exception.message, error.cause?.message)
    }

    @Test
    fun `A deferred credential without key binding is accepted`() = runTest {
        coEvery {
            mockFetchCredentialByConfig(any(), any(), any(), any())
        } returns Ok(deferredCredential.copy(keyBindings = null))

        val result = useCase(oneIdentifierCredentialOffer)

        val deferredResult = result.assertSuccessType(FetchCredentialResult.DeferredCredential::class)
        assertEquals(DEFERRED_CREDENTIAL_ID, deferredResult.credentialId)
    }

    @Test
    fun `Fetching and saving credential maps errors from handling the deferred credential result`() = runTest {
        coEvery {
            mockFetchCredentialByConfig(any(), any(), any(), any())
        } returns Ok(deferredCredential)

        val exception = IllegalStateException("credential error")
        coEvery {
            mockHandleDeferredCredentialResult(any(), any(), any(), any())
        } returns Err(CredentialError.Unexpected(exception))

        val result = useCase(oneIdentifierCredentialOffer)

        val error = result.assertErrorType(CredentialError.Unexpected::class)
        assertEquals(exception, error.cause)
    }
    //endregion

    @Test
    fun `Issuer with EXTERNAL actor environment returns UnknownRegistry error`() = runTest {
        coEvery { mockGetActorEnvironment(any()) } returns ActorEnvironment.EXTERNAL

        useCase(oneIdentifierCredentialOffer).assertErrorType(CredentialError.UnknownRegistry::class)
    }

    @Test
    fun `Issuer with BETA actor environment returns Ok`() = runTest {
        coEvery { mockGetActorEnvironment(any()) } returns ActorEnvironment.BETA

        useCase(oneIdentifierCredentialOffer).assertOk()
    }

    private fun setupDefaultMocks(
        credentialOffer: CredentialOffer = oneIdentifierCredentialOffer,
        credentialInfo: IssuerCredentialInfo = oneConfigCredentialInformation,
        verifiableCredentialParams: VerifiableCredentialParams = verifiableCredentialParamsHardwareBinding,
    ) {
        every {
            mockVcSdJwtCredential.getClaimsForPresentation()
        } returns parseToJsonElement(CREDENTIAL_CLAIMS_FOR_PRESENTATION).jsonObject
        every { mockVcSdJwtCredential.issuer } returns ISSUER_DID
        every { mockVcSdJwtCredential.vcSchemaId } returns VC_SCHEMA_ID
        coEvery { mockVcSdJwtCredential.keyBinding } returns keyBinding
        coEvery { mockVcSdJwtCredential.payload } returns VC_PAYLOAD
        coEvery { mockVcSdJwtCredential.format } returns VC_FORMAT
        coEvery { mockVcSdJwtCredential.validFromInstant } returns VC_VALID_FROM
        coEvery { mockVcSdJwtCredential.validUntilInstant } returns VC_VALID_UNTIL

        every { mockIssuerCredentialInfoJwt.payloadString } returns RAW_ISSUER_INFO_JWT

        coEvery {
            mockFetchRawAndParsedCredentialInfo(
                issuerEndpoint = CREDENTIAL_ISSUER,
                forceRefresh = true,
            )
        } returns Ok(
            RawAndParsedIssuerCredentialInfo(
                issuerCredentialInfo = credentialInfo,
                rawIssuerCredentialInfo = mockIssuerCredentialInfoJwt
            )
        )

        coEvery {
            mockGetSignedMetadataDid(mockIssuerCredentialInfoJwt)
        } returns Ok(ISSUER_DID)

        every { mockIdentityTrustStatement.kid } returns KEY_ID

        coEvery {
            mockProcessIdentityTrustStatement(identityTrustStatementJwt = mockIdentityJwt, actorDid = ISSUER_DID)
        } returns Ok(mockIdentityTrustStatement)

        coEvery { mockValidateIssuerCredentialInfo(credentialInfo) } returns true

        coEvery { mockEnvironmentSetupRepository.batchIssuanceEnabled } returns false

        coEvery {
            mockGetPayloadEncryption(requestEncryption, responseEncryption)
        } returns Ok(
            PayloadEncryption(
                requestEncryption = requestEncryption,
                responseEncryption = responseEncryption,
                responseEncryptionKeyPair = payloadEncryptionKeyPair,
            )
        )

        coEvery {
            mockGetVerifiableCredentialParams(
                credentialInfo,
                credentialConfig,
                credentialOffer
            )
        } returns Ok(verifiableCredentialParams)

        coEvery {
            mockGetCredentialConfig(
                credentials = credentialOffer.credentialConfigurationIds,
                credentialConfigurations = credentialInfo.credentialConfigurations
            )
        } returns Ok(credentialConfig)

        coEvery {
            mockHandleCredentialResult(
                any(), any(), any(), any(), any(), any()
            )
        } returns Ok(FetchCredentialResult.Credential(CREDENTIAL_ID))

        coEvery {
            mockHandleDeferredCredentialResult(
                any(), any(), any(), any()
            )
        } returns Ok(FetchCredentialResult.DeferredCredential(DEFERRED_CREDENTIAL_ID))

        coEvery {
            mockGenerateProofKeyPairs(1, proofTypeConfigHardwareBinding, ISSUER_DID)
        } returns Ok(listOf(validHardwareKeyPair))
        coEvery {
            mockGenerateProofKeyPairs(1, proofTypeConfigSoftwareBinding, ISSUER_DID)
        } returns Ok(listOf(validSoftwareKeyPair))

        coEvery {
            mockFetchCredentialByConfig(any(), any(), any(), any())
        } returns Ok(mockAnyVerifiedCredential)

        coEvery {
            mockCredentialOfferRepository.saveCredentialOffer(
                keyBindings = any(),
                payloads = any(),
                format = any(),
                selectedConfigurationId = any(),
                validFrom = any(),
                validUntil = any(),
                issuer = any(),
                issuerDisplays = any(),
                credentialDisplays = any(),
                clusters = any(),
                rawCredentialData = any(),
                issuerUrl = any(),
            )
        } returns Ok(CREDENTIAL_ID)

        coEvery {
            mockCredentialOfferRepository.saveDeferredCredentialOffer(
                transactionId = any(),
                accessToken = any(),
                tokenType = any(),
                dpopKeyBinding = any(),
                endpoint = any(),
                pollInterval = any(),
                keyBindings = any(),
                format = any(),
                issuerDisplays = any(),
                credentialDisplays = any(),
                rawCredentialData = any(),
                selectedConfigurationId = any(),
                issuerUrl = any(),
                refreshToken = any(),
            )
        } returns Ok(DEFERRED_CREDENTIAL_ID)

        coEvery { mockGenerateDPoPKeyPair(any(), ISSUER_DID) } returns Ok(null)

        coEvery { mockGetActorEnvironment(any()) } returns ActorEnvironment.PRODUCTION

        coEvery { mockAnyVerifiedCredential.vcSdJwtCredential } returns mockVcSdJwtCredential
    }

    private companion object {
        const val CREDENTIAL_ID = 111L
        const val DEFERRED_CREDENTIAL_ID = 222L
        val CREDENTIAL_CLAIMS_FOR_PRESENTATION = """
            {
                "key":"value"
            }
        """.trimIndent()
        const val ISSUER_DID = "issuer did"
        const val KEY_ID = "keyId"
        const val VC_SCHEMA_ID = "vcSchemaId"
        const val VC_PAYLOAD = "payload"
        val VC_FORMAT = CredentialFormat.VC_SD_JWT
        val VC_VALID_FROM: Instant = Instant.ofEpochSecond(0)
        val VC_VALID_UNTIL: Instant = Instant.ofEpochSecond(100)

        const val RAW_ISSUER_INFO_JWT =
            "eyJhbGciOiJFUzI1NiIsImtpZCI6ImtleUlkIn0.eyJzb21lQ2xhaW0iOiJzb21lVmFsdWUifQ.squXfPeA3JtaVfnhQ35ThpCW-voZCByc6mvxQmH3zY4REanpybWAAjQ_EVlxDkOgRIv0Akuuu81uoaJof3BWoA"
        private val keyBinding = KeyBinding(
            identifier = "keyId",
            algorithm = SigningAlgorithm.ES512,
            bindingType = KeyBindingType.SOFTWARE,
        )

        val deferredCredential = DeferredCredential(
            format = CredentialFormat.VC_SD_JWT,
            keyBindings = listOf(keyBinding),
            transactionId = "transactionId",
            accessToken = "accessToken",
            tokenType = TokenType.BEARER,
            endpoint = URL("https://example"),
            pollInterval = 1,
            refreshToken = "refreshToken",
            dpopKeyBinding = null,
        )

        val mockPayloadEncryptionJwsKeyPair = mockk<JWSKeyPair>()
        val payloadEncryptionKeyPair = PayloadEncryptionKeyPair(
            keyPair = mockPayloadEncryptionJwsKeyPair,
            alg = "alg",
            enc = "enc",
            zip = null,
        )
    }
}

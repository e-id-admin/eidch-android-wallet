package ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.SignatureAlgorithm
import ch.admin.foitt.openid4vc.domain.model.anycredential.Validity
import ch.admin.foitt.openid4vc.domain.model.jwk.Jwks
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.AuthorizationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.ClientMetaData
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.RequestObject
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.RequestObjectVerificationOutcome
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtError
import ch.admin.foitt.openid4vc.domain.usecase.VerifyRequestObjectSignature
import ch.admin.foitt.wallet.platform.actorEnvironment.domain.model.ActorEnvironment
import ch.admin.foitt.wallet.platform.actorEnvironment.domain.usecase.GetActorEnvironment
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CredentialPresentationError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.VerificationProcessType
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.ValidatePresentationRequest
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.ValidateVerificationQueryPublicStatement
import ch.admin.foitt.wallet.platform.credentialPresentation.mock.MockPresentationRequest
import ch.admin.foitt.wallet.platform.credentialPresentation.mock.MockPresentationRequest.CLIENT_ID
import ch.admin.foitt.wallet.platform.credentialPresentation.mock.MockPresentationRequest.CLIENT_ID_WITH_PREFIX
import ch.admin.foitt.wallet.platform.credentialPresentation.mock.MockPresentationRequest.VERIFIER_ATTESTATION_CLIENT_ID
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IdentityV2TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustRegistryError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VerificationQueryPublicStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.ProcessIdentityTrustStatement
import ch.admin.foitt.wallet.util.SafeJsonTestInstance
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.mockk
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import uniffi.heidi_dcql_rust.DcqlQuery
import java.time.Instant

class ValidatePresentationRequestImplTest {

    private val testSafeJson = SafeJsonTestInstance.safeJson

    @MockK
    private lateinit var mockRequestObject: RequestObject

    @MockK
    private lateinit var mockEnvironmentSetupRepository: EnvironmentSetupRepository

    @MockK
    private lateinit var mockVerifyRequestObjectSignature: VerifyRequestObjectSignature

    @MockK
    private lateinit var mockProcessIdentityTrustStatement: ProcessIdentityTrustStatement

    @MockK
    private lateinit var mockGetActorEnvironment: GetActorEnvironment

    @MockK
    private lateinit var mockValidateVerificationQueryPublicStatement: ValidateVerificationQueryPublicStatement

    @MockK
    private lateinit var mockIdentityTrustStatement: IdentityV2TrustStatement

    @SpyK
    private var mockPresentationJwt: Jwt = Jwt(MockPresentationRequest.VALID_JWT)

    @SpyK
    private var mockTrustedDids: List<String> = mockk()

    private lateinit var useCase: ValidatePresentationRequest

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = ValidatePresentationRequestImpl(
            safeJson = testSafeJson,
            environmentSetupRepository = mockEnvironmentSetupRepository,
            processIdentityTrustStatement = mockProcessIdentityTrustStatement,
            verifyRequestObjectSignature = mockVerifyRequestObjectSignature,
            getActorEnvironment = mockGetActorEnvironment,
            validateVqPs = mockValidateVerificationQueryPublicStatement,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `A valid jwt presentation request returns presentation request`() = runTest {
        val result = useCase(VerificationProcessType.NETWORK, mockRequestObject).assertOk()

        assertEquals(MockPresentationRequest.authorizationRequest, result.authorizationRequest)
        assertEquals(mockPresentationJwt.rawJwt, result.rawPresentationRequest)
        assertEquals(VerificationProcessType.NETWORK, result.verificationProcessType)
        assertNull(result.verifierAttestationTrusted)
        assertEquals(verifiedDcql, result.dcqlQuery)
        assertEquals(true, result.hasVerifiedQuery)

        coVerify(exactly = 1) {
            mockGetActorEnvironment(any())
            mockProcessIdentityTrustStatement(any(), any())
            mockValidateVerificationQueryPublicStatement(any(), any())
        }
    }

    @Test
    fun `A presentation request without vqPS returns presentation request`() = runTest {
        every { mockPresentationJwt.payloadJson } returns MockPresentationRequest.authorizationRequestWithoutVqPS.toJsonObject()

        val result = useCase(VerificationProcessType.NETWORK, mockRequestObject).assertOk()

        val expectedRequest = MockPresentationRequest.authorizationRequestWithoutVqPS
        assertEquals(expectedRequest, result.authorizationRequest)
        assertEquals(mockPresentationJwt.rawJwt, result.rawPresentationRequest)
        assertEquals(VerificationProcessType.NETWORK, result.verificationProcessType)
        assertNull(result.verifierAttestationTrusted)
        assertEquals(false, result.hasVerifiedQuery)
        assertEquals(expectedRequest.dcqlQuery, result.dcqlQuery)

        coVerify(exactly = 0) {
            mockValidateVerificationQueryPublicStatement(any(), any())
        }
    }

    @Test
    fun `Request object jwt with static discovery aud claim returns Ok`() = runTest {
        val payloadJson = MockPresentationRequest.authorizationRequest.toJsonObject().toMutableMap().apply {
            put("aud", JsonPrimitive("https://self-issued.me/v2"))
        }.let { JsonObject(it) }
        every { mockPresentationJwt.payloadJson } returns payloadJson

        useCase(VerificationProcessType.NETWORK, mockRequestObject).assertOk()
    }

    @Test
    fun `A valid jwt presentation request with client ID prefix on request object returns Ok`() = runTest {
        every { mockRequestObject.clientId } returns CLIENT_ID_WITH_PREFIX

        useCase(VerificationProcessType.NETWORK, mockRequestObject).assertOk()
    }

    @Test
    fun `A valid jwt presentation request with client ID prefix on authorization request returns Ok`() = runTest {
        useCase(VerificationProcessType.NETWORK, mockRequestObject).assertOk()
    }

    @Test
    fun `A valid proximity jwt presentation request returns Ok`() = runTest {
        mockProximity()

        val result = useCase(VerificationProcessType.PROXIMITY, mockRequestObject).assertOk()

        val expectedRequest = MockPresentationRequest.authorizationRequestDcApi
        assertEquals(expectedRequest, result.authorizationRequest)
        assertEquals(mockPresentationJwt.rawJwt, result.rawPresentationRequest)
        assertEquals(VerificationProcessType.PROXIMITY, result.verificationProcessType)
        assertEquals(true, result.verifierAttestationTrusted)
        assertEquals(expectedRequest.dcqlQuery, result.dcqlQuery)
        assertEquals(true, result.hasVerifiedQuery)

        coVerify(exactly = 0) {
            mockGetActorEnvironment(any())
            mockProcessIdentityTrustStatement(any(), any())
            mockValidateVerificationQueryPublicStatement(any(), any())
        }
    }

    @Test
    fun `Proximity request object jwt missing response_uri claim returns Ok`() = runTest {
        mockProximity(responseUri = null)

        useCase(VerificationProcessType.PROXIMITY, mockRequestObject).assertOk()
    }

    @Test
    fun `Proximity request object jwt missing kid header returns Ok`(): Unit = runTest {
        mockProximity(keyId = null)

        useCase(VerificationProcessType.PROXIMITY, mockRequestObject).assertOk()
    }

    @Test
    fun `ATTESTATION_UNTRUSTED verification outcome over proximity returns success with verifierAttestationTrusted = false`() = runTest {
        mockProximity(requestVerificationOutcome = RequestObjectVerificationOutcome.ATTESTATION_UNTRUSTED)

        val result = useCase(VerificationProcessType.PROXIMITY, mockRequestObject).assertOk()

        assertEquals(false, result.verifierAttestationTrusted)
    }

    @Test
    fun `DCQL Authorization request using holder binding and containing state returns presentation request`(): Unit = runTest {
        every {
            mockPresentationJwt.payloadJson
        } returns MockPresentationRequest.authorizationRequestWithState.toJsonObject()

        useCase(VerificationProcessType.NETWORK, mockRequestObject).assertOk()
    }

    @Test
    fun `DCQL Authorization request not using holder binding and containing state returns presentation request`(): Unit = runTest {
        every {
            mockPresentationJwt.payloadJson
        } returns MockPresentationRequest.authorizationRequestWithStateAndNoHolderBinding.toJsonObject()

        useCase(VerificationProcessType.NETWORK, mockRequestObject).assertOk()
    }

    @Test
    fun `Verifier with BETA actor environment returns presentation request`() = runTest {
        coEvery { mockGetActorEnvironment(any()) } returns ActorEnvironment.BETA

        useCase(VerificationProcessType.NETWORK, mockRequestObject).assertOk()
    }

    @Test
    fun `Errors in identity trust return unverified issuer error`(): Unit = runTest {
        val exception = IllegalStateException("trust error")
        coEvery {
            mockProcessIdentityTrustStatement(any(), any())
        } returns Err(TrustRegistryError.Unexpected(exception))

        useCase(VerificationProcessType.NETWORK, mockRequestObject).assertErrorType(CredentialPresentationError.UnverifiedVerifier::class)
    }

    @Test
    fun `Without idTS vqPS is still evaluated`(): Unit = runTest {
        every {
            mockPresentationJwt.payloadJson
        } returns MockPresentationRequest.authorizationRequestWithoutIdTS.toJsonObject()

        useCase(VerificationProcessType.NETWORK, mockRequestObject).assertOk()

        coVerify(exactly = 1) { mockValidateVerificationQueryPublicStatement(any(), any()) }
    }

    @Test
    fun `Request object jwt with no header type returns an error`(): Unit = runTest {
        every { mockPresentationJwt.type } returns null

        useCase(VerificationProcessType.NETWORK, mockRequestObject).assertErrorType(CredentialPresentationError.InvalidRequest::class)
    }

    @Test
    fun `Request object jwt with other header type returns an error`(): Unit = runTest {
        every { mockPresentationJwt.type } returns "otherType"

        useCase(VerificationProcessType.NETWORK, mockRequestObject).assertErrorType(CredentialPresentationError.InvalidRequest::class)
    }

    @TestFactory
    fun `Request object jwt missing parameter returns unexpected error`(): List<DynamicTest> {
        val input = listOf("client_id", "response_uri")
        return input.map {
            DynamicTest.dynamicTest("Request object jwt missing $it returns an error") {
                runTest {
                    val payloadJson = MockPresentationRequest.authorizationRequest.toJsonObject().toMutableMap().apply {
                        remove(it)
                    }.let { json -> JsonObject(json) }
                    every { mockPresentationJwt.payloadJson } returns payloadJson

                    useCase(
                        VerificationProcessType.NETWORK,
                        mockRequestObject
                    ).assertErrorType(CredentialPresentationError.Unexpected::class)
                }
            }
        }
    }

    @Test
    fun `Request object clientId not matching jwt client_id returns an error`() = runTest {
        coEvery { mockRequestObject.clientId } returns "other client id"

        useCase(VerificationProcessType.NETWORK, mockRequestObject).assertErrorType(CredentialPresentationError.InvalidRequest::class)
    }

    @Test
    fun `Request object jwt with an invalid jwt alg header returns an invalid presentation error`() = runTest {
        every { mockPresentationJwt.algorithm } returns INVALID_JWT_ALGORITHM

        useCase(VerificationProcessType.NETWORK, mockRequestObject).assertErrorType(CredentialPresentationError.InvalidRequest::class)
    }

    @Test
    fun `Request object jwt missing kid header over network returns an invalid presentation error`(): Unit = runTest {
        every { mockPresentationJwt.keyId } returns null

        useCase(VerificationProcessType.NETWORK, mockRequestObject).assertErrorType(CredentialPresentationError.InvalidRequest::class)
    }

    @Test
    fun `Request object jwt that is not yet valid returns an invalid presentation error`() = runTest {
        every { mockPresentationJwt.jwtValidity } returns Validity.NotYetValid(Instant.now())

        useCase(VerificationProcessType.NETWORK, mockRequestObject).assertErrorType(CredentialPresentationError.InvalidRequest::class)
    }

    @Test
    fun `Request object jwt that is expired returns an invalid presentation error`() = runTest {
        every { mockPresentationJwt.jwtValidity } returns Validity.Expired(Instant.now())

        useCase(VerificationProcessType.NETWORK, mockRequestObject).assertErrorType(CredentialPresentationError.InvalidRequest::class)
    }

    @Test
    fun `Request object jwt with other aud claim returns error`() = runTest {
        val payloadJson = MockPresentationRequest.authorizationRequest.toJsonObject().toMutableMap().apply {
            put("aud", JsonPrimitive("otherAudience"))
        }.let { JsonObject(it) }
        every { mockPresentationJwt.payloadJson } returns payloadJson

        useCase(VerificationProcessType.NETWORK, mockRequestObject).assertErrorType(CredentialPresentationError.InvalidRequest::class)
    }

    @Test
    fun `ValidatePresentationRequest maps errors from verifying request object signature`(): Unit = runTest {
        coEvery {
            mockVerifyRequestObjectSignature(any(), any())
        } returns Err(VcSdJwtError.InvalidJwt)

        useCase(VerificationProcessType.NETWORK, mockRequestObject).assertErrorType(CredentialPresentationError.InvalidRequest::class)
    }

    @Test
    fun `ATTESTATION_UNTRUSTED verification outcome over network returns an UnknownVerifier error`() = runTest {
        coEvery {
            mockVerifyRequestObjectSignature(any(), any())
        } returns Ok(RequestObjectVerificationOutcome.ATTESTATION_UNTRUSTED)

        useCase(VerificationProcessType.NETWORK, mockRequestObject).assertErrorType(CredentialPresentationError.UnknownVerifier::class)
    }

    @Test
    fun `verifier_attestation client_id over network returns an InvalidRequest error`() = runTest {
        mockProximity()

        useCase(VerificationProcessType.NETWORK, mockRequestObject)
            .assertErrorType(CredentialPresentationError.InvalidRequest::class)
    }

    @Test
    fun `Request object jwt containing an authorization request with the transaction_data field returns an error`() = runTest {
        every { mockPresentationJwt.payloadJson } returns
            MockPresentationRequest.authorizationRequest.copy(transactionData = "data").toJsonObject()

        useCase(VerificationProcessType.NETWORK, mockRequestObject)
            .assertErrorType(CredentialPresentationError.InvalidTransactionData::class)
    }

    @Test
    fun `Request object jwt containing an invalid authorization request returns an error`() = runTest {
        every { mockPresentationJwt.payloadJson } returns JsonObject(mapOf("invalid" to JsonPrimitive("data")))

        useCase(VerificationProcessType.NETWORK, mockRequestObject).assertErrorType(CredentialPresentationError.Unexpected::class)
    }

    @Test
    fun `Authorization request with an invalid response_type (something else than 'vp_token') returns an invalid presentation error`() =
        runTest {
            every { mockPresentationJwt.payloadJson } returns
                MockPresentationRequest.authorizationRequest.copy(responseType = INVALID_RESPONSE_TYPE).toJsonObject()

            useCase(VerificationProcessType.NETWORK, mockRequestObject).assertErrorType(CredentialPresentationError.InvalidRequest::class)
        }

    @Test
    fun `Authorization request with an invalid response_mode returns an invalid presentation error`() =
        runTest {
            every { mockPresentationJwt.payloadJson } returns
                MockPresentationRequest.authorizationRequest.copy(responseMode = INVALID_RESPONSE_MODE).toJsonObject()

            useCase(VerificationProcessType.NETWORK, mockRequestObject).assertErrorType(CredentialPresentationError.InvalidRequest::class)
        }

    @Test
    fun `Authorization request without client metadata returns an invalid presentation error`() =
        runTest {
            every { mockPresentationJwt.payloadJson } returns
                MockPresentationRequest.authorizationRequest.copy(clientMetaData = null).toJsonObject()

            useCase(VerificationProcessType.NETWORK, mockRequestObject).assertErrorType(CredentialPresentationError.InvalidRequest::class)
        }

    @Test
    fun `Authorization request without jwks on client metadata returns an invalid presentation error`() =
        runTest {
            val clientMetadata =
                ClientMetaData(clientNameList = emptyList(), logoUriList = emptyList(), jwks = Jwks(emptyList()))
            every { mockPresentationJwt.payloadJson } returns
                MockPresentationRequest.authorizationRequest.copy(clientMetaData = clientMetadata).toJsonObject()

            useCase(VerificationProcessType.NETWORK, mockRequestObject).assertErrorType(CredentialPresentationError.InvalidRequest::class)
        }

    @Test
    fun `Authorization request with empty DCQL query claims returns an invalid presentation error`(): Unit = runTest {
        every {
            mockPresentationJwt.payloadJson
        } returns MockPresentationRequest.invalidPresentationRequestClaims().toJsonObject()

        useCase(VerificationProcessType.NETWORK, mockRequestObject).assertErrorType(CredentialPresentationError.InvalidRequest::class)
    }

    @Test
    fun `Authorization request with missing DCQL query returns an invalid presentation error`(): Unit =
        runTest {
            every {
                mockPresentationJwt.payloadJson
            } returns MockPresentationRequest.invalidPresentationRequestNoDCQL().toJsonObject()

            useCase(VerificationProcessType.NETWORK, mockRequestObject).assertErrorType(CredentialPresentationError.InvalidRequest::class)
        }

    @Test
    fun `DCQL Authorization request not using holder binding and missing state returns an invalid presentation error`(): Unit = runTest {
        every {
            mockPresentationJwt.payloadJson
        } returns MockPresentationRequest.invalidPresentationRequestState().toJsonObject()

        useCase(VerificationProcessType.NETWORK, mockRequestObject).assertErrorType(CredentialPresentationError.InvalidRequest::class)
    }

    @Test
    fun `Verifier with EXTERNAL actor environment returns UnknownRegistry error`() = runTest {
        coEvery { mockGetActorEnvironment(any()) } returns ActorEnvironment.EXTERNAL

        useCase(VerificationProcessType.NETWORK, mockRequestObject).assertErrorType(CredentialPresentationError.UnknownRegistry::class)
    }

    @Test
    fun `Proximity request without DCQL query returns an invalid presentation error`() = runTest {
        val authRequest = MockPresentationRequest.authorizationRequestDcApi.copy(dcqlQuery = null)
        every { mockPresentationJwt.payloadJson } returns authRequest.toJsonObject()

        useCase(VerificationProcessType.PROXIMITY, mockRequestObject).assertErrorType(CredentialPresentationError.InvalidRequest::class)
    }

    @Test
    fun `Authorization request with both JAR DCQL query and scope returns an error`() = runTest {
        every { mockPresentationJwt.payloadJson } returns
            MockPresentationRequest.authorizationRequestWithoutVqPS.copy(scope = "openid").toJsonObject()

        useCase(VerificationProcessType.NETWORK, mockRequestObject).assertErrorType(CredentialPresentationError.InvalidRequest::class)
    }

    @Test
    fun `Authorization request with mismatching scope returns an error`() = runTest {
        every { mockPresentationJwt.payloadJson } returns
            MockPresentationRequest.authorizationRequest.copy(scope = "other").toJsonObject()

        useCase(VerificationProcessType.NETWORK, mockRequestObject).assertErrorType(CredentialPresentationError.InvalidRequest::class)
    }

    private fun AuthorizationRequest.toJsonObject(): JsonObject =
        testSafeJson.json.encodeToJsonElement(value = this).jsonObject

    private fun setupDefaultMocks() {
        coEvery { mockRequestObject.jwt } returns mockPresentationJwt
        coEvery { mockRequestObject.clientId } returns CLIENT_ID
        coEvery {
            mockProcessIdentityTrustStatement(
                Jwt(MockPresentationRequest.VALID_IDTS), CLIENT_ID
            )
        } returns Ok(mockIdentityTrustStatement)
        coEvery { mockProcessIdentityTrustStatement(null, CLIENT_ID) } returns Ok(null)

        every { mockPresentationJwt.payloadJson } returns MockPresentationRequest.authorizationRequest.toJsonObject()
        every { mockPresentationJwt.algorithm } returns SignatureAlgorithm.ES256.stdName
        every { mockPresentationJwt.keyId } returns KEY_ID
        every { mockPresentationJwt.jwtValidity } returns Validity.Valid
        every { mockPresentationJwt.type } returns "oauth-authz-req+jwt"

        coEvery { mockEnvironmentSetupRepository.attestationsServiceTrustedDids } returns mockTrustedDids

        coEvery {
            mockVerifyRequestObjectSignature(mockRequestObject, mockTrustedDids)
        } returns Ok(RequestObjectVerificationOutcome.DID_PATH)

        coEvery { mockGetActorEnvironment(CLIENT_ID) } returns ActorEnvironment.PRODUCTION

        val vqPS = mockk<VerificationQueryPublicStatement> {
            every { request.scope } returns SCOPE
            every { request.query } returns verifiedDcql
        }
        coEvery {
            mockValidateVerificationQueryPublicStatement(Jwt(MockPresentationRequest.VALID_VQPS), CLIENT_ID)
        } returns Ok(vqPS)
    }

    private fun mockProximity(
        responseUri: String? = "response_uri",
        keyId: String? = KEY_ID,
        requestVerificationOutcome: RequestObjectVerificationOutcome = RequestObjectVerificationOutcome.ATTESTATION_TRUSTED,
    ) {
        val request = MockPresentationRequest.authorizationRequestDcApi.copy(responseUri = responseUri)
        val presentationJson = request.toJsonObject()
        every { mockPresentationJwt.payloadJson } returns presentationJson
        every { mockPresentationJwt.keyId } returns keyId
        coEvery {
            mockVerifyRequestObjectSignature(mockRequestObject, mockTrustedDids)
        } returns Ok(requestVerificationOutcome)
        coEvery { mockRequestObject.clientId } returns null
        coEvery { mockProcessIdentityTrustStatement(null, VERIFIER_ATTESTATION_CLIENT_ID) } returns Ok(null)
    }

    private companion object {
        const val INVALID_RESPONSE_TYPE = "invalid response_type"
        const val INVALID_RESPONSE_MODE = "invalid response_mode"
        const val INVALID_JWT_ALGORITHM = "HS256"
        const val KEY_ID = "keyId"
        const val SCOPE = "scope"
        val verifiedDcql = mockk<DcqlQuery> {
            every { credentials } returns MockPresentationRequest.authorizationRequestWithoutVqPS.dcqlQuery?.credentials
        }
    }
}

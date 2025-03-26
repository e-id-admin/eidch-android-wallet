package ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequestContainer
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtError
import ch.admin.foitt.openid4vc.domain.usecase.VerifyJwtSignature
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CredentialPresentationError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.ValidatePresentationRequest
import ch.admin.foitt.wallet.platform.credentialPresentation.mock.MockPresentationRequest
import ch.admin.foitt.wallet.util.SafeJsonTestInstance
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.SpyK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory

class ValidatePresentationRequestImplTest {

    private val testSafeJson = SafeJsonTestInstance.safeJson

    @MockK
    private lateinit var mockJwtPresentationContainer: PresentationRequestContainer.Jwt

    @MockK
    private lateinit var mockJsonPresentationContainer: PresentationRequestContainer.Json

    @MockK
    private lateinit var mockVerifyJwtSignature: VerifyJwtSignature

    @SpyK
    private var mockPresentationJwt: Jwt = Jwt(MockPresentationRequest.validJwt)

    private var mockPresentationJson: JsonObject = MockPresentationRequest.presentationRequest.toJsonObject()

    private lateinit var useCase: ValidatePresentationRequest

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = ValidatePresentationRequestImpl(
            testSafeJson,
            mockVerifyJwtSignature
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `A valid json Presentation request returns Ok`() = runTest {
        useCase(mockJsonPresentationContainer).assertOk()
    }

    @TestFactory
    fun `Invalid constrain paths should throw error`(): List<DynamicTest> {
        val invalidConstrainPaths = listOf(
            listOf("$..book[?(@.price <= $['expensive'])]"),
            listOf("$..book[ ?(@.price <= $['expensive'])]"),
            listOf("$..book[\t?(@.isbn)]"),
            listOf("$..book[        ?(@.isbn)]"),
            listOf("$..book[?(@.author =~ /.*REES/i)]"),
            listOf("valid.path", "$..book[?(@.isbn)]"),
            listOf("$..book[?(@.isbn)]", "valid.path"),
            listOf("valid.path", "$..book[?(@.isbn)]", "valid.path"),
        )
        return invalidConstrainPaths.map { paths ->
            DynamicTest.dynamicTest("$paths contains an invalid contrains path") {
                runTest {
                    coEvery {
                        mockJsonPresentationContainer.json
                    } returns MockPresentationRequest.invalidPresentationRequest(paths).toJsonObject()
                    useCase(mockJsonPresentationContainer).assertErrorType(CredentialPresentationError.InvalidPresentation::class)
                }
            }
        }
    }

    @Test
    fun `Json Presentation request with an invalid response_type (something else than 'vp_token') returns invalid presentation error`() =
        runTest {
            val presentationJson = MockPresentationRequest.presentationRequest
                .copy(responseType = INVALID_RESPONSE_TYPE)
                .toJsonObject()

            coEvery { mockJsonPresentationContainer.json } returns presentationJson

            useCase(mockJsonPresentationContainer).assertErrorType(CredentialPresentationError.InvalidPresentation::class)
        }

    @Test
    fun `Json Presentation request with an invalid response_mode (something else than 'direct_post') returns an invalid presentation error`() =
        runTest {
            val presentationJson = MockPresentationRequest.presentationRequest
                .copy(responseMode = INVALID_RESPONSE_MODE)
                .toJsonObject()
            coEvery { mockJsonPresentationContainer.json } returns presentationJson

            useCase(mockJsonPresentationContainer).assertErrorType(CredentialPresentationError.InvalidPresentation::class)
        }

    @Test
    fun `Json Presentation request missing client_id_scheme returns invalid presentation error`(): Unit =
        runTest {
            val presentationJson = MockPresentationRequest.presentationRequest
                .copy(clientIdScheme = null)
                .toJsonObject()
            coEvery { mockJsonPresentationContainer.json } returns presentationJson

            useCase(mockJsonPresentationContainer).assertErrorType(CredentialPresentationError.InvalidPresentation::class)
        }

    @Test
    fun `Json Presentation request with an invalid client_id_scheme (something else than 'did') returns invalid presentation error`() =
        runTest {
            val presentationJson = MockPresentationRequest.presentationRequest
                .copy(clientIdScheme = INVALID_CLIENT_ID_SCHEME)
                .toJsonObject()
            coEvery { mockJsonPresentationContainer.json } returns presentationJson

            useCase(mockJsonPresentationContainer).assertErrorType(CredentialPresentationError.InvalidPresentation::class)
        }

    @Test
    fun `Json Presentation request with an invalid client_id (something that is no did) returns invalid presentation error`() =
        runTest {
            val presentationJson = MockPresentationRequest.presentationRequest
                .copy(clientId = INVALID_CLIENT_ID)
                .toJsonObject()
            coEvery { mockJsonPresentationContainer.json } returns presentationJson

            useCase(mockJsonPresentationContainer).assertErrorType(CredentialPresentationError.InvalidPresentation::class)
        }

    @Test
    fun `A valid jwt Presentation request returns Ok`() = runTest {
        useCase(mockJwtPresentationContainer).assertOk()
    }

    @Test
    fun `Jwt Presentation request with an invalid response_type (something else than 'vp_token') returns invalid presentation error`() =
        runTest {
            val presentationJson = MockPresentationRequest.presentationRequest
                .copy(responseType = INVALID_RESPONSE_TYPE)
                .toJsonObject()

            coEvery { mockPresentationJwt.payloadJson } returns presentationJson

            useCase(mockJwtPresentationContainer).assertErrorType(CredentialPresentationError.InvalidPresentation::class)
        }

    @Test
    fun `Jwt Presentation request with an invalid response_mode (something else than 'direct_post') returns an invalid presentation error`() =
        runTest {
            val presentationJson = MockPresentationRequest.presentationRequest
                .copy(responseMode = INVALID_RESPONSE_MODE)
                .toJsonObject()
            coEvery { mockPresentationJwt.payloadJson } returns presentationJson

            useCase(mockJwtPresentationContainer).assertErrorType(CredentialPresentationError.InvalidPresentation::class)
        }

    @Test
    fun `Jwt Presentation request missing client_id_scheme returns invalid presentation error`() =
        runTest {
            val presentationJson = MockPresentationRequest.presentationRequest
                .copy(clientIdScheme = null)
                .toJsonObject()
            coEvery { mockPresentationJwt.payloadJson } returns presentationJson

            useCase(mockJwtPresentationContainer).assertErrorType(CredentialPresentationError.InvalidPresentation::class)
        }

    @Test
    fun `Jwt Presentation request with an invalid client_id_scheme (something else than 'did') returns invalid presentation error`() =
        runTest {
            val presentationJson = MockPresentationRequest.presentationRequest
                .copy(clientIdScheme = INVALID_CLIENT_ID_SCHEME)
                .toJsonObject()
            coEvery { mockPresentationJwt.payloadJson } returns presentationJson

            useCase(mockJwtPresentationContainer).assertErrorType(CredentialPresentationError.InvalidPresentation::class)
        }

    @Test
    fun `Jwt Presentation request with an invalid client_id (something that is no did) returns invalid presentation error`(): Unit =
        runTest {
            val presentationJson = MockPresentationRequest.presentationRequest
                .copy(clientId = INVALID_CLIENT_ID)
                .toJsonObject()
            coEvery { mockPresentationJwt.payloadJson } returns presentationJson

            useCase(mockJwtPresentationContainer).assertErrorType(CredentialPresentationError.InvalidPresentation::class)
        }

    @Test
    fun `Jwt Presentation request with an invalid jwt alg header return invalid presentation error `() = runTest {
        coEvery { mockPresentationJwt.algorithm } returns INVALID_JWT_ALGORITHM

        useCase(mockJwtPresentationContainer).assertErrorType(CredentialPresentationError.InvalidPresentation::class)
    }

    @Test
    fun `Jwt Presentation request with a missing jwt kid header return invalid presentation error `(): Unit = runTest {
        coEvery { mockPresentationJwt.keyId } returns null

        useCase(mockJwtPresentationContainer).assertErrorType(CredentialPresentationError.InvalidPresentation::class)
    }

    @Test
    fun `Jwt Presentation request map invalid jwt signature to invalid presentation error`(): Unit = runTest {
        coEvery {
            mockVerifyJwtSignature.invoke(did = any(), kid = any(), jwt = mockPresentationJwt)
        } returns Err(VcSdJwtError.InvalidJwt)

        useCase(mockJwtPresentationContainer).assertErrorType(CredentialPresentationError.InvalidPresentation::class)
    }

    @Test
    fun `Jwt Presentation request map jwt issuer validation failure to unknown verifier error`(): Unit = runTest {
        coEvery {
            mockVerifyJwtSignature.invoke(did = any(), kid = any(), jwt = mockPresentationJwt)
        } returns Err(VcSdJwtError.IssuerValidationFailed)

        useCase(mockJwtPresentationContainer).assertErrorType(CredentialPresentationError.UnknownVerifier::class)
    }

    @Test
    fun `Jwt Presentation request map network error during did resolution to network error`(): Unit = runTest {
        coEvery {
            mockVerifyJwtSignature.invoke(did = any(), kid = any(), jwt = mockPresentationJwt)
        } returns Err(VcSdJwtError.NetworkError)

        useCase(mockJwtPresentationContainer).assertErrorType(CredentialPresentationError.NetworkError::class)
    }

    @Test
    fun `Jwt Presentation request map invalid jwt signature with DidDocumentDeactivated to invalid presentation error`(): Unit = runTest {
        coEvery {
            mockVerifyJwtSignature.invoke(did = any(), kid = any(), jwt = mockPresentationJwt)
        } returns Err(VcSdJwtError.DidDocumentDeactivated)

        useCase(mockJwtPresentationContainer).assertErrorType(CredentialPresentationError.InvalidPresentation::class)
    }

    private fun setupDefaultMocks() {
        coEvery { mockVerifyJwtSignature(any(), any(), any()) } returns Ok(Unit)
        coEvery { mockJwtPresentationContainer.jwt } returns mockPresentationJwt
        coEvery { mockJsonPresentationContainer.json } returns mockPresentationJson
    }

    private fun PresentationRequest.toJsonObject(): JsonObject =
        testSafeJson.json.encodeToJsonElement(value = this).jsonObject

    private companion object {
        const val INVALID_RESPONSE_TYPE = "invalid response_type"
        const val INVALID_RESPONSE_MODE = "invalid response_mode"
        const val INVALID_CLIENT_ID = "invalid client_id"
        const val INVALID_CLIENT_ID_SCHEME = "invalid client_id_scheme"
        const val INVALID_JWT_ALGORITHM = "HS256"
    }
}

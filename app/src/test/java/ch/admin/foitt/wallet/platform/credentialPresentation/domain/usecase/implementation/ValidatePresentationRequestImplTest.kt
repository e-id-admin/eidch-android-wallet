package ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.Field
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.InputDescriptor
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.JsonPresentationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.JwtPresentationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationDefinition
import ch.admin.foitt.openid4vc.domain.usecase.VerifyJwtSignature
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.model.CredentialPresentationError
import ch.admin.foitt.wallet.platform.credentialPresentation.domain.usecase.ValidatePresentationRequest
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import ch.admin.foitt.wallet.util.create
import com.github.michaelbull.result.Ok
import com.nimbusds.jwt.SignedJWT
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ValidatePresentationRequestImplTest {

    @MockK
    private lateinit var mockJwtPresentationRequest: JwtPresentationRequest

    @MockK
    private lateinit var mockJsonPresentationRequest: JsonPresentationRequest

    @MockK
    private lateinit var mockVerifyJwtSignature: VerifyJwtSignature

    private lateinit var useCase: ValidatePresentationRequest

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = ValidatePresentationRequestImpl(
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
        useCase(mockJsonPresentationRequest).assertOk()
    }

    @Test
    fun `Json Presentation request with an invalid response_type (something else than 'vp_token') returns invalid presentation error`() =
        runTest {
            every { mockJsonPresentationRequest.responseType } returns INVALID_RESPONSE_TYPE

            useCase(mockJsonPresentationRequest).assertErrorType(CredentialPresentationError.InvalidPresentation::class)
        }

    @Test
    fun `Json Presentation request with an invalid response_mode (something else than 'direct_post') returns an invalid presentation error`() =
        runTest {
            every { mockJsonPresentationRequest.responseMode } returns INVALID_RESPONSE_MODE

            useCase(mockJsonPresentationRequest).assertErrorType(CredentialPresentationError.InvalidPresentation::class)
        }

    @Test
    fun `Json Presentation request with client_id, but missing client_id_scheme returns invalid presentation error`() =
        runTest {
            every { mockJsonPresentationRequest.clientIdScheme } returns null

            useCase(mockJsonPresentationRequest).assertErrorType(CredentialPresentationError.InvalidPresentation::class)
        }

    @Test
    fun `Json Presentation request with a DID as client_id, but not 'did' as client_id_scheme returns invalid presentation error`() =
        runTest {
            every { mockJsonPresentationRequest.clientIdScheme } returns INVALID_CLIENT_ID_SCHEME

            useCase(mockJsonPresentationRequest).assertErrorType(CredentialPresentationError.InvalidPresentation::class)
        }

    @Test
    fun `Json Presentation request with an invalid client_id_scheme (something else than 'did') returns invalid presentation error`() =
        runTest {
            every { mockJsonPresentationRequest.clientIdScheme } returns INVALID_CLIENT_ID_SCHEME

            useCase(mockJsonPresentationRequest).assertErrorType(CredentialPresentationError.InvalidPresentation::class)
        }

    @Test
    fun `Json Presentation request with a valid client_id_scheme, but invalid client_id (something that is no did) returns invalid presentation error`() =
        runTest {
            every { mockJsonPresentationRequest.clientId } returns INVALID_CLIENT_ID

            useCase(mockJsonPresentationRequest).assertErrorType(CredentialPresentationError.InvalidPresentation::class)
        }

    @Test
    fun `A valid jwt Presentation request returns Ok`() = runTest {
        useCase(mockJwtPresentationRequest).assertOk()
    }

    @Test
    fun `Jwt Presentation request with an invalid response_type (something else than 'vp_token') returns invalid presentation error`() =
        runTest {
            every { mockJwtPresentationRequest.responseType } returns INVALID_RESPONSE_TYPE

            useCase(mockJwtPresentationRequest).assertErrorType(CredentialPresentationError.InvalidPresentation::class)
        }

    @Test
    fun `Jwt Presentation request with an invalid response_mode (something else than 'direct_post') returns an invalid presentation error`() =
        runTest {
            every { mockJwtPresentationRequest.responseMode } returns INVALID_RESPONSE_MODE

            useCase(mockJwtPresentationRequest).assertErrorType(CredentialPresentationError.InvalidPresentation::class)
        }

    @Test
    fun `Jwt Presentation request with client_id, but missing client_id_scheme returns invalid presentation error`() =
        runTest {
            every { mockJwtPresentationRequest.clientIdScheme } returns null

            useCase(mockJwtPresentationRequest).assertErrorType(CredentialPresentationError.InvalidPresentation::class)
        }

    @Test
    fun `Jwt Presentation request with a DID as client_id, but not 'did' as client_id_scheme returns invalid presentation error`() =
        runTest {
            every { mockJwtPresentationRequest.clientIdScheme } returns INVALID_CLIENT_ID_SCHEME

            useCase(mockJwtPresentationRequest).assertErrorType(CredentialPresentationError.InvalidPresentation::class)
        }

    @Test
    fun `Jwt Presentation request with an invalid client_id_scheme (something else than 'did') returns invalid presentation error`() =
        runTest {
            every { mockJwtPresentationRequest.clientIdScheme } returns INVALID_CLIENT_ID_SCHEME

            useCase(mockJwtPresentationRequest).assertErrorType(CredentialPresentationError.InvalidPresentation::class)
        }

    @Test
    fun `Jwt Presentation request with a valid client_id_scheme, but invalid client_id (something that is no did) returns invalid presentation error`() =
        runTest {
            every { mockJwtPresentationRequest.clientId } returns INVALID_CLIENT_ID

            useCase(mockJwtPresentationRequest).assertErrorType(CredentialPresentationError.InvalidPresentation::class)
        }

    @Test
    fun `Jwt Presentation request with an invalid jwt alg header return invalid presentation error `() = runTest {
        every { mockJwtPresentationRequest.signedJWT } returns mockJwtInvalidAlgorithm

        useCase(mockJwtPresentationRequest).assertErrorType(CredentialPresentationError.InvalidPresentation::class)
    }

    @Test
    fun `Jwt Presentation request with an invalid jwt kid header return invalid presentation error `() = runTest {
        every { mockJwtPresentationRequest.signedJWT } returns mockJwtInvalidKeyId

        useCase(mockJwtPresentationRequest).assertErrorType(CredentialPresentationError.InvalidPresentation::class)
    }

    private fun setupDefaultMocks() {
        coEvery { mockVerifyJwtSignature(any(), any(), any()) } returns Ok(Unit)

        every { mockJsonPresentationRequest.responseUri } returns RESPONSE_URI
        every { mockJsonPresentationRequest.responseType } returns VALID_RESPONSE_TYPE
        every { mockJsonPresentationRequest.clientId } returns VALID_CLIENT_ID
        every { mockJsonPresentationRequest.clientIdScheme } returns VALID_CLIENT_ID_SCHEME
        every { mockJsonPresentationRequest.responseMode } returns VALID_RESPONSE_MODE
        every { mockJsonPresentationRequest.presentationDefinition } returns mockPresentationDefinition

        every { mockJwtPresentationRequest.responseUri } returns RESPONSE_URI
        every { mockJwtPresentationRequest.responseType } returns VALID_RESPONSE_TYPE
        every { mockJwtPresentationRequest.clientId } returns VALID_CLIENT_ID
        every { mockJwtPresentationRequest.clientIdScheme } returns VALID_CLIENT_ID_SCHEME
        every { mockJwtPresentationRequest.responseMode } returns VALID_RESPONSE_MODE
        every { mockJwtPresentationRequest.presentationDefinition } returns mockPresentationDefinition
        every { mockJwtPresentationRequest.signedJWT } returns mockValidJwt
    }

    private companion object {
        const val VALID_RESPONSE_TYPE = "vp_token"
        const val INVALID_RESPONSE_TYPE = "invalid response_type"
        const val VALID_RESPONSE_MODE = "direct_post"
        const val INVALID_RESPONSE_MODE = "invalid response_mode"
        const val VALID_CLIENT_ID = "did:method:identifier"
        const val INVALID_CLIENT_ID = "invalid client_id"
        const val VALID_CLIENT_ID_SCHEME = "did"
        const val INVALID_CLIENT_ID_SCHEME = "invalid client_id_scheme"
        const val RESPONSE_URI = ""

        val mockPresentationDefinition = PresentationDefinition(
            id = "id",
            name = "name",
            inputDescriptors = listOf(
                InputDescriptor.create(Field(path = listOf("path"))),
            ),
            purpose = "purpose",
        )

        /*
        header:
        {
          "kid": "did:tdw:identifier",
          "alg": "ES256"
        }
        payload:
        {
          "key": "value"
        }
         */
        val mockValidJwt: SignedJWT = SignedJWT.parse(
            "ewogICJraWQiOiJkaWQ6dGR3OmlkZW50aWZpZXIiLAogICJhbGciOiJFUzI1NiIKfQ.ewogICJrZXkiOiJ2YWx1ZSIKfQ.ZXdvZ0lDSnJhV1FpT2lKa2FXUTZkR1IzT21sa1pXNTBhV1pwWlhJaUxBb2dJQ0poYkdjaU9pSkZVekkxTmlJS2ZRLi5wTVZ5TjFNU0hkOFBaMXJUWS00ZXZpTDJYQWZzUmtqajB0dUhVdlRLa09zYlFwNks0LVVXWTc5SHc0N1NCZ1pleEo5NW5pVXlTZ3lxVy1WeFlIRmRSZw"
        )

        /*
        header:
        {
          "kid": "did:tdw:identifier",
          "alg": "HS256"
        }
        payload:
        {
          "key": "value"
        }
         */
        val mockJwtInvalidAlgorithm: SignedJWT = SignedJWT.parse(
            "ewogICJraWQiOiJkaWQ6dGR3OmlkZW50aWZpZXIiLAogICJhbGciOiJIUzI1NiIKfQ.ewogICJrZXkiOiJ2YWx1ZSIKfQ.ZXdvZ0lDSnJhV1FpT2lKa2FXUTZkR1IzT21sa1pXNTBhV1pwWlhJaUxBb2dJQ0poYkdjaU9pSklVekkxTmlJS2ZRLi5wVXA2Mk5ULUlvZXhXZnJ4VlVmcDNiTk9DcXFkYV9TbFUzWlFQdzdUdlpZ"
        )

        /*
        header:
        {
          "alg": "ES256"
        }
        payload:
        {
          "key": "value"
        }
         */
        val mockJwtInvalidKeyId: SignedJWT = SignedJWT.parse(
            "ewogICJhbGciOiJFUzI1NiIKfQ.ewogICJrZXkiOiJ2YWx1ZSIKfQ.ZXdvZ0lDSmhiR2NpT2lKRlV6STFOaUlLZlEuLlF6ckpuU3J5OWdHQXllY0xPOHdablZ1RWxWWnJJbXNtVHNIRjFBQ2V5eHk1QW5NVUpVU0x2R2FfNndrUVdFOUlYN09FdnJ3X05CMXI2RldDbjFPZEtR"
        )
    }
}

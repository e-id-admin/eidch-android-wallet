package ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.implementation

import ch.admin.foitt.openid4vc.domain.model.VerifiableCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOffer
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.VcSdJwtCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.sdjwt.SdJwt
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtError
import ch.admin.foitt.openid4vc.domain.usecase.FetchVerifiableCredential
import ch.admin.foitt.openid4vc.domain.usecase.VerifyJwtSignature
import ch.admin.foitt.openid4vc.util.assertErrorType
import ch.admin.foitt.openid4vc.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.EqMatcher
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.mockkConstructor
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonElement
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError as OpenIdCredentialOfferError

class FetchVcSdJwtCredentialImplTest {

    @MockK
    private lateinit var mockFetchVerifiableCredential: FetchVerifiableCredential

    @MockK
    private lateinit var mockCredentialConfig: VcSdJwtCredentialConfiguration

    @MockK
    private lateinit var mockCredentialOffer: CredentialOffer

    @MockK
    private lateinit var mockVerifyJwtSignature: VerifyJwtSignature

    @MockK
    private lateinit var mockJson: JsonElement

    private lateinit var useCase: FetchVcSdJwtCredentialImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = FetchVcSdJwtCredentialImpl(
            fetchVerifiableCredential = mockFetchVerifiableCredential,
            verifyJwtSignature = mockVerifyJwtSignature,
        )

        every { mockCredentialOffer.credentialIssuer } returns CREDENTIAL_ISSUER

        coEvery {
            mockFetchVerifiableCredential(mockCredentialConfig, mockCredentialOffer)
        } returns Ok(mockVerifiableCredential)

        mockkConstructor(SdJwt::class)
        every { constructedWith<SdJwt>(EqMatcher(PAYLOAD)).json } returns mockJson

        coEvery { mockVerifyJwtSignature(any(), any(), any()) } returns Ok(Unit)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Fetching jwt vc json credential which is valid returns a credential`(): Unit = runTest {
        every { constructedWith<SdJwt>(EqMatcher(PAYLOAD)).issuer } returns "issuer"
        every { constructedWith<SdJwt>(EqMatcher(PAYLOAD)).signedJWT.header.keyID } returns "keyId"
        val result = useCase(mockCredentialConfig, mockCredentialOffer)

        val credential = result.assertOk()
        assertEquals(SIGNING_KEY_ID, credential.signingKeyId)
        assertEquals(PAYLOAD, credential.payload)
        assertEquals(CredentialFormat.VC_SD_JWT, credential.format)
        assertEquals(mockJson, credential.json)
    }

    @Test
    fun `Fetching jwt vc json credential which is invalid returns an error`(): Unit = runTest {
        every { constructedWith<SdJwt>(EqMatcher(PAYLOAD)).issuer } returns "issuer"
        every { constructedWith<SdJwt>(EqMatcher(PAYLOAD)).signedJWT.header.keyID } returns "keyId"
        coEvery {
            mockVerifyJwtSignature(any(), any(), any())
        } returns Err(VcSdJwtError.InvalidJwt)

        val result = useCase(mockCredentialConfig, mockCredentialOffer)

        result.assertErrorType(CredentialOfferError.IntegrityCheckFailed::class)
    }

    @Test
    fun `Fetching jwt vc json credential maps errors from fetching verifiable credential`(): Unit = runTest {
        val exception = IllegalStateException()
        coEvery {
            mockFetchVerifiableCredential(any(), any())
        } returns Err(OpenIdCredentialOfferError.Unexpected(exception))

        val result = useCase(mockCredentialConfig, mockCredentialOffer)

        val error = result.assertErrorType(CredentialOfferError.Unexpected::class)
        assertEquals(exception, error.cause)
    }

    @Test
    fun `Fetching jwt vc json credential maps errors from verifying jwt`(): Unit = runTest {
        every { constructedWith<SdJwt>(EqMatcher(PAYLOAD)).issuer } returns "issuer"
        every { constructedWith<SdJwt>(EqMatcher(PAYLOAD)).signedJWT.header.keyID } returns "keyId"
        coEvery {
            mockVerifyJwtSignature(any(), any(), any())
        } returns Err(VcSdJwtError.InvalidJwt)

        val result = useCase(mockCredentialConfig, mockCredentialOffer)

        result.assertErrorType(CredentialOfferError.IntegrityCheckFailed::class)
    }

    private companion object {
        const val CREDENTIAL_ISSUER = "credentialIssuer"
        const val SIGNING_KEY_ID = "signingKeyId"
        const val PAYLOAD =
            "eyJhbGciOiJFUzI1NiIsInR5cCI6InZjK3NkLWp3dCJ9.eyJpc3MiOiJpc3N1ZXIiLCJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMn0.J-MdLh8yTTVTUqidxZWetTcgvvupOjjffMbuSWZzdfrv3S4PDbByxDi1L9amBv8E9QY9m74OxFYakvZxesHD6w"
        val mockVerifiableCredential = VerifiableCredential(
            signingKeyId = SIGNING_KEY_ID,
            credential = PAYLOAD,
            format = CredentialFormat.VC_SD_JWT,
            signingAlgorithm = SigningAlgorithm.ES512,
        )
    }
}

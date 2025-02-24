package ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.implementation

import ch.admin.foitt.openid4vc.domain.model.VerifiableCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOffer
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.CredentialOfferError
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.VcSdJwtCredentialConfiguration
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtError
import ch.admin.foitt.openid4vc.domain.usecase.FetchVerifiableCredential
import ch.admin.foitt.openid4vc.domain.usecase.VerifyJwtSignature
import ch.admin.foitt.openid4vc.domain.usecase.vcSdJwt.FetchVcSdJwtCredential
import ch.admin.foitt.openid4vc.util.SafeJsonTestInstance
import ch.admin.foitt.openid4vc.util.assertErrorType
import ch.admin.foitt.openid4vc.util.assertOk
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

    private lateinit var useCase: FetchVcSdJwtCredential

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

        coEvery { mockVerifyJwtSignature(any(), any(), any()) } returns Ok(Unit)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Fetching jwt vc json credential which is valid returns a credential`(): Unit = runTest {
        val result = useCase(mockCredentialConfig, mockCredentialOffer)

        val credential = result.assertOk()
        assertEquals(KEY_BINDING_IDENTIFIER, credential.keyBindingIdentifier)
        assertEquals(PAYLOAD, credential.payload)
        assertEquals(CredentialFormat.VC_SD_JWT, credential.format)
        assertEquals(SafeJsonTestInstance.json.parseToJsonElement(credentialJson), credential.json)
    }

    @Test
    fun `Fetching jwt vc json credential which is invalid returns an error`(): Unit = runTest {
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
        coEvery {
            mockVerifyJwtSignature(any(), any(), any())
        } returns Err(VcSdJwtError.InvalidJwt)

        val result = useCase(mockCredentialConfig, mockCredentialOffer)

        result.assertErrorType(CredentialOfferError.IntegrityCheckFailed::class)
    }

    private companion object {
        const val CREDENTIAL_ISSUER = "credentialIssuer"
        const val KEY_BINDING_IDENTIFIER = "signingKeyId"
        const val PAYLOAD =
            "ewogICJhbGciOiJFUzI1NiIsCiAgInR5cCI6InZjK3NkLWp3dCIsCiAgImtpZCI6ImtleUlkIgp9.ewogICJpc3MiOiJpc3N1ZXIiLAogICJ2Y3QiOiJ2Y3QiLAogICJzdWIiOiIxMjM0NTY3ODkwIiwKICAibmFtZSI6IkpvaG4gRG9lIiwKICAiaWF0IjoxNTE2MjM5MDIyCn0.ZXdvZ0lDSmhiR2NpT2lKRlV6STFOaUlzQ2lBZ0luUjVjQ0k2SW5aakszTmtMV3AzZENJc0NpQWdJbXRwWkNJNkltdGxlVWxrSWdwOS4uSm90ZUNENWFSQjNsVUpUMUtKM1VXRHZSbE5DeW9HR0xnbDVXVEpXa01ObUQzbDc5MGZ3MnlJeHdKTHgyZFllWVFRUHRSU1l6TUd1NGlMZjRJMWhmbFE"
        val mockVerifiableCredential = VerifiableCredential(
            keyBindingIdentifier = KEY_BINDING_IDENTIFIER,
            credential = PAYLOAD,
            format = CredentialFormat.VC_SD_JWT,
            keyBindingAlgorithm = SigningAlgorithm.ES512,
        )
        val credentialJson = """
            {
              "iss":"issuer",
              "vct":"vct",
              "sub":"1234567890",
              "name":"John Doe",
              "iat":1516239022
            }
        """.trimIndent()
    }
}

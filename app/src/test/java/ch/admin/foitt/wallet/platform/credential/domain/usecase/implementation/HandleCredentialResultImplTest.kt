package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.SigningAlgorithm
import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyVerifiedCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.RawAndParsedIssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBinding
import ch.admin.foitt.openid4vc.domain.model.keyBinding.KeyBindingType
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchCredentialResult
import ch.admin.foitt.wallet.platform.credential.domain.usecase.HandleCredentialResult
import ch.admin.foitt.wallet.platform.credential.domain.usecase.SaveVcSdJwtCredentials
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.credentialConfig
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.oneConfigCredentialInformation
import ch.admin.foitt.wallet.platform.credentialRefresh.domain.repository.CredentialRefreshDataRepository
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IdentityV2TrustStatement
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertSuccessType
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URL

class HandleCredentialResultImplTest {
    @MockK
    private lateinit var mockVcSdJwtCredential: VcSdJwtCredential

    @MockK
    private lateinit var mockSaveVcSdJwtCredentials: SaveVcSdJwtCredentials

    @MockK
    private lateinit var mockCredentialRefreshDataRepository: CredentialRefreshDataRepository

    @MockK
    private lateinit var mockTrustStatement: IdentityV2TrustStatement

    @MockK
    private lateinit var mockIssuerCredentialInfoJwt: Jwt

    private lateinit var useCase: HandleCredentialResult

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = HandleCredentialResultImpl(
            saveVcSdJwtCredentials = mockSaveVcSdJwtCredentials,
            credentialRefreshDataRepository = mockCredentialRefreshDataRepository,
        )
        setupDefaultMocks()
    }

    @Test
    fun `Saving a credential runs specific steps`() = runTest {
        val result = useCase.invoke(
            issuerUrl = ISSUER_URL,
            anyVerifiedCredential = createAnyVerifiedCredential(),
            identityTrustStatement = mockTrustStatement,
            rawAndParsedCredentialInfo = rawAndParsedIssuerCredentialInfo,
            credentialConfig = credentialConfig,
        )

        val credentialResult = result.assertSuccessType(FetchCredentialResult.Credential::class)
        assertEquals(CREDENTIAL_ID, credentialResult.credentialId)

        coVerify {
            mockSaveVcSdJwtCredentials(
                issuerUrl = ISSUER_URL,
                vcSdJwtCredentials = listOf(mockVcSdJwtCredential),
                identityTrustStatement = mockTrustStatement,
                rawAndParsedCredentialInfo = rawAndParsedIssuerCredentialInfo,
                credentialConfig = credentialConfig,
            )
        }
    }

    @Test
    fun `Saving credential maps errors from SaveVcSdJwtCredentials`() = runTest {
        coEvery {
            mockSaveVcSdJwtCredentials(
                issuerUrl = any(),
                vcSdJwtCredentials = any(),
                identityTrustStatement = any(),
                rawAndParsedCredentialInfo = any(),
                credentialConfig = any(),
            )
        } returns Err(CredentialError.InvalidCredentialOffer)

        useCase(
            issuerUrl = ISSUER_URL,
            anyVerifiedCredential = createAnyVerifiedCredential(),
            identityTrustStatement = mockTrustStatement,
            rawAndParsedCredentialInfo = rawAndParsedIssuerCredentialInfo,
            credentialConfig = credentialConfig,
        ).assertErrorType(CredentialError.InvalidCredentialOffer::class)
    }

    private fun setupDefaultMocks() {
        coEvery {
            mockSaveVcSdJwtCredentials(
                issuerUrl = any(),
                vcSdJwtCredentials = any(),
                identityTrustStatement = mockTrustStatement,
                rawAndParsedCredentialInfo = any(),
                credentialConfig = any(),
            )
        } returns Ok(CREDENTIAL_ID)

        coEvery {
            mockCredentialRefreshDataRepository.saveRefreshData(
                credentialId = any(),
                batchSize = any(),
                accessToken = any(),
                refreshToken = any(),
                dpopKeyBinding = any(),
            )
        } returns Ok(0L)
    }

    private fun createAnyVerifiedCredential() = AnyVerifiedCredential(
        accessToken = "accessToken",
        refreshToken = "refreshToken",
        dpopKeyBinding = KeyBinding(
            identifier = "identifier",
            algorithm = SigningAlgorithm.ES256,
            publicKey = byteArrayOf(),
            privateKey = byteArrayOf(),
            bindingType = KeyBindingType.SOFTWARE
        ),
        vcSdJwtCredential = mockVcSdJwtCredential,
    )
    private val rawAndParsedIssuerCredentialInfo by lazy {
        RawAndParsedIssuerCredentialInfo(
            issuerCredentialInfo = oneConfigCredentialInformation,
            rawIssuerCredentialInfo = mockIssuerCredentialInfoJwt
        )
    }

    companion object {
        private const val CREDENTIAL_ID = 1337L
        private val ISSUER_URL = URL("https://issuer.example")
    }
}

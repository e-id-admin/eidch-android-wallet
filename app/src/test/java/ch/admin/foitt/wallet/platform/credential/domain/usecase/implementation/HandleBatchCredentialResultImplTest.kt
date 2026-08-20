package ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.BatchSize
import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyVerifiedBatchCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.RawAndParsedIssuerCredentialInfo
import ch.admin.foitt.openid4vc.domain.model.jwt.Jwt
import ch.admin.foitt.openid4vc.domain.model.vcSdJwt.VcSdJwtCredential
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.model.FetchCredentialResult
import ch.admin.foitt.wallet.platform.credential.domain.usecase.HandleBatchCredentialResult
import ch.admin.foitt.wallet.platform.credential.domain.usecase.SaveVcSdJwtCredentials
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.credentialConfig
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.mock.MockFetchCredential.oneConfigCredentialInformation
import ch.admin.foitt.wallet.platform.credentialRefresh.domain.model.CredentialRefreshDataError
import ch.admin.foitt.wallet.platform.credentialRefresh.domain.repository.CredentialRefreshDataRepository
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.IdentityV2TrustStatement
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertSuccessType
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.URL

class HandleBatchCredentialResultImplTest {

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

    private lateinit var useCase: HandleBatchCredentialResult

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = HandleBatchCredentialResultImpl(
            saveVcSdJwtCredentials = mockSaveVcSdJwtCredentials,
            credentialRefreshDataRepository = mockCredentialRefreshDataRepository,
        )
        setupDefaultMocks()
    }

    @Test
    fun `Saving a batch credential with refreshToken runs specific steps`() = runTest {
        val result = useCase(
            issuerUrl = ISSUER_URL,
            batchSize = BATCH_SIZE,
            anyVerifiedBatchCredential = createAnyVerifiedBatchCredential(),
            identityTrustStatement = mockTrustStatement,
            rawAndParsedCredentialInfo = rawAndParsedIssuerCredentialInfo,
            credentialConfig = credentialConfig,
        )

        val credentialResult = result.assertSuccessType(FetchCredentialResult.Credential::class)
        assertEquals(CREDENTIAL_ID, credentialResult.credentialId)

        coVerifyOrder {
            mockSaveVcSdJwtCredentials(
                issuerUrl = ISSUER_URL,
                vcSdJwtCredentials = listOf(mockVcSdJwtCredential),
                identityTrustStatement = mockTrustStatement,
                rawAndParsedCredentialInfo = rawAndParsedIssuerCredentialInfo,
                credentialConfig = credentialConfig,
            )
            mockCredentialRefreshDataRepository.saveRefreshData(
                credentialId = CREDENTIAL_ID,
                batchSize = BATCH_SIZE,
                accessToken = ACCESS_TOKEN,
                refreshToken = REFRESH_TOKEN,
                dpopKeyBinding = null,
            )
        }
    }

    @Test
    fun `Saving a batch credential without refreshToken does not persist refresh data`() = runTest {
        val result = useCase(
            issuerUrl = ISSUER_URL,
            batchSize = BATCH_SIZE,
            anyVerifiedBatchCredential = createAnyVerifiedBatchCredential().copy(refreshToken = null),
            identityTrustStatement = mockTrustStatement,
            rawAndParsedCredentialInfo = rawAndParsedIssuerCredentialInfo,
            credentialConfig = credentialConfig,
        )

        val credentialResult = result.assertSuccessType(FetchCredentialResult.Credential::class)
        assertEquals(CREDENTIAL_ID, credentialResult.credentialId)

        coVerify(exactly = 0) {
            mockCredentialRefreshDataRepository.saveRefreshData(
                credentialId = any(),
                batchSize = any(),
                accessToken = any(),
                refreshToken = any(),
                dpopKeyBinding = any(),
            )
        }
    }

    @Test
    fun `Saving batch credential maps errors from SaveVcSdJwtCredentials`() = runTest {
        coEvery {
            mockSaveVcSdJwtCredentials(
                credentialId = any(),
                issuerUrl = any(),
                vcSdJwtCredentials = any(),
                identityTrustStatement = any(),
                rawAndParsedCredentialInfo = any(),
                credentialConfig = any(),
            )
        } returns Err(CredentialError.InvalidCredentialOffer)

        useCase(
            credentialId = CREDENTIAL_ID,
            issuerUrl = ISSUER_URL,
            batchSize = BATCH_SIZE,
            anyVerifiedBatchCredential = createAnyVerifiedBatchCredential(),
            identityTrustStatement = mockTrustStatement,
            rawAndParsedCredentialInfo = rawAndParsedIssuerCredentialInfo,
            credentialConfig = credentialConfig,
        ).assertErrorType(CredentialError.InvalidCredentialOffer::class)
    }

    @Test
    fun `Saving batch credential maps errors from SaveBatchRefreshData`() = runTest {
        val exception = Exception("save batch refresh exception")
        coEvery {
            mockCredentialRefreshDataRepository.saveRefreshData(
                credentialId = any(),
                batchSize = any(),
                accessToken = any(),
                refreshToken = any(),
                dpopKeyBinding = any(),
            )
        } returns Err(CredentialRefreshDataError.Unexpected(exception))

        val error = useCase(
            issuerUrl = ISSUER_URL,
            batchSize = BATCH_SIZE,
            anyVerifiedBatchCredential = createAnyVerifiedBatchCredential(),
            identityTrustStatement = mockTrustStatement,
            rawAndParsedCredentialInfo = rawAndParsedIssuerCredentialInfo,
            credentialConfig = credentialConfig,
        ).assertErrorType(CredentialError.Unexpected::class)

        assertEquals(exception, error.cause)
    }

    private fun setupDefaultMocks() {
        coEvery {
            mockSaveVcSdJwtCredentials(
                credentialId = any(),
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
        } returns Ok(1L)
    }

    val rawAndParsedIssuerCredentialInfo by lazy {
        RawAndParsedIssuerCredentialInfo(
            issuerCredentialInfo = oneConfigCredentialInformation,
            rawIssuerCredentialInfo = mockIssuerCredentialInfoJwt,
        )
    }

    private fun createAnyVerifiedBatchCredential() = AnyVerifiedBatchCredential(
        accessToken = ACCESS_TOKEN,
        refreshToken = REFRESH_TOKEN,
        dpopKeyBinding = null,
        vcSdJwtCredentials = listOf(mockVcSdJwtCredential),
    )

    private companion object {
        const val CREDENTIAL_ID = 1337L
        const val BATCH_SIZE: BatchSize = 10
        const val REFRESH_TOKEN = "refresh-token"
        const val ACCESS_TOKEN = "access-token"
        val ISSUER_URL = URL("https://issuer.example")
    }
}

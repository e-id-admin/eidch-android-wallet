package ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.implementation

import ch.admin.foitt.didResolver.domain.DidResolverHelper
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialStatusError
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.TokenStatusList
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.TokenStatusListProperties
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.TokenStatusListResponse
import ch.admin.foitt.wallet.platform.credentialStatus.domain.repository.CredentialStatusRepository
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.FetchStatusFromTokenStatusList
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.ParseTokenStatusList
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.ValidateTokenStatusList
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
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
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.net.URL

class FetchStatusFromTokenStatusListImplTest {
    @MockK
    private lateinit var mockDidResolverHelper: DidResolverHelper

    @MockK
    private lateinit var mockEnvironmentSetupRepository: EnvironmentSetupRepository

    @MockK
    private lateinit var mockCredentialStatusRepository: CredentialStatusRepository

    @MockK
    private lateinit var mockValidateTokenStatusList: ValidateTokenStatusList

    @MockK
    private lateinit var mockParseTokenStatusList: ParseTokenStatusList

    @MockK
    private lateinit var mockStatusList: TokenStatusList

    @MockK
    private lateinit var mockTokenStatusListProperties: TokenStatusListProperties

    private lateinit var useCase: FetchStatusFromTokenStatusList

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = FetchStatusFromTokenStatusListImpl(
            didResolverHelper = mockDidResolverHelper,
            environmentSetupRepository = mockEnvironmentSetupRepository,
            credentialStatusRepository = mockCredentialStatusRepository,
            validateTokenStatusList = mockValidateTokenStatusList,
            parseTokenStatusList = mockParseTokenStatusList,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @ParameterizedTest
    @MethodSource("generateStatusMapping")
    fun `Token status code should map to correct CredentialStatus`(statusMap: Pair<Int, CredentialStatus>): Unit = runTest {
        coEvery { mockParseTokenStatusList(mockStatusList, INDEX) } returns Ok(statusMap.first)

        val status = useCase(CREDENTIAL_ISSUER, mockTokenStatusListProperties).assertOk()

        assertEquals(statusMap.second, status)
    }

    @Test
    fun `Fetching token status maps errors from did resolver`() = runTest {
        val exception = IllegalStateException("did error")
        coEvery {
            mockDidResolverHelper.getHttpsUrl(any())
        } returns Err(exception)

        useCase(CREDENTIAL_ISSUER, mockTokenStatusListProperties)
            .assertErrorType(CredentialStatusError.Unexpected::class)
    }

    @Test
    fun `Fetching token status with with invalid status list uri returns an error`() = runTest {
        every { mockTokenStatusListProperties.statusList.uri } returns "not an uri"

        useCase(CREDENTIAL_ISSUER, mockTokenStatusListProperties)
            .assertErrorType(CredentialStatusError.UnknownRegistry::class)
    }

    @Test
    fun `Fetching token status with untrusted URI host returns error without fetching`(): Unit = runTest {
        every { mockTokenStatusListProperties.statusList.uri } returns UNTRUSTED_URI

        useCase(CREDENTIAL_ISSUER, mockTokenStatusListProperties)
            .assertErrorType(CredentialStatusError.UnknownRegistry::class)

        coVerify(exactly = 0) {
            mockCredentialStatusRepository.fetchTokenStatusListJwt(any())
        }
    }

    @Test
    fun `Fetching token status maps error from fetching status list jwt`(): Unit = runTest {
        val exception = IllegalStateException("message")
        coEvery {
            mockCredentialStatusRepository.fetchTokenStatusListJwt(any())
        } returns Err(CredentialStatusError.Unexpected(exception))

        val result = useCase(CREDENTIAL_ISSUER, mockTokenStatusListProperties)

        val error = result.assertErrorType(CredentialStatusError.Unexpected::class)
        assertEquals(exception.message, error.cause?.message)
    }

    @Test
    fun `Fetching token status maps error from validating status list`(): Unit = runTest {
        val exception = IllegalStateException("message")
        coEvery { mockValidateTokenStatusList(any(), any(), any()) } returns Err(CredentialStatusError.Unexpected(exception))

        val result = useCase(CREDENTIAL_ISSUER, mockTokenStatusListProperties)

        val error = result.assertErrorType(CredentialStatusError.Unexpected::class)
        assertEquals(exception.message, error.cause?.message)
    }

    @Test
    fun `Fetching token status with DidDocumentDeactivated error will return Unexpected error`(): Unit = runTest {
        val exception = CredentialStatusError.Unexpected(null)
        coEvery { mockValidateTokenStatusList(any(), any(), any()) } returns Err(CredentialStatusError.DidDocumentDeactivated)

        val result = useCase(CREDENTIAL_ISSUER, mockTokenStatusListProperties)

        val error = result.assertErrorType(CredentialStatusError.Unexpected::class)
        assertEquals(exception, error)
    }

    @Test
    fun `Fetching token status maps error from parsing status list`(): Unit = runTest {
        val exception = IllegalStateException("parsing error")
        coEvery { mockParseTokenStatusList(any(), any()) } returns Err(CredentialStatusError.Unexpected(exception))

        val result = useCase(CREDENTIAL_ISSUER, mockTokenStatusListProperties)

        val error = result.assertErrorType(CredentialStatusError.Unexpected::class)
        assertEquals(exception.message, error.cause?.message)
    }

    private fun setupDefaultMocks() {
        every { mockTokenStatusListProperties.statusList.index } returns INDEX
        every { mockTokenStatusListProperties.statusList.uri } returns TRUSTED_URI

        coEvery { mockDidResolverHelper.getHttpsUrl(CREDENTIAL_ISSUER) } returns Ok(statusListIdentifierUrl)
        coEvery { mockEnvironmentSetupRepository.statusListMapping } returns statusListMapping
        coEvery { mockCredentialStatusRepository.fetchTokenStatusListJwt(TRUSTED_URI) } returns Ok(JWT)
        coEvery {
            mockValidateTokenStatusList(CREDENTIAL_ISSUER, JWT, TRUSTED_URI)
        } returns Ok(TokenStatusListResponse(statusList = mockStatusList))
        coEvery { mockParseTokenStatusList(mockStatusList, INDEX) } returns Ok(0)
    }

    private companion object {
        const val INDEX = 1
        const val STATUS_LIST_HOST = "status.statuslist.ch"
        const val TRUSTED_URI = "https://$STATUS_LIST_HOST/api/v1/statuslist/1"
        const val UNTRUSTED_URI = "https://evil.example.com/api/v1/statuslist/1"
        const val JWT = "jwt"
        const val CREDENTIAL_ISSUER = "credentialIssuer"
        const val STATUS_LIST_IDENTIFIER = "identifier.statuslist.ch"
        val statusListIdentifierUrl = URL("https://$STATUS_LIST_IDENTIFIER/api/v1/statuslist")
        val statusListMapping = mapOf(
            STATUS_LIST_IDENTIFIER to STATUS_LIST_HOST
        )

        @JvmStatic
        fun generateStatusMapping() = listOf(
            0 to CredentialStatus.VALID,
            1 to CredentialStatus.REVOKED,
            2 to CredentialStatus.SUSPENDED,
            3 to CredentialStatus.UNSUPPORTED,
            4 to CredentialStatus.UNSUPPORTED,
            5 to CredentialStatus.UNSUPPORTED,
        )
    }
}

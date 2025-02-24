package ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialStatusError
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.TokenStatusList
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.TokenStatusListProperties
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.TokenStatusListResponse
import ch.admin.foitt.wallet.platform.credentialStatus.domain.repository.CredentialStatusRepository
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.ParseTokenStatusList
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.ValidateTokenStatusList
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class FetchStatusFromTokenStatusListImplTest {
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

    private lateinit var useCase: FetchStatusFromTokenStatusListImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = FetchStatusFromTokenStatusListImpl(
            credentialStatusRepository = mockCredentialStatusRepository,
            validateTokenStatusList = mockValidateTokenStatusList,
            parseTokenStatusList = mockParseTokenStatusList,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @ParameterizedTest
    @MethodSource("generateStatusMapping")
    fun `Token status code should map to correct CredentialStatus`(statusMap: Pair<Int, CredentialStatus>): Unit = runTest {
        setDefaultMockedReturns()
        coEvery { mockParseTokenStatusList(mockStatusList, INDEX) } returns Ok(statusMap.first)

        val status = useCase(CREDENTIAL_ISSUER, mockTokenStatusListProperties).assertOk()

        assertEquals(statusMap.second, status)
    }

    @Test
    fun `Fetching token status maps error from fetching status list jwt`(): Unit = runTest {
        setDefaultMockedReturns()
        val exception = IllegalStateException("message")
        coEvery {
            mockCredentialStatusRepository.fetchTokenStatusListJwt(any())
        } returns Err(CredentialStatusError.Unexpected(exception))

        val result = useCase(CREDENTIAL_ISSUER, mockTokenStatusListProperties)

        val error = result.assertErrorType(CredentialStatusError.Unexpected::class)
        assertEquals(exception.message, error.cause?.message)
    }

    @Test
    fun `Fetching token status maps error from parsing status list`(): Unit = runTest {
        setDefaultMockedReturns()
        val exception = IllegalStateException("message")
        coEvery { mockParseTokenStatusList(any(), any()) } returns Err(CredentialStatusError.Unexpected(exception))

        val result = useCase(CREDENTIAL_ISSUER, mockTokenStatusListProperties)

        val error = result.assertErrorType(CredentialStatusError.Unexpected::class)
        assertEquals(exception.message, error.cause?.message)
    }

    @Test
    fun `Fetching token status maps error from validating status list`(): Unit = runTest {
        setDefaultMockedReturns()
        val exception = IllegalStateException("message")
        coEvery { mockValidateTokenStatusList(any(), any(), any()) } returns Err(CredentialStatusError.Unexpected(exception))

        val result = useCase(CREDENTIAL_ISSUER, mockTokenStatusListProperties)

        val error = result.assertErrorType(CredentialStatusError.Unexpected::class)
        assertEquals(exception.message, error.cause?.message)
    }

    private fun setDefaultMockedReturns(statusValue: Int = 0) {
        every { mockTokenStatusListProperties.statusList.index } returns INDEX
        every { mockTokenStatusListProperties.statusList.uri } returns URI

        coEvery { mockCredentialStatusRepository.fetchTokenStatusListJwt(URI) } returns Ok(JWT)
        coEvery { mockValidateTokenStatusList(CREDENTIAL_ISSUER, JWT, URI) } returns
            Ok(TokenStatusListResponse(statusList = mockStatusList))
        coEvery { mockParseTokenStatusList(mockStatusList, INDEX) } returns Ok(statusValue)
    }

    private companion object {
        const val INDEX = 1
        const val URI = "uri"
        const val JWT = "jwt"
        const val CREDENTIAL_ISSUER = "credentialIssuer"

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

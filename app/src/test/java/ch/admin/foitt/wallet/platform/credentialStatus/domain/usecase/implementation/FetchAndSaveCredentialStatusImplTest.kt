package ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.CredentialStatusError
import ch.admin.foitt.wallet.platform.credentialStatus.domain.model.TokenStatusListProperties
import ch.admin.foitt.wallet.platform.credentialStatus.domain.usecase.FetchStatusFromTokenStatusList
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FetchAndSaveCredentialStatusImplTest {

    @MockK
    private lateinit var mockFetchStatusFromTokenStatusList: FetchStatusFromTokenStatusList

    @MockK
    private lateinit var mockTokenStatusListProperties: TokenStatusListProperties

    private lateinit var useCase: FetchCredentialStatusImpl

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        useCase = FetchCredentialStatusImpl(mockFetchStatusFromTokenStatusList)

        success()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Fetching credential status for token status list returns credential status`() = runTest {
        val result = useCase(credendialIssuer, mockTokenStatusListProperties)

        val status = result.assertOk()
        assertEquals(credentialStatus, status)
    }

    @Test
    fun `Fetching credential status maps error from fetching jwt status from token status list`() = runTest {
        val exception = IllegalStateException()
        coEvery { mockFetchStatusFromTokenStatusList(any(), any()) } returns Err(CredentialStatusError.Unexpected(exception))

        val result = useCase(credendialIssuer, mockTokenStatusListProperties)

        val error = result.assertErrorType(CredentialStatusError.Unexpected::class)
        assertEquals(exception, error.cause)
    }

    private fun success() {
        coEvery {
            mockFetchStatusFromTokenStatusList(credendialIssuer, mockTokenStatusListProperties)
        } returns Ok(credentialStatus)
    }

    private companion object {
        val credentialStatus = CredentialStatus.VALID
        const val credendialIssuer = "credentialIssuer"
    }
}

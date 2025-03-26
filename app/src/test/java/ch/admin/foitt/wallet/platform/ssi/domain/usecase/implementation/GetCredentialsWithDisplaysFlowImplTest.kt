package ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.MapToCredentialDisplayData
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialWithDisplays
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialWithDisplaysRepository
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.GetCredentialsWithDisplaysFlow
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.mock.MockCredentialWithDisplays.credential1Displays
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.mock.MockCredentialWithDisplays.credential2Displays
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.mock.MockCredentialWithDisplays.credentialDisplayData1
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.mock.MockCredentialWithDisplays.credentialDisplayData2
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetCredentialsWithDisplaysFlowImplTest {

    @MockK
    lateinit var mockCredentialWithDisplaysRepository: CredentialWithDisplaysRepository

    @MockK
    lateinit var mockMapToCredentialDisplayData: MapToCredentialDisplayData

    @MockK
    lateinit var mockCredential1: Credential

    @MockK
    lateinit var mockCredential2: Credential

    @MockK
    lateinit var mockCredentialWithDisplays1: CredentialWithDisplays

    @MockK
    lateinit var mockCredentialWithDisplays2: CredentialWithDisplays

    private lateinit var getCredentialsWithDisplaysFlow: GetCredentialsWithDisplaysFlow

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        getCredentialsWithDisplaysFlow = GetCredentialsWithDisplaysFlowImpl(
            mockCredentialWithDisplaysRepository,
            mockMapToCredentialDisplayData,
        )

        success()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Getting the credentialsWithDisplays flow without updates returns a flow with 2 credential previews`() = runTest {
        val result = getCredentialsWithDisplaysFlow().firstOrNull()

        assertNotNull(result)
        val credentialList = result?.assertOk()
        assertEquals(2, credentialList?.size)
        assertEquals(credentialDisplayData1, credentialList?.get(0))
        assertEquals(credentialDisplayData2, credentialList?.get(1))
    }

    @Test
    fun `Getting the credentialsWithDisplays flow maps errors from the repository`() = runTest {
        val exception = IllegalStateException("db error")
        coEvery {
            mockCredentialWithDisplaysRepository.getCredentialsWithDisplays()
        } returns flowOf(Err(SsiError.Unexpected(exception)))

        val result = getCredentialsWithDisplaysFlow().firstOrNull()

        assertNotNull(result)
        val error = result?.assertErrorType(SsiError.Unexpected::class)
        assertEquals(exception, error?.cause)
    }

    @Test
    fun `Getting the credentialsWithDisplays flow maps errors from the MapToCredentialDisplayData use case`() = runTest {
        val exception = IllegalStateException("map to credential display data error")
        coEvery {
            mockMapToCredentialDisplayData(mockCredential1, credential1Displays)
        } returns Err(CredentialError.Unexpected(exception))

        val result = getCredentialsWithDisplaysFlow().firstOrNull()

        assertNotNull(result)
        result?.assertErrorType(SsiError.Unexpected::class)
    }

    private fun success() {
        every { mockCredentialWithDisplays1.credential } returns mockCredential1
        every { mockCredentialWithDisplays1.displays } returns credential1Displays

        every { mockCredentialWithDisplays2.credential } returns mockCredential2
        every { mockCredentialWithDisplays2.displays } returns credential2Displays

        coEvery {
            mockCredentialWithDisplaysRepository.getCredentialsWithDisplays()
        } returns flowOf(Ok(listOf(mockCredentialWithDisplays1, mockCredentialWithDisplays2)))

        coEvery {
            mockMapToCredentialDisplayData(mockCredential1, credential1Displays)
        } returns Ok(credentialDisplayData1)

        coEvery {
            mockMapToCredentialDisplayData(mockCredential2, credential2Displays)
        } returns Ok(credentialDisplayData2)
    }
}

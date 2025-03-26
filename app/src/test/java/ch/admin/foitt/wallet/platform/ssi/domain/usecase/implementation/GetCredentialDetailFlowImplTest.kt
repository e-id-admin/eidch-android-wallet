package ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.MapToCredentialDisplayData
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimWithDisplays
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialWithDisplaysAndClaims
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialWithDisplaysAndClaimsRepository
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.GetCredentialDetailFlow
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.MapToCredentialClaimData
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.mock.MockCredentialDetail.CREDENTIAL_ID
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.mock.MockCredentialDetail.claimData1
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.mock.MockCredentialDetail.claimData2
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.mock.MockCredentialDetail.claimWithDisplays1
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.mock.MockCredentialDetail.claimWithDisplays2
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.mock.MockCredentialDetail.claims
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.mock.MockCredentialDetail.credentialDetail
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.mock.MockCredentialDetail.credentialDetail2
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.mock.MockCredentialDetail.credentialDisplayData
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.implementation.mock.MockCredentialDetail.credentialDisplays
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.getError
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetCredentialDetailFlowImplTest {

    @MockK
    lateinit var mockCredentialWithDisplaysAndClaimsRepository: CredentialWithDisplaysAndClaimsRepository

    @MockK
    lateinit var mockMapToCredentialDisplayData: MapToCredentialDisplayData

    @MockK
    lateinit var mockMapToCredentialClaimData: MapToCredentialClaimData

    @MockK
    lateinit var mockCredential: Credential

    @MockK
    lateinit var mockCredentialWithDisplaysAndClaims: CredentialWithDisplaysAndClaims

    private lateinit var getCredentialDetailFlow: GetCredentialDetailFlow

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        getCredentialDetailFlow = GetCredentialDetailFlowImpl(
            mockCredentialWithDisplaysAndClaimsRepository,
            mockMapToCredentialDisplayData,
            mockMapToCredentialClaimData,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Getting the credential detail flow without updates returns a flow with one credential detail`() = runTest {
        val result = getCredentialDetailFlow(CREDENTIAL_ID).firstOrNull()

        assertNotNull(result)
        result?.assertOk()
    }

    @Test
    fun `Getting the credential detail flow with updates returns a flow with the credential detail`() = runTest {
        coEvery {
            mockCredentialWithDisplaysAndClaimsRepository.getNullableCredentialWithDisplaysAndClaimsFlowById(CREDENTIAL_ID)
        } returns flow {
            emit(Ok(mockCredentialWithDisplaysAndClaims))
            emit(Ok(mockCredentialWithDisplaysAndClaims))
        }

        val result = getCredentialDetailFlow(CREDENTIAL_ID).toList()
        assertEquals(2, result.size)
        result[0].assertOk()
        result[1].assertOk()
        assertEquals(credentialDetail, result[0].value)
        assertEquals(credentialDetail2, result[1].value)
    }

    @Test
    fun `Getting the credential detail flow maps errors from the repository`() = runTest {
        val exception = IllegalStateException("db error")
        coEvery {
            mockCredentialWithDisplaysAndClaimsRepository.getNullableCredentialWithDisplaysAndClaimsFlowById(CREDENTIAL_ID)
        } returns flowOf(Err(SsiError.Unexpected(exception)))

        val result = getCredentialDetailFlow(CREDENTIAL_ID).firstOrNull()

        assertNotNull(result)
        result?.assertErrorType(SsiError.Unexpected::class)
        val error = result?.getError() as SsiError.Unexpected
        assertEquals(exception, error.cause)
    }

    @Test
    fun `Getting the credential detail flow maps errors from the MapToCredentialDisplayData use case`() = runTest {
        val exception = IllegalStateException("map to credential display data error")
        coEvery {
            mockMapToCredentialDisplayData(mockCredential, credentialDisplays)
        } returns Err(CredentialError.Unexpected(exception))

        val result = getCredentialDetailFlow(CREDENTIAL_ID).firstOrNull()

        assertNotNull(result)
        result?.assertErrorType(SsiError.Unexpected::class)
    }

    @Test
    fun `Getting the credential detail flow maps errors from the MapToCredentialClaimData use case`() = runTest {
        val exception = IllegalStateException("no claim displays found")
        coEvery {
            mockMapToCredentialClaimData(any<CredentialClaimWithDisplays>())
        } returns Err(SsiError.Unexpected(exception))

        val result = getCredentialDetailFlow(CREDENTIAL_ID).firstOrNull()

        assertNotNull(result)
        val error = result?.assertErrorType(SsiError.Unexpected::class)
        assertEquals(exception, error?.cause)
    }

    private fun setupDefaultMocks() {
        every { mockCredentialWithDisplaysAndClaims.credential } returns mockCredential
        every { mockCredentialWithDisplaysAndClaims.credentialDisplays } returns credentialDisplays
        every { mockCredentialWithDisplaysAndClaims.claims } returns claims

        coEvery {
            mockCredentialWithDisplaysAndClaimsRepository.getNullableCredentialWithDisplaysAndClaimsFlowById(CREDENTIAL_ID)
        } returns flowOf(Ok(mockCredentialWithDisplaysAndClaims))
        coEvery {
            mockMapToCredentialDisplayData(mockCredential, credentialDisplays)
        } returns Ok(credentialDisplayData)
        coEvery {
            mockMapToCredentialClaimData(claimWithDisplays1)
        } returns Ok(claimData1)
        coEvery {
            mockMapToCredentialClaimData(claimWithDisplays2)
        } returns Ok(claimData2)
    }
}

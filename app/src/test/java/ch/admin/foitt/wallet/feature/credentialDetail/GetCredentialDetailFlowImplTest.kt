package ch.admin.foitt.wallet.feature.credentialDetail

import ch.admin.foitt.wallet.feature.credentialDetail.domain.model.CredentialDetailError
import ch.admin.foitt.wallet.feature.credentialDetail.domain.repository.CredentialDetailRepository
import ch.admin.foitt.wallet.feature.credentialDetail.domain.usecase.GetCredentialDetailFlow
import ch.admin.foitt.wallet.feature.credentialDetail.domain.usecase.implementation.GetCredentialDetailFlowImpl
import ch.admin.foitt.wallet.feature.credentialDetail.mock.MockCredentialDetail.CREDENTIAL_ID
import ch.admin.foitt.wallet.feature.credentialDetail.mock.MockCredentialDetail.claimData1
import ch.admin.foitt.wallet.feature.credentialDetail.mock.MockCredentialDetail.claimData2
import ch.admin.foitt.wallet.feature.credentialDetail.mock.MockCredentialDetail.claimWithDisplays1
import ch.admin.foitt.wallet.feature.credentialDetail.mock.MockCredentialDetail.claimWithDisplays2
import ch.admin.foitt.wallet.feature.credentialDetail.mock.MockCredentialDetail.credentialDetail
import ch.admin.foitt.wallet.feature.credentialDetail.mock.MockCredentialDetail.credentialDetail2
import ch.admin.foitt.wallet.feature.credentialDetail.mock.MockCredentialDetail.credentialDisplay1
import ch.admin.foitt.wallet.feature.credentialDetail.mock.MockCredentialDetail.credentialWithDetails1
import ch.admin.foitt.wallet.feature.credentialDetail.mock.MockCredentialDetail.credentialWithDetails2
import ch.admin.foitt.wallet.feature.credentialDetail.mock.MockCredentialDetail.issuerDisplay1
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.IsCredentialFromBetaIssuerImpl
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimDisplay
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedDisplay
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.MapToCredentialClaimData
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.getError
import io.mockk.MockKAnnotations
import io.mockk.coEvery
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
    lateinit var mockCredentialDetailRepository: CredentialDetailRepository

    @MockK
    lateinit var mockGetLocalizedDisplay: GetLocalizedDisplay

    @MockK
    lateinit var mockMapToCredentialClaimData: MapToCredentialClaimData

    @MockK
    lateinit var mockIsCredentialFromBetaIssuerImpl: IsCredentialFromBetaIssuerImpl

    private lateinit var getCredentialDetailFlow: GetCredentialDetailFlow

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        getCredentialDetailFlow = GetCredentialDetailFlowImpl(
            mockCredentialDetailRepository,
            mockGetLocalizedDisplay,
            mockMapToCredentialClaimData,
            mockIsCredentialFromBetaIssuerImpl
        )

        success()
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
            mockCredentialDetailRepository.getCredentialDetailByIdFlow(CREDENTIAL_ID)
        } returns flow {
            emit(Ok(credentialWithDetails1))
            emit(Ok(credentialWithDetails2))
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
            mockCredentialDetailRepository.getCredentialDetailByIdFlow(any())
        } returns flowOf(Err(SsiError.Unexpected(exception)))

        val result = getCredentialDetailFlow(CREDENTIAL_ID).firstOrNull()

        assertNotNull(result)
        result?.assertErrorType(CredentialDetailError.Unexpected::class)
        val error = result?.getError() as CredentialDetailError.Unexpected
        assertEquals(exception, error.throwable)
    }

    @Test
    fun `Getting the credential detail flow maps errors from the GetLocalizedDisplay use case`() = runTest {
        coEvery { mockGetLocalizedDisplay(credentialWithDetails1.credentialDisplays) } returns null

        val result = getCredentialDetailFlow(CREDENTIAL_ID).firstOrNull()

        assertNotNull(result)
        result?.assertErrorType(CredentialDetailError.Unexpected::class)
    }

    @Test
    fun `Getting the credential detail flow maps from the MapToCredentialClaimData use case`() = runTest {
        val exception = IllegalStateException("no claim displays found")
        coEvery {
            mockMapToCredentialClaimData(any<CredentialClaim>(), any<List<CredentialClaimDisplay>>())
        } returns Err(SsiError.Unexpected(exception))

        val result = getCredentialDetailFlow(CREDENTIAL_ID).firstOrNull()

        assertNotNull(result)
        result?.assertErrorType(CredentialDetailError.Unexpected::class)
        val error = result?.getError() as CredentialDetailError.Unexpected
        assertEquals(exception, error.throwable)
    }

    private fun success() {
        coEvery {
            mockCredentialDetailRepository.getCredentialDetailByIdFlow(CREDENTIAL_ID)
        } returns flowOf(Ok(credentialWithDetails1))
        coEvery { mockGetLocalizedDisplay(credentialWithDetails1.credentialDisplays) } returns credentialDisplay1
        coEvery { mockGetLocalizedDisplay(credentialWithDetails1.issuerDisplays) } returns issuerDisplay1
        coEvery {
            mockMapToCredentialClaimData(claimWithDisplays1.claim, claimWithDisplays1.displays)
        } returns Ok(claimData1)
        coEvery {
            mockMapToCredentialClaimData(claimWithDisplays2.claim, claimWithDisplays2.displays)
        } returns Ok(claimData2)
        coEvery {
            mockIsCredentialFromBetaIssuerImpl(any())
        } returns false
    }
}

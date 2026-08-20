package ch.admin.foitt.wallet.feature.credentialDetail.domain.usecase.implementation

import ch.admin.foitt.wallet.feature.credentialDetail.domain.model.CredentialDetailError
import ch.admin.foitt.wallet.feature.credentialDetail.domain.model.IssuanceType
import ch.admin.foitt.wallet.platform.credentialRefresh.domain.model.CredentialRefreshDataError
import ch.admin.foitt.wallet.platform.credentialRefresh.domain.repository.CredentialRefreshDataRepository
import ch.admin.foitt.wallet.platform.database.domain.model.BatchRefreshDataEntity
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

class GetCredentialIssuanceTypeImplTest {

    @MockK
    private lateinit var mockCredentialRefreshDataRepository: CredentialRefreshDataRepository

    private lateinit var useCase: GetCredentialIssuanceTypeImpl

    private val credentialId = 1L

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = GetCredentialIssuanceTypeImpl(mockCredentialRefreshDataRepository)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `No batch refresh data returns STANDARD`() = runTest {
        coEvery { mockCredentialRefreshDataRepository.getBatchRefreshDataById(credentialId) } returns Ok(null)

        assertEquals(Ok(IssuanceType.STANDARD), useCase(credentialId))
    }

    @Test
    fun `Batch size of 1 returns STANDARD (BETA-ID workaround)`() = runTest {
        coEvery {
            mockCredentialRefreshDataRepository.getBatchRefreshDataById(credentialId)
        } returns Ok(BatchRefreshDataEntity(credentialId = credentialId, batchSize = 1))

        assertEquals(Ok(IssuanceType.STANDARD), useCase(credentialId))
    }

    @Test
    fun `Batch size greater than 1 returns BATCH`() = runTest {
        coEvery {
            mockCredentialRefreshDataRepository.getBatchRefreshDataById(credentialId)
        } returns Ok(BatchRefreshDataEntity(credentialId = credentialId, batchSize = 10))

        assertEquals(Ok(IssuanceType.BATCH), useCase(credentialId))
    }

    @Test
    fun `A repository error returns an error`() = runTest {
        val cause = IllegalStateException("error")
        coEvery {
            mockCredentialRefreshDataRepository.getBatchRefreshDataById(credentialId)
        } returns Err(CredentialRefreshDataError.Unexpected(cause))

        assertEquals(Err(CredentialDetailError.Unexpected(cause)), useCase(credentialId))
    }
}

package ch.admin.foitt.wallet.platform.batch.domain.usecase.implementation

import ch.admin.foitt.openid4vc.domain.model.BatchSize
import ch.admin.foitt.wallet.platform.batch.domain.usecase.GetBatchSize
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

class GetBatchSizeImplTest {

    @MockK
    private lateinit var mockCredentialRefreshDataRepository: CredentialRefreshDataRepository

    private lateinit var useCase: GetBatchSize

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = GetBatchSizeImpl(
            credentialRefreshDataRepository = mockCredentialRefreshDataRepository,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Returns the stored batch size when refresh data is present`() = runTest {
        val batchSize: BatchSize = 5
        coEvery {
            mockCredentialRefreshDataRepository.getBatchRefreshDataById(CREDENTIAL_ID)
        } returns Ok(
            BatchRefreshDataEntity(
                credentialId = CREDENTIAL_ID,
                batchSize = batchSize,
            )
        )

        assertEquals(batchSize, useCase(CREDENTIAL_ID))
    }

    @Test
    fun `Returns zero when there is no refresh data`() = runTest {
        coEvery {
            mockCredentialRefreshDataRepository.getBatchRefreshDataById(CREDENTIAL_ID)
        } returns Ok(null)

        assertEquals(0, useCase(CREDENTIAL_ID))
    }

    @Test
    fun `Returns zero when fetching the refresh data fails`() = runTest {
        coEvery {
            mockCredentialRefreshDataRepository.getBatchRefreshDataById(CREDENTIAL_ID)
        } returns Err(CredentialRefreshDataError.Unexpected(IllegalStateException("refresh data repo error")))

        assertEquals(0, useCase(CREDENTIAL_ID))
    }

    private companion object {
        const val CREDENTIAL_ID = 42L
    }
}

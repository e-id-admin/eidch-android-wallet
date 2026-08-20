package ch.admin.foitt.wallet.platform.credentialRefresh

import ch.admin.foitt.openid4vc.domain.model.TokenType
import ch.admin.foitt.wallet.platform.credentialRefresh.domain.model.CredentialRefreshDataError
import ch.admin.foitt.wallet.platform.credentialRefresh.domain.repository.CredentialRefreshDataRepository
import ch.admin.foitt.wallet.platform.credentialRefresh.domain.usecase.implementation.IsCredentialRefreshableImpl
import ch.admin.foitt.wallet.platform.database.domain.model.BundleItemEntity
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialAuthenticationEntity
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialStatus
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.BundleItemRepository
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
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.junit.jupiter.params.provider.ValueSource

class IsCredentialRefreshableImplTest {

    @MockK
    private lateinit var mockCredentialRefreshDataRepository: CredentialRefreshDataRepository

    @MockK
    private lateinit var mockBundleItemRepository: BundleItemRepository

    private lateinit var useCase: IsCredentialRefreshableImpl

    private val authenticationEntity = CredentialAuthenticationEntity(
        id = 1,
        credentialId = 2,
        tokenType = TokenType.DPOP,
        accessToken = "accessToken",
        refreshToken = "refreshToken",
    )

    private val bundleItem = BundleItemEntity(
        id = 1,
        status = CredentialStatus.VALID,
        credentialId = 2,
        payload = "payload",
    )

    private val credentialId = 1L

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = IsCredentialRefreshableImpl(
            credentialRefreshDataRepository = mockCredentialRefreshDataRepository,
            bundleItemRepository = mockBundleItemRepository,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `A successful response from the repository is mapped`(isRefreshable: Boolean) = runTest {
        coEvery {
            mockCredentialRefreshDataRepository.getCredentialAuthenticationById(credentialId)
        } returns Ok(authenticationEntity.copy(refreshToken = if (isRefreshable) "refreshToken" else null))

        assertEquals(isRefreshable, useCase(credentialId))
    }

    @Test
    fun `A revoked credential is not refreshable`() = runTest {
        coEvery {
            mockBundleItemRepository.getAllByCredentialId(credentialId)
        } returns Ok(listOf(bundleItem.copy(status = CredentialStatus.REVOKED)))

        assertEquals(false, useCase(credentialId))
    }

    @ParameterizedTest
    @EnumSource(value = CredentialStatus::class, names = ["REVOKED"], mode = EnumSource.Mode.EXCLUDE)
    fun `A credential which is not revoked is refreshable`(status: CredentialStatus) = runTest {
        coEvery {
            mockBundleItemRepository.getAllByCredentialId(credentialId)
        } returns Ok(listOf(bundleItem.copy(status = status)))

        assertEquals(true, useCase(credentialId))
    }

    @Test
    fun `A repository error returns false`() = runTest {
        coEvery {
            mockCredentialRefreshDataRepository.getCredentialAuthenticationById(any())
        } returns Err(CredentialRefreshDataError.Unexpected(IllegalStateException("get refresh error")))

        assertEquals(false, useCase(credentialId))
    }

    @Test
    fun `A bundle item repository error does not block the refresh`() = runTest {
        coEvery {
            mockBundleItemRepository.getAllByCredentialId(any())
        } returns Err(SsiError.Unexpected(IllegalStateException("get bundle items error")))

        assertEquals(true, useCase(credentialId))
    }

    private fun setupDefaultMocks() {
        coEvery {
            mockCredentialRefreshDataRepository.getCredentialAuthenticationById(any())
        } returns Ok(authenticationEntity)

        coEvery {
            mockBundleItemRepository.getAllByCredentialId(any())
        } returns Ok(listOf(bundleItem))
    }
}

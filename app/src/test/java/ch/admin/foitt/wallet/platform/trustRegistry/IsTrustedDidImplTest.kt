package ch.admin.foitt.wallet.platform.trustRegistry

import ch.admin.foitt.didResolver.domain.DidResolverHelper
import ch.admin.foitt.wallet.platform.environmentSetup.domain.repository.EnvironmentSetupRepository
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustRegistryError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.GetTrustDomainFromDid
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.IsTrustedDid
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.implementation.IsTrustedDidImpl
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class IsTrustedDidImplTest {

    @MockK
    private lateinit var mockDidResolverHelper: DidResolverHelper

    @MockK
    private lateinit var mockGetTrustDomainFromDid: GetTrustDomainFromDid

    @MockK
    private lateinit var mockEnvironmentSetupRepository: EnvironmentSetupRepository

    private lateinit var useCase: IsTrustedDid

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)

        useCase = IsTrustedDidImpl(
            didResolverHelper = mockDidResolverHelper,
            getTrustDomainFromDid = mockGetTrustDomainFromDid,
            environmentSetupRepository = mockEnvironmentSetupRepository,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `A valid VerificationAuthorizationTrustStatement passes validation`() = runTest {
        useCase(KEY_ID, TYPE).assertOk()
    }

    @Test
    fun `Errors from did resolver are mapped`() = runTest {
        val exception = IllegalStateException("jwt error")
        coEvery {
            mockDidResolverHelper.getDidStringFromAbsoluteKeyId(any())
        } returns Err(exception)

        useCase(KEY_ID, TYPE).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `Errors from getting the trust domain are mapped`() = runTest {
        val exception = IllegalStateException("jwt error")
        coEvery {
            mockGetTrustDomainFromDid(any())
        } returns Err(TrustRegistryError.Unexpected(exception))

        useCase(KEY_ID, TYPE).assertErrorType(TrustRegistryError.Unexpected::class)
    }

    @Test
    fun `Dids not found for trust domain returns and error`() = runTest {
        coEvery { mockGetTrustDomainFromDid(DID) } returns Ok("other did")

        useCase(KEY_ID, TYPE).assertErrorType(TrustRegistryError.UnknownRegistry::class)
    }

    @Test
    fun `Dids not found for type returns and error`() = runTest {
        useCase(KEY_ID, "other type").assertErrorType(TrustRegistryError.UnknownRegistry::class)
    }

    @Test
    fun `Not trusted did returns and error`() = runTest {
        coEvery { mockEnvironmentSetupRepository.trustRegistryTrustedDids } returns mapOf(
            TRUST_DOMAIN to mapOf(
                TYPE to listOf("other did"),
            )
        )

        useCase(KEY_ID, TYPE).assertErrorType(TrustRegistryError.UnknownRegistry::class)
    }

    private fun setupDefaultMocks() {
        coEvery { mockDidResolverHelper.getDidStringFromAbsoluteKeyId(KEY_ID) } returns Ok(DID)

        coEvery { mockGetTrustDomainFromDid(DID) } returns Ok(TRUST_DOMAIN)

        coEvery { mockEnvironmentSetupRepository.trustRegistryTrustedDids } returns trustedDids
    }

    private companion object {
        const val KEY_ID = "kid"
        const val DID = "did"
        const val TRUST_DOMAIN = "trust domain"
        const val TYPE = "type"
        val trustedDids = mapOf(
            TRUST_DOMAIN to mapOf(
                TYPE to listOf(DID),
            )
        )
    }
}

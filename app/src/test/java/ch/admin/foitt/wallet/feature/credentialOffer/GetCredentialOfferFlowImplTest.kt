package ch.admin.foitt.wallet.feature.credentialOffer

import ch.admin.foitt.wallet.feature.credentialOffer.domain.model.CredentialOffer
import ch.admin.foitt.wallet.feature.credentialOffer.domain.model.CredentialOfferError
import ch.admin.foitt.wallet.feature.credentialOffer.domain.usecase.GetCredentialOfferFlow
import ch.admin.foitt.wallet.feature.credentialOffer.domain.usecase.implementation.GetCredentialOfferFlowImpl
import ch.admin.foitt.wallet.feature.credentialOffer.mock.MockCredentialOffer.CREDENTIAL_ID
import ch.admin.foitt.wallet.feature.credentialOffer.mock.MockCredentialOffer.ISSUER
import ch.admin.foitt.wallet.feature.credentialOffer.mock.MockCredentialOffer.claimData1
import ch.admin.foitt.wallet.feature.credentialOffer.mock.MockCredentialOffer.claimData2
import ch.admin.foitt.wallet.feature.credentialOffer.mock.MockCredentialOffer.claimWithDisplays1
import ch.admin.foitt.wallet.feature.credentialOffer.mock.MockCredentialOffer.claimWithDisplays2
import ch.admin.foitt.wallet.feature.credentialOffer.mock.MockCredentialOffer.claims
import ch.admin.foitt.wallet.feature.credentialOffer.mock.MockCredentialOffer.credentialDisplayData
import ch.admin.foitt.wallet.feature.credentialOffer.mock.MockCredentialOffer.credentialDisplays
import ch.admin.foitt.wallet.feature.credentialOffer.mock.MockCredentialOffer.credentialOffer
import ch.admin.foitt.wallet.feature.credentialOffer.mock.MockCredentialOffer.credentialOffer2
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.FetchAndCacheIssuerDisplayData
import ch.admin.foitt.wallet.platform.credential.domain.model.CredentialError
import ch.admin.foitt.wallet.platform.credential.domain.usecase.MapToCredentialDisplayData
import ch.admin.foitt.wallet.platform.database.domain.model.Credential
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimWithDisplays
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialWithDisplaysAndClaims
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialWithDisplaysAndClaimsRepository
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.MapToCredentialClaimData
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.runs
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

class GetCredentialOfferFlowImplTest {

    @MockK
    lateinit var mockCredentialWithDisplaysAndClaimsRepository: CredentialWithDisplaysAndClaimsRepository

    @MockK
    lateinit var mockMapToCredentialDisplayData: MapToCredentialDisplayData

    @MockK
    lateinit var mockMapToCredentialClaimData: MapToCredentialClaimData

    @MockK
    lateinit var mockFetchAndCacheIssuerDisplayData: FetchAndCacheIssuerDisplayData

    @MockK
    lateinit var mockCredential: Credential

    @MockK
    lateinit var mockCredentialWithDisplaysAndClaims: CredentialWithDisplaysAndClaims

    private lateinit var getCredentialOfferFlow: GetCredentialOfferFlow

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        getCredentialOfferFlow = GetCredentialOfferFlowImpl(
            mockCredentialWithDisplaysAndClaimsRepository,
            mockMapToCredentialDisplayData,
            mockMapToCredentialClaimData,
            mockFetchAndCacheIssuerDisplayData,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Getting the credential offer returns a result with one credential offer`() = runTest {
        val result = getCredentialOfferFlow(CREDENTIAL_ID).firstOrNull()

        assertNotNull(result)
        result?.assertOk()

        val expected = CredentialOffer(
            credential = credentialOffer.credential,
            claims = credentialOffer.claims,
        )

        assertEquals(expected, result?.value)
    }

    @Test
    fun `Getting the credential offer flow with updates returns a flow with the credential offer`() = runTest {
        coEvery {
            mockCredentialWithDisplaysAndClaimsRepository.getNullableCredentialWithDisplaysAndClaimsFlowById(CREDENTIAL_ID)
        } returns flow {
            emit(Ok(mockCredentialWithDisplaysAndClaims))
            emit(Ok(mockCredentialWithDisplaysAndClaims))
        }

        val result = getCredentialOfferFlow(CREDENTIAL_ID).toList()
        assertEquals(2, result.size)
        result[0].assertOk()
        result[1].assertOk()
        assertEquals(credentialOffer, result[0].value)
        assertEquals(credentialOffer2, result[1].value)
    }

    @Test
    fun `Getting the credential offer flow maps errors from the repository`() = runTest {
        val exception = IllegalStateException("db error")
        coEvery {
            mockCredentialWithDisplaysAndClaimsRepository.getNullableCredentialWithDisplaysAndClaimsFlowById(CREDENTIAL_ID)
        } returns flowOf(Err(SsiError.Unexpected(exception)))

        val result = getCredentialOfferFlow(CREDENTIAL_ID).firstOrNull()

        assertNotNull(result)
        val error = result?.assertErrorType(CredentialOfferError.Unexpected::class)
        assertEquals(exception, error?.throwable)
    }

    @Test
    fun `Getting the credential offer flow maps error from MapToCredentialDisplayData use case`() = runTest {
        val exception = IllegalStateException("map to credential claim display data error")
        coEvery {
            mockMapToCredentialDisplayData(mockCredential, credentialDisplays)
        } returns Err(CredentialError.Unexpected(exception))

        val result = getCredentialOfferFlow(CREDENTIAL_ID).firstOrNull()

        assertNotNull(result)
        val error = result?.assertErrorType(CredentialOfferError.Unexpected::class)
        assertEquals(exception, error?.throwable)
    }

    @Test
    fun `Getting the credential offer flow maps errors from the MapToCredentialClaimData use case`() = runTest {
        val exception = IllegalStateException("no claim displays found")
        coEvery {
            mockMapToCredentialClaimData(any<CredentialClaimWithDisplays>())
        } returns Err(SsiError.Unexpected(exception))

        val result = getCredentialOfferFlow(CREDENTIAL_ID).firstOrNull()

        assertNotNull(result)
        val error = result?.assertErrorType(CredentialOfferError.Unexpected::class)
        assertEquals(exception, error?.throwable)
    }

    private fun setupDefaultMocks() {
        every { mockCredential.issuer } returns ISSUER

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
        coEvery { mockFetchAndCacheIssuerDisplayData(CREDENTIAL_ID, ISSUER) } just runs
    }
}

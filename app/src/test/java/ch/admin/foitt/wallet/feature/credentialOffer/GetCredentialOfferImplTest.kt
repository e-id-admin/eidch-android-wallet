package ch.admin.foitt.wallet.feature.credentialOffer

import ch.admin.foitt.wallet.feature.credentialOffer.domain.model.CredentialOfferError
import ch.admin.foitt.wallet.feature.credentialOffer.domain.usecase.GetCredentialOffer
import ch.admin.foitt.wallet.feature.credentialOffer.domain.usecase.implementation.GetCredentialOfferImpl
import ch.admin.foitt.wallet.feature.credentialOffer.mock.MockCredentialOffer.CREDENTIAL_ID
import ch.admin.foitt.wallet.feature.credentialOffer.mock.MockCredentialOffer.claimData1
import ch.admin.foitt.wallet.feature.credentialOffer.mock.MockCredentialOffer.claimData2
import ch.admin.foitt.wallet.feature.credentialOffer.mock.MockCredentialOffer.credentialDisplay1
import ch.admin.foitt.wallet.feature.credentialOffer.mock.MockCredentialOffer.credentialWithDetails
import ch.admin.foitt.wallet.feature.credentialOffer.mock.MockCredentialOffer.issuerDisplay1
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorField
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorType
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.FetchIssuerDisplayData
import ch.admin.foitt.wallet.platform.credential.domain.usecase.implementation.IsCredentialFromBetaIssuerImpl
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaim
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialClaimDisplay
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedDisplay
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialOfferRepository
import ch.admin.foitt.wallet.platform.ssi.domain.usecase.MapToCredentialClaimData
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
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
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetCredentialOfferImplTest {

    @MockK
    lateinit var mockCredentialOfferRepository: CredentialOfferRepository

    @MockK
    lateinit var mockGetLocalizedDisplay: GetLocalizedDisplay

    @MockK
    lateinit var mockMapToCredentialClaimData: MapToCredentialClaimData

    @MockK
    lateinit var mockIsCredentialFromBetaIssuerImpl: IsCredentialFromBetaIssuerImpl

    @MockK
    lateinit var mockFetchIssuerDisplayData: FetchIssuerDisplayData

    private lateinit var getCredentialOffer: GetCredentialOffer

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        getCredentialOffer = GetCredentialOfferImpl(
            credentialOfferRepository = mockCredentialOfferRepository,
            getLocalizedDisplay = mockGetLocalizedDisplay,
            mapToCredentialClaimData = mockMapToCredentialClaimData,
            fetchIssuerDisplayData = mockFetchIssuerDisplayData,
            isCredentialFromBetaIssuer = mockIsCredentialFromBetaIssuerImpl,
        )

        setupDefaultMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Getting the credential offer returns a result with one credential offer`() = runTest {
        val result = getCredentialOffer(CREDENTIAL_ID).firstOrNull()

        assertNotNull(result)
        result?.assertOk()
    }

    @Test
    fun `Getting the credential offer maps errors from the repository`() = runTest {
        val exception = IllegalStateException("db error")
        coEvery {
            mockCredentialOfferRepository.getCredentialOfferByIdFlow(any())
        } returns flow { emit(Err(SsiError.Unexpected(exception))) }

        val result = getCredentialOffer(CREDENTIAL_ID).firstOrNull()

        assertNotNull(result)
        result?.assertErrorType(CredentialOfferError.Unexpected::class)
        val error = result?.getError() as CredentialOfferError.Unexpected
        Assertions.assertEquals(exception, error.throwable)
    }

    @Test
    fun `Getting the credential offer maps errors from the GetLocalizedDisplay use case`() = runTest {
        coEvery { mockGetLocalizedDisplay(credentialWithDetails.credentialDisplays) } returns null

        val result = getCredentialOffer(CREDENTIAL_ID).firstOrNull()

        assertNotNull(result)
        result?.assertErrorType(CredentialOfferError.Unexpected::class)
    }

    @Test
    fun `Getting the credential offer maps errors from the MapToCredentialClaim use case`() = runTest {
        val exception = IllegalStateException("no credential displays found")
        coEvery {
            mockMapToCredentialClaimData(any<CredentialClaim>(), any<List<CredentialClaimDisplay>>())
        } returns Err(SsiError.Unexpected(exception))

        val result = getCredentialOffer(CREDENTIAL_ID).firstOrNull()

        assertNotNull(result)
        result?.assertErrorType(CredentialOfferError.Unexpected::class)
        val error = result?.getError() as CredentialOfferError.Unexpected
        Assertions.assertEquals(exception, error.throwable)
    }

    private fun setupDefaultMocks() {
        coEvery {
            mockCredentialOfferRepository.getCredentialOfferByIdFlow(CREDENTIAL_ID)
        } returns flow { emit(Ok(credentialWithDetails)) }
        coEvery { mockGetLocalizedDisplay(credentialWithDetails.issuerDisplays) } returns issuerDisplay1
        coEvery { mockGetLocalizedDisplay(credentialWithDetails.credentialDisplays) } returns credentialDisplay1
        coEvery {
            mockMapToCredentialClaimData(any<CredentialClaim>(), any<List<CredentialClaimDisplay>>())
        } returnsMany listOf(Ok(claimData1), Ok(claimData2))

        coEvery { mockFetchIssuerDisplayData.invoke(credentialId = any()) } returns mockIssuerDisplayData
        coEvery { mockIsCredentialFromBetaIssuerImpl(any()) } returns false
    }

    private val mockIssuerDisplayData = ActorDisplayData(
        name = listOf(
            ActorField(value = "a", "de"),
            ActorField(value = "b", "en"),
        ),
        image = null,
        preferredLanguage = "de",
        trustStatus = TrustStatus.TRUSTED,
        actorType = ActorType.ISSUER,
    )
}

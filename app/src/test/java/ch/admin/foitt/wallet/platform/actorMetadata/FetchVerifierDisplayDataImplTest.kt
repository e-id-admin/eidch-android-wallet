package ch.admin.foitt.wallet.platform.actorMetadata

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.ClientMetaData
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.ClientName
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.LogoUri
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequest
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorField
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorType
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.FetchAndCacheVerifierDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.InitializeActorForScope
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.implementation.FetchAndCacheVerifierDisplayDataImpl
import ch.admin.foitt.wallet.platform.navigation.domain.model.ComponentScope
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustRegistryError
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatement
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.usecase.FetchTrustStatementFromDid
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.impl.annotations.MockK
import io.mockk.just
import io.mockk.runs
import io.mockk.slot
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class FetchVerifierDisplayDataImplTest {

    @MockK
    private lateinit var mockFetchTrustStatementFromDid: FetchTrustStatementFromDid

    @MockK
    private lateinit var mockPresentationRequest: PresentationRequest

    @MockK
    private lateinit var mockTrustStatement01: TrustStatement

    @MockK
    private lateinit var mockInitializeActorForScope: InitializeActorForScope

    private lateinit var useCase: FetchAndCacheVerifierDisplayData

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        useCase = FetchAndCacheVerifierDisplayDataImpl(
            fetchTrustStatementFromDid = mockFetchTrustStatementFromDid,
            initializeActorForScope = mockInitializeActorForScope,
        )

        coEvery { mockTrustStatement01.orgName } returns mockTrustedNames
        coEvery { mockTrustStatement01.prefLang } returns mockPreferredLanguage

        coEvery { mockFetchTrustStatementFromDid.invoke(did = any()) } returns Ok(mockTrustStatement01)
        coEvery {
            mockInitializeActorForScope.invoke(any(), componentScope = ComponentScope.Verifier)
        } just runs
        defaultPresentationRequestMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Fetching the verifier display data is following specific steps`(): Unit = runTest {
        useCase(
            presentationRequest = mockPresentationRequest,
            shouldFetchTrustStatement = true,
        )

        coVerifyOrder {
            mockFetchTrustStatementFromDid.invoke(did = any())
            mockInitializeActorForScope.invoke(any(), componentScope = ComponentScope.Verifier)
        }
    }

    @Test
    fun `A trust statement is fetched using the presentation request client id`(): Unit = runTest {
        useCase(
            presentationRequest = mockPresentationRequest,
            shouldFetchTrustStatement = true,
        )

        coVerifyOrder {
            mockFetchTrustStatementFromDid.invoke(did = clientId)
        }
    }

    @Test
    fun `A valid trust statement will display as trusted`(): Unit = runTest {
        useCase(
            presentationRequest = mockPresentationRequest,
            shouldFetchTrustStatement = true,
        )

        val capturedDisplayData = slot<ActorDisplayData>()
        coVerifyOrder {
            mockInitializeActorForScope.invoke(actorDisplayData = capture(capturedDisplayData), any())
        }

        assertEquals(TrustStatus.TRUSTED, capturedDisplayData.captured.trustStatus)
    }

    @Test
    fun `An invalid trust statement will display as not trusted`(): Unit = runTest {
        coEvery { mockFetchTrustStatementFromDid.invoke(did = any()) } returns trustRegistryError
        useCase(
            presentationRequest = mockPresentationRequest,
            shouldFetchTrustStatement = true,
        )

        val capturedDisplayData = slot<ActorDisplayData>()
        coVerifyOrder {
            mockInitializeActorForScope.invoke(actorDisplayData = capture(capturedDisplayData), any())
        }

        assertEquals(TrustStatus.NOT_TRUSTED, capturedDisplayData.captured.trustStatus)
    }

    @Test
    fun `The trust statement is not fetched if the parameter is set to false`(): Unit = runTest {
        useCase(
            presentationRequest = mockPresentationRequest,
            shouldFetchTrustStatement = false,
        )

        coVerify(exactly = 0) {
            mockFetchTrustStatementFromDid.invoke(did = any())
        }
    }

    @Test
    fun `Valid trust statement data is shown first`(): Unit = runTest {
        useCase(
            presentationRequest = mockPresentationRequest,
            shouldFetchTrustStatement = true,
        )

        val capturedDisplayData = slot<ActorDisplayData>()
        coVerifyOrder {
            mockInitializeActorForScope.invoke(actorDisplayData = capture(capturedDisplayData), any())
        }

        assertEquals(mockTrustedNamesDisplay, capturedDisplayData.captured.name)
        // logo of the trust statement is ignored for now -> metadata logo is used instead
        assertEquals(mockMetadataLogoDisplays, capturedDisplayData.captured.image)
    }

    @Test
    fun `In case of invalid trust statement, falls back to the presentation request metadata`(): Unit = runTest {
        coEvery { mockFetchTrustStatementFromDid.invoke(did = any()) } returns trustRegistryError
        useCase(
            presentationRequest = mockPresentationRequest,
            shouldFetchTrustStatement = true,
        )

        val capturedDisplayData = slot<ActorDisplayData>()
        coVerifyOrder {
            mockInitializeActorForScope.invoke(actorDisplayData = capture(capturedDisplayData), any())
        }

        assertEquals(mockMetadataNameDisplays, capturedDisplayData.captured.name)
        assertEquals(mockMetadataLogoDisplays, capturedDisplayData.captured.image)
    }

    @Test
    fun `Missing both client metadata and trust statement leads to empty display data`(): Unit = runTest {
        coEvery { mockFetchTrustStatementFromDid.invoke(did = any()) } returns trustRegistryError
        coEvery { mockPresentationRequest.clientMetaData } returns null

        useCase(
            presentationRequest = mockPresentationRequest,
            shouldFetchTrustStatement = true,
        )

        val capturedDisplayData = slot<ActorDisplayData>()
        coVerifyOrder {
            mockInitializeActorForScope.invoke(actorDisplayData = capture(capturedDisplayData), any())
        }

        assertEquals(emptyActorDisplayData, capturedDisplayData.captured)
    }

    private fun defaultPresentationRequestMocks() {
        coEvery { mockPresentationRequest.clientId } returns clientId
        coEvery { mockPresentationRequest.clientMetaData } returns mockClientMetadata
    }
    //region mock data

    private val clientId = "clientId1"

    private val trustRegistryError = Err(TrustRegistryError.Unexpected(IllegalStateException("error")))

    private val mockPreferredLanguage = "en-us"

    private val mockTrustedNames = mapOf(
        "de-de" to "name DeDe",
        "en-gb" to "name EnGb"
    )

    private val mockTrustedNamesDisplay = mockTrustedNames.entries.map { entry ->
        ActorField(value = entry.value, locale = entry.key)
    }

    private val mockClientMetadata = ClientMetaData(
        clientNameList = listOf(
            ClientName("clientName En", locale = "en-gb")
        ),
        logoUriList = listOf(
            LogoUri("logoUri De", locale = "de-de")
        ),
    )

    private val mockMetadataNameDisplays = mockClientMetadata.clientNameList.map { entry ->
        ActorField(
            value = entry.clientName,
            locale = entry.locale,
        )
    }
    private val mockMetadataLogoDisplays = mockClientMetadata.logoUriList.map { entry ->
        ActorField(
            value = entry.logoUri,
            locale = entry.locale,
        )
    }

    private val emptyActorDisplayData = ActorDisplayData(
        name = null,
        image = null,
        preferredLanguage = null,
        trustStatus = TrustStatus.NOT_TRUSTED,
        actorType = ActorType.VERIFIER,
    )
    //endregion
}

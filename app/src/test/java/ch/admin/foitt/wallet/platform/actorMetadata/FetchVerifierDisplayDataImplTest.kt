package ch.admin.foitt.wallet.platform.actorMetadata

import ch.admin.foitt.openid4vc.domain.model.presentationRequest.ClientMetaData
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.ClientName
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.JsonPresentationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.JwtPresentationRequest
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.LogoUri
import ch.admin.foitt.openid4vc.domain.model.presentationRequest.PresentationRequest
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorField
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.FetchVerifierDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.implementation.FetchVerifierDisplayDataImpl
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
    private lateinit var mockPresentationRequestJwt: JwtPresentationRequest

    @MockK
    private lateinit var mockPresentationRequestJson: JsonPresentationRequest

    @MockK
    private lateinit var mockTrustStatement01: TrustStatement

    private lateinit var useCase: FetchVerifierDisplayData

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        useCase = FetchVerifierDisplayDataImpl(
            fetchTrustStatementFromDid = mockFetchTrustStatementFromDid,
        )

        coEvery { mockTrustStatement01.logoUri } returns mockTrustedLogos
        coEvery { mockTrustStatement01.orgName } returns mockTrustedNames
        coEvery { mockTrustStatement01.prefLang } returns mockPreferredLanguage

        coEvery { mockFetchTrustStatementFromDid.invoke(did = any()) } returns Ok(mockTrustStatement01)
        defaultPresentationRequestMocks()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `A trust statement is fetched using the presentation request client id`(): Unit = runTest {
        useCase(mockPresentationRequestJwt)

        coVerifyOrder {
            mockFetchTrustStatementFromDid.invoke(did = clientId)
        }
    }

    @Test
    fun `A valid trust statement will display as trusted`(): Unit = runTest {
        val displayData: ActorDisplayData = useCase(mockPresentationRequestJwt)

        assertEquals(TrustStatus.TRUSTED, displayData.trustStatus)
    }

    @Test
    fun `An invalid trust statement will display as not trusted`(): Unit = runTest {
        coEvery { mockFetchTrustStatementFromDid.invoke(did = any()) } returns trustRegistryError
        val displayData: ActorDisplayData = useCase(mockPresentationRequestJwt)

        assertEquals(TrustStatus.NOT_TRUSTED, displayData.trustStatus)
    }

    @Test
    fun `The trust statement is not fetched if the presentation request is not a jwt`(): Unit = runTest {
        useCase(mockPresentationRequestJson)

        coVerify(exactly = 0) {
            mockFetchTrustStatementFromDid.invoke(did = any())
        }
    }

    @Test
    fun `The trust statement is not fetched if cliendIdScheme is not a Did`(): Unit = runTest {
        coEvery { mockPresentationRequestJwt.clientIdScheme } returns "clientIdScheme"
        useCase(mockPresentationRequestJwt)

        coVerify(exactly = 0) {
            mockFetchTrustStatementFromDid.invoke(did = any())
        }
    }

    @Test
    fun `Valid trust statement data is shown first`(): Unit = runTest {
        val displayData: ActorDisplayData = useCase(mockPresentationRequestJwt)

        assertEquals(mockTrustedNamesDisplay, displayData.name)
        assertEquals(mockTrustedLogosDisplay, displayData.image)
    }

    @Test
    fun `In case of invalid trust statement, falls back to the presentation request metadata`(): Unit = runTest {
        coEvery { mockFetchTrustStatementFromDid.invoke(did = any()) } returns trustRegistryError
        val displayData: ActorDisplayData = useCase(mockPresentationRequestJwt)

        assertEquals(mockMetadataNameDisplays, displayData.name)
        assertEquals(mockMetadataLogoDisplays, displayData.image)
    }

    @Test
    fun `Missing both client metadata and trust statement leads to empty display data`(): Unit = runTest {
        coEvery { mockFetchTrustStatementFromDid.invoke(did = any()) } returns trustRegistryError
        coEvery { mockPresentationRequestJwt.clientMetaData } returns null

        val displayData: ActorDisplayData = useCase(mockPresentationRequestJwt)

        assertEquals(emptyActorDisplayData, displayData)
    }

    fun defaultPresentationRequestMocks() {
        coEvery { mockPresentationRequestJwt.clientId } returns clientId
        coEvery { mockPresentationRequestJwt.clientMetaData } returns mockClientMetatdata
        coEvery { mockPresentationRequestJwt.clientIdScheme } returns PresentationRequest.DID

        coEvery { mockPresentationRequestJson.clientId } returns clientId
        coEvery { mockPresentationRequestJson.clientMetaData } returns mockClientMetatdata
        coEvery { mockPresentationRequestJson.clientIdScheme } returns PresentationRequest.DID
    }

    //region mock data

    private val clientId = "clientId1"

    private val trustRegistryError = Err(TrustRegistryError.Unexpected(IllegalStateException("error")))

    private val mockPreferredLanguage = "en-us"

    private val mockTrustedLogos = mapOf(
        "en-us" to "logo EnUs",
        "de-de" to "logo DeDe",
    )

    private val mockTrustedNames = mapOf(
        "de-de" to "name DeDe",
        "en-gb" to "name EnGb"
    )

    private val mockTrustedNamesDisplay = mockTrustedNames.entries.map { entry ->
        ActorField(value = entry.value, locale = entry.key)
    }

    private val mockTrustedLogosDisplay = mockTrustedLogos.entries.map { entry ->
        ActorField(value = entry.value, locale = entry.key)
    }

    private val mockClientMetatdata = ClientMetaData(
        clientNameList = listOf(
            ClientName("clientName En", locale = "en-gb")
        ),
        logoUriList = listOf(
            LogoUri("logoUri De", locale = "de-de")
        ),
    )

    private val mockMetadataNameDisplays = mockClientMetatdata.clientNameList.map { entry ->
        ActorField(
            value = entry.clientName,
            locale = entry.locale,
        )
    }
    private val mockMetadataLogoDisplays = mockClientMetatdata.logoUriList.map { entry ->
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
    )
    //endregion
}

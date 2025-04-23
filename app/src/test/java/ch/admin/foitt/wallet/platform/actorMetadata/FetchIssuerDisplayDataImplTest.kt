package ch.admin.foitt.wallet.platform.actorMetadata

import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorField
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorType
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.FetchAndCacheIssuerDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.InitializeActorForScope
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.implementation.FetchAndCacheIssuerDisplayDataImpl
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialIssuerDisplay
import ch.admin.foitt.wallet.platform.navigation.domain.model.ComponentScope
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.CredentialIssuerDisplayRepo
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

class FetchIssuerDisplayDataImplTest {

    @MockK
    private lateinit var mockCredentialIssuerDisplayRepo: CredentialIssuerDisplayRepo

    @MockK
    private lateinit var mockFetchTrustStatementFromDid: FetchTrustStatementFromDid

    @MockK
    private lateinit var mockTrustStatement01: TrustStatement

    @MockK
    private lateinit var mockInitializeActorForScope: InitializeActorForScope

    private lateinit var useCase: FetchAndCacheIssuerDisplayData

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        useCase = FetchAndCacheIssuerDisplayDataImpl(
            credentialIssuerDisplayRepo = mockCredentialIssuerDisplayRepo,
            fetchTrustStatementFromDid = mockFetchTrustStatementFromDid,
            initializeActorForScope = mockInitializeActorForScope,
        )

        coEvery { mockTrustStatement01.orgName } returns mockTrustedNames
        coEvery { mockTrustStatement01.prefLang } returns mockPreferredLanguage

        coEvery { mockFetchTrustStatementFromDid.invoke(did = any()) } returns Ok(mockTrustStatement01)

        coEvery {
            mockCredentialIssuerDisplayRepo.getIssuerDisplays(any())
        } returns Ok(listOf(credentialIssuerDisplay01, credentialIssuerDisplay02))

        coEvery {
            mockInitializeActorForScope.invoke(any(), componentScope = ComponentScope.CredentialIssuer)
        } just runs
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Fetching the issuer display data is following specific steps`(): Unit = runTest {
        useCase(credentialId01, mockDid)

        coVerifyOrder {
            mockCredentialIssuerDisplayRepo.getIssuerDisplays(any())
            mockFetchTrustStatementFromDid.invoke(did = any())
            mockInitializeActorForScope.invoke(any(), componentScope = ComponentScope.CredentialIssuer)
        }
    }

    @Test
    fun `A trust statement is fetched using the credential issuer id`(): Unit = runTest {
        useCase(credentialId01, mockDid)

        coVerifyOrder {
            mockFetchTrustStatementFromDid.invoke(did = mockDid)
        }
    }

    @Test
    fun `A valid trust statement will display as trusted`(): Unit = runTest {
        useCase(credentialId01, mockDid)

        val capturedDisplayData = slot<ActorDisplayData>()
        coVerifyOrder {
            mockInitializeActorForScope.invoke(actorDisplayData = capture(capturedDisplayData), any())
        }

        assertEquals(TrustStatus.TRUSTED, capturedDisplayData.captured.trustStatus)
    }

    @Test
    fun `No trust statement is fetched when issuer is not a did`(): Unit = runTest {
        useCase(credentialId01, "not a did")

        val capturedDisplayData = slot<ActorDisplayData>()
        coVerifyOrder {
            mockInitializeActorForScope.invoke(actorDisplayData = capture(capturedDisplayData), any())
        }

        assertEquals(TrustStatus.NOT_TRUSTED, capturedDisplayData.captured.trustStatus)

        coVerify(exactly = 0) {
            mockFetchTrustStatementFromDid.invoke(did = "not a did")
        }
    }

    @Test
    fun `An invalid trust statement will display as not trusted`(): Unit = runTest {
        coEvery { mockFetchTrustStatementFromDid.invoke(did = any()) } returns trustRegistryError
        useCase(credentialId01, mockDid)
        val capturedDisplayData = slot<ActorDisplayData>()
        coVerifyOrder {
            mockInitializeActorForScope.invoke(actorDisplayData = capture(capturedDisplayData), any())
        }

        assertEquals(TrustStatus.NOT_TRUSTED, capturedDisplayData.captured.trustStatus)
    }

    @Test
    fun `Valid trust statement data is shown first`(): Unit = runTest {
        useCase(credentialId01, mockDid)

        val capturedDisplayData = slot<ActorDisplayData>()
        coVerifyOrder {
            mockInitializeActorForScope.invoke(actorDisplayData = capture(capturedDisplayData), any())
        }

        assertEquals(mockTrustedNamesDisplay, capturedDisplayData.captured.name)
        // logo of the trust statement is ignored for now -> metadata logo is used instead
        assertEquals(mockMetadataLogoDisplays, capturedDisplayData.captured.image)
    }

    @Test
    fun `In case of invalid trust statement, falls back to the credential issuer metadata`(): Unit = runTest {
        coEvery { mockFetchTrustStatementFromDid.invoke(did = any()) } returns trustRegistryError
        useCase(credentialId01, mockDid)

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
        coEvery { mockCredentialIssuerDisplayRepo.getIssuerDisplays(any()) } returns credentialIssuerDisplayError

        useCase(credentialId01, mockDid)

        val capturedDisplayData = slot<ActorDisplayData>()
        coVerifyOrder {
            mockInitializeActorForScope.invoke(actorDisplayData = capture(capturedDisplayData), any())
        }

        assertEquals(emptyActorDisplayData, capturedDisplayData.captured)
    }

    //region mock data

    private val trustRegistryError = Err(TrustRegistryError.Unexpected(IllegalStateException("trustError")))
    private val credentialIssuerDisplayError = Err(SsiError.Unexpected(IllegalStateException("displayError")))

    private val mockPreferredLanguage = "en-us"

    private val mockTrustedNames = mapOf(
        "de-de" to "name DeDe",
        "en-gb" to "name EnGb"
    )

    private val mockTrustedNamesDisplay = mockTrustedNames.entries.map { entry ->
        ActorField(value = entry.value, locale = entry.key)
    }

    private val emptyActorDisplayData = ActorDisplayData(
        name = null,
        image = null,
        preferredLanguage = null,
        trustStatus = TrustStatus.NOT_TRUSTED,
        actorType = ActorType.ISSUER,
    )

    private val credentialId01 = 1L

    private val credentialIssuerDisplay01 = CredentialIssuerDisplay(
        credentialId = credentialId01,
        name = "credentialIssuer01",
        image = "credentialImage01",
        imageAltText = null,
        locale = "en-us",
    )

    private val credentialIssuerDisplay02 = credentialIssuerDisplay01.copy(
        credentialId = credentialId01,
        name = "credentialIssuer02",
        image = "credentialImage02",
        imageAltText = null,
        locale = "de-de",
    )

    private val credentialIssuerDisplays = listOf(
        credentialIssuerDisplay01,
        credentialIssuerDisplay02,
    )

    private val mockMetadataNameDisplays = credentialIssuerDisplays.map { entry ->
        ActorField(
            value = entry.name,
            locale = entry.locale,
        )
    }

    private val mockMetadataLogoDisplays = credentialIssuerDisplays.mapNotNull { entry ->
        entry.image?.let {
            ActorField(
                value = entry.image,
                locale = entry.locale,
            )
        }
    }

    private val mockDid = "did:tdw:identifier"
    //endregion
}

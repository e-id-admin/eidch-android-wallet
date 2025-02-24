package ch.admin.foitt.wallet.platform.actorMetadata

import ch.admin.foitt.openid4vc.domain.model.anycredential.AnyCredential
import ch.admin.foitt.openid4vc.domain.model.credentialoffer.metadata.CredentialFormat
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorField
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.FetchIssuerDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.domain.usecase.implementation.FetchIssuerDisplayDataImpl
import ch.admin.foitt.wallet.platform.credential.domain.usecase.GetAnyCredential
import ch.admin.foitt.wallet.platform.database.domain.model.CredentialIssuerDisplay
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
    private lateinit var mockGetAnyCredential: GetAnyCredential

    @MockK
    private lateinit var mockTrustStatement01: TrustStatement

    @MockK
    private lateinit var mockAnyCredential: AnyCredential

    private lateinit var useCase: FetchIssuerDisplayData

    @BeforeEach
    fun setup() {
        MockKAnnotations.init(this)
        useCase = FetchIssuerDisplayDataImpl(
            credentialIssuerDisplayRepo = mockCredentialIssuerDisplayRepo,
            fetchTrustStatementFromDid = mockFetchTrustStatementFromDid,
            getAnyCredential = mockGetAnyCredential,
        )

        coEvery { mockAnyCredential.payload } returns mockPayload
        coEvery { mockAnyCredential.format } returns CredentialFormat.VC_SD_JWT
        coEvery { mockAnyCredential.issuer } returns mockDid

        coEvery { mockTrustStatement01.logoUri } returns mockTrustedLogos
        coEvery { mockTrustStatement01.orgName } returns mockTrustedNames
        coEvery { mockTrustStatement01.prefLang } returns mockPreferredLanguage

        coEvery { mockGetAnyCredential.invoke(credentialId = credentialId01) } returns Ok(mockAnyCredential)

        coEvery { mockFetchTrustStatementFromDid.invoke(did = any()) } returns Ok(mockTrustStatement01)

        coEvery {
            mockCredentialIssuerDisplayRepo.getIssuerDisplays(any())
        } returns Ok(listOf(credentialIssuerDisplay01, credentialIssuerDisplay02))
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `A trust statement is following specific steps`(): Unit = runTest {
        useCase(credentialId01)

        coVerifyOrder {
            mockGetAnyCredential.invoke(credentialId = any())
            mockCredentialIssuerDisplayRepo.getIssuerDisplays(any())
            mockFetchTrustStatementFromDid.invoke(did = any())
        }
    }

    @Test
    fun `A trust statement is fetched using the credential issuer id`(): Unit = runTest {
        useCase(credentialId01)

        coVerifyOrder {
            mockFetchTrustStatementFromDid.invoke(did = mockDid)
        }
    }

    @Test
    fun `A valid trust statement will display as trusted`(): Unit = runTest {
        val displayData: ActorDisplayData = useCase(credentialId01)

        assertEquals(TrustStatus.TRUSTED, displayData.trustStatus)
    }

    @Test
    fun `No trust statement is fetched when issuer is not a did`(): Unit = runTest {
        coEvery { mockAnyCredential.issuer } returns "not a did"
        val displayData: ActorDisplayData = useCase(credentialId01)

        assertEquals(TrustStatus.NOT_TRUSTED, displayData.trustStatus)

        coVerify(exactly = 0) {
            mockFetchTrustStatementFromDid.invoke(did = "not a did")
        }
    }

    @Test
    fun `An invalid trust statement will display as not trusted`(): Unit = runTest {
        coEvery { mockFetchTrustStatementFromDid.invoke(did = any()) } returns trustRegistryError
        val displayData: ActorDisplayData = useCase(credentialId01)

        assertEquals(TrustStatus.NOT_TRUSTED, displayData.trustStatus)
    }

    @Test
    fun `Valid trust statement data is shown first`(): Unit = runTest {
        val displayData: ActorDisplayData = useCase(credentialId01)

        assertEquals(mockTrustedNamesDisplay, displayData.name)
        assertEquals(mockTrustedLogosDisplay, displayData.image)
    }

    @Test
    fun `In case of invalid trust statement, falls back to the credential issuer metadata`(): Unit = runTest {
        coEvery { mockFetchTrustStatementFromDid.invoke(did = any()) } returns trustRegistryError
        val displayData: ActorDisplayData = useCase(credentialId01)

        assertEquals(mockMetadataNameDisplays, displayData.name)
        assertEquals(mockMetadataLogoDisplays, displayData.image)
    }

    @Test
    fun `Missing both client metadata and trust statement leads to empty display data`(): Unit = runTest {
        coEvery { mockFetchTrustStatementFromDid.invoke(did = any()) } returns trustRegistryError
        coEvery { mockCredentialIssuerDisplayRepo.getIssuerDisplays(any()) } returns credentialIssuerDisplayError

        val displayData: ActorDisplayData = useCase(credentialId01)

        assertEquals(emptyActorDisplayData, displayData)
    }

    //region mock data

    private val trustRegistryError = Err(TrustRegistryError.Unexpected(IllegalStateException("trustError")))
    private val credentialIssuerDisplayError = Err(SsiError.Unexpected(IllegalStateException("displayError")))

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

    private val emptyActorDisplayData = ActorDisplayData(
        name = null,
        image = null,
        preferredLanguage = null,
        trustStatus = TrustStatus.NOT_TRUSTED,
    )

    private val credentialId01 = 1L

    private val credentialIssuerDisplay01 = CredentialIssuerDisplay(
        credentialId = credentialId01,
        name = "credentialIssuer01",
        image = "crecentialImage01",
        imageAltText = null,
        locale = "en-us",
    )

    private val credentialIssuerDisplay02 = credentialIssuerDisplay01.copy(
        credentialId = credentialId01,
        name = "credentialIssuer02",
        image = "crecentialImage02",
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

    /*
    {
        "iss":"did:tdw:identifier"
    }
     */
    private val mockPayload =
        "ewogICJ0eXAiOiJ2YytzZC1qd3QiLAogICJhbGciOiJFUzI1NiIsCiAgImtpZCI6ImtleUlkIgp9.ewogICJpc3MiOiJkaWQ6dGR3OmlkZW50aWZpZXIiLAogICJ2Y3QiOiJ2Y3QiCn0.ZXdvZ0lDSjBlWEFpT2lKMll5dHpaQzFxZDNRaUxBb2dJQ0poYkdjaU9pSkZVekkxTmlJc0NpQWdJbXRwWkNJNkltdGxlVWxrSWdwOS4uNHNwTXBzWE1nYlNyY0lqMFdNbXJNYXdhcVRzeG9GWmItcjdwTWlubEhvZklRRUhhS2pzV1J0dENzUTkyd0tfa3RpaDQta2VCdjdVbkc2MkRPa2NDbGc"
    //endregion
}

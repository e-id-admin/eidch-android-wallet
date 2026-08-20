package ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.activityList.domain.model.ActivityType
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorField
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorMetadataDisplayData
import ch.admin.foitt.wallet.platform.actorMetadata.mock.ActorMetadataMocks.mockActorDisplayData01
import ch.admin.foitt.wallet.platform.database.domain.model.RawCredentialData
import ch.admin.foitt.wallet.platform.database.domain.model.VerifiableCredentialEntity
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedDisplay
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceError
import ch.admin.foitt.wallet.platform.nonCompliance.domain.model.NonComplianceReportingData
import ch.admin.foitt.wallet.platform.nonCompliance.domain.usecase.GetNonComplianceReportingData
import ch.admin.foitt.wallet.platform.ssi.domain.model.SsiError
import ch.admin.foitt.wallet.platform.ssi.domain.repository.RawCredentialDataRepository
import ch.admin.foitt.wallet.platform.ssi.domain.repository.VerifiableCredentialRepository
import ch.admin.foitt.wallet.platform.utils.compress
import ch.admin.foitt.wallet.platform.utils.domain.usecase.GetImageDataFromUri
import ch.admin.foitt.wallet.util.assertErrorType
import ch.admin.foitt.wallet.util.assertOk
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class GetNonComplianceReportingDataImplTest {

    @MockK
    private lateinit var mockRawCredentialDataRepository: RawCredentialDataRepository

    @MockK
    private lateinit var mockVerifiableCredentialRepository: VerifiableCredentialRepository

    @MockK
    private lateinit var mockGetImageDataFromUri: GetImageDataFromUri

    @MockK
    private lateinit var mockGetLocalizedDisplay: GetLocalizedDisplay

    @MockK
    private lateinit var mockRawCredentialData: RawCredentialData

    @MockK
    private lateinit var mockVerifiableCredential: VerifiableCredentialEntity

    private lateinit var useCase: GetNonComplianceReportingData

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        useCase = GetNonComplianceReportingDataImpl(
            rawCredentialDataRepository = mockRawCredentialDataRepository,
            verifiableCredentialRepository = mockVerifiableCredentialRepository,
            getImageDataFromUri = mockGetImageDataFromUri,
            getLocalizedDisplay = mockGetLocalizedDisplay,
        )
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Getting non compliance reporting data returns Ok`() = runTest {
        val actorDisplayData = mockActorDisplayData01
        val rawMetadata = "rawMetadata".toByteArray()
        val compressedMetadata = rawMetadata.compress()
        val issuerDid = "issuerDid"
        val localizedName = "localizedName"
        val iconUri = "iconUri"
        val iconBytes = "iconBytes".toByteArray()

        every { mockRawCredentialData.rawOIDMetadata } returns compressedMetadata
        coEvery { mockRawCredentialDataRepository.getByCredentialId(CREDENTIAL_ID) } returns Ok(mockRawCredentialData)

        every { mockVerifiableCredential.issuer } returns issuerDid
        coEvery { mockVerifiableCredentialRepository.getById(CREDENTIAL_ID) } returns Ok(mockVerifiableCredential)

        every {
            mockGetLocalizedDisplay(actorDisplayData.name!!, actorDisplayData.preferredLanguage)
        } returns ActorField(value = localizedName, locale = "de")

        every {
            mockGetLocalizedDisplay(actorDisplayData.image!!, actorDisplayData.preferredLanguage)
        } returns ActorField(value = iconUri, locale = "de")

        coEvery { mockGetImageDataFromUri(iconUri) } returns iconBytes

        val result = useCase(CREDENTIAL_ID, actorDisplayData, ACTIVITY_TYPE)

        val reportingData = result.assertOk()
        val expectedReportingData = NonComplianceReportingData(
            actorDisplayData = ActorMetadataDisplayData(
                activityId = null,
                localizedActorName = localizedName,
                actorImageData = iconBytes,
            ),
            rawData = "rawMetadata",
            issuerDid = issuerDid,
            activityType = ACTIVITY_TYPE,
        )
        assertEquals(expectedReportingData, reportingData)
    }

    @Test
    fun `Getting non compliance reporting data with missing fields returns empty values`() = runTest {
        val actorDisplayData = ActorDisplayData.EMPTY

        every { mockRawCredentialData.rawOIDMetadata } returns null
        coEvery { mockRawCredentialDataRepository.getByCredentialId(CREDENTIAL_ID) } returns Ok(mockRawCredentialData)

        every { mockVerifiableCredential.issuer } returns null
        coEvery { mockVerifiableCredentialRepository.getById(CREDENTIAL_ID) } returns Ok(mockVerifiableCredential)

        every {
            mockGetLocalizedDisplay(any<Collection<ActorField<String>>>(), any())
        } returns null

        val result = useCase(CREDENTIAL_ID, actorDisplayData, ACTIVITY_TYPE)

        val reportingData = result.assertOk()
        assertEquals("", reportingData.rawData)
        assertEquals("", reportingData.issuerDid)
        assertEquals("", reportingData.actorDisplayData.localizedActorName)
        assertEquals(null, reportingData.actorDisplayData.actorImageData)
    }

    @Test
    fun `Getting non compliance reporting data maps errors from raw credential repository`() = runTest {
        coEvery {
            mockRawCredentialDataRepository.getByCredentialId(CREDENTIAL_ID)
        } returns Err(SsiError.Unexpected(IllegalStateException("error")))

        useCase(CREDENTIAL_ID, mockActorDisplayData01, ACTIVITY_TYPE)
            .assertErrorType(NonComplianceError.Unexpected::class)
    }

    @Test
    fun `Getting non compliance reporting data maps errors from verifiable credential repository`() = runTest {
        every { mockRawCredentialData.rawOIDMetadata } returns null
        coEvery { mockRawCredentialDataRepository.getByCredentialId(CREDENTIAL_ID) } returns Ok(mockRawCredentialData)
        coEvery {
            mockVerifiableCredentialRepository.getById(CREDENTIAL_ID)
        } returns Err(SsiError.Unexpected(IllegalStateException("error")))

        useCase(CREDENTIAL_ID, mockActorDisplayData01, ACTIVITY_TYPE)
            .assertErrorType(NonComplianceError.Unexpected::class)
    }

    private companion object {
        const val CREDENTIAL_ID = 1L
        val ACTIVITY_TYPE = ActivityType.ISSUANCE
    }
}

package ch.admin.foitt.wallet.platform.versionEnforcement.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedDisplay
import ch.admin.foitt.wallet.platform.utils.AppVersion
import ch.admin.foitt.wallet.platform.utils.BuildConfigProvider
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.model.AppVersionInfo
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.model.VersionEnforcement
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.model.VersionEnforcementError
import ch.admin.foitt.wallet.platform.versionEnforcement.domain.repository.VersionEnforcementRepository
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
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class FetchAppVersionInfoImplTest {

    @MockK
    private lateinit var mockBuildConfigProvider: BuildConfigProvider

    @MockK
    private lateinit var mockVersionEnforcementRepository: VersionEnforcementRepository

    @MockK
    private lateinit var mockGetLocalizedDisplay: GetLocalizedDisplay

    @MockK
    private lateinit var mockVersionEnforcement: VersionEnforcement

    @MockK
    private lateinit var mockDisplays: List<VersionEnforcement.Display>

    private lateinit var useCase: FetchAppVersionInfoImpl

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)

        useCase = FetchAppVersionInfoImpl(
            buildConfigProvider = mockBuildConfigProvider,
            versionEnforcementRepository = mockVersionEnforcementRepository,
            getLocalizedDisplay = mockGetLocalizedDisplay
        )

        success()
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `Fetching app version info where version is lower than max app version and min app version is null returns blocked`() = runTest {
        mockVersions(
            minVersion = null,
            maxVersion = higherAppVersion,
            appVersion = middleAppVersion,
        )

        val info = useCase()

        assertTrue(info is AppVersionInfo.Blocked)
        info as AppVersionInfo.Blocked
        assertEquals(info.title, TITLE)
        assertEquals(info.text, TEXT)
    }

    @ParameterizedTest
    @ValueSource(strings = [MIDDLE_APP_VERSION, HIGHER_APP_VERSION])
    fun `Fetching app version info where version is equal or higher than max app version and min app version is null returns valid`(
        version: String
    ) = runTest {
        mockVersions(
            minVersion = null,
            maxVersion = middleAppVersion,
            appVersion = AppVersion(version),
        )

        val info = useCase()

        assertTrue(info is AppVersionInfo.Valid)
    }

    @Test
    fun `Fetching app version info where version is lower than min app version returns valid`() = runTest {
        mockVersions(
            minVersion = middleAppVersion,
            maxVersion = higherAppVersion,
            appVersion = lowerAppVersion,
        )

        val info = useCase()

        assertTrue(info is AppVersionInfo.Valid)
    }

    @ParameterizedTest
    @ValueSource(strings = [LOWER_APP_VERSION, MIDDLE_APP_VERSION])
    fun `Fetching app version info where version is equal or higher than min app version and lower than max app version returns blocked`(
        version: String
    ) = runTest {
        mockVersions(
            minVersion = lowerAppVersion,
            maxVersion = higherAppVersion,
            appVersion = AppVersion(version),
        )

        val info = useCase()

        assertTrue(info is AppVersionInfo.Blocked)
        info as AppVersionInfo.Blocked
        assertEquals(info.title, TITLE)
        assertEquals(info.text, TEXT)
    }

    @Test
    fun `Fetching app version info where version is higher than max app version returns valid`() =
        runTest {
            mockVersions(
                minVersion = lowerAppVersion,
                maxVersion = middleAppVersion,
                appVersion = higherAppVersion,
            )

            val info = useCase()

            assertTrue(info is AppVersionInfo.Valid)
        }

    @Test
    fun `Fetching app version info where version is blocked and no localized display is found returns blocked`() = runTest {
        coEvery { mockGetLocalizedDisplay(mockDisplays) } returns null

        val info = useCase()

        assertTrue(info is AppVersionInfo.Blocked)
        info as AppVersionInfo.Blocked
        assertEquals(info.title, null)
        assertEquals(info.text, null)
    }

    @Test
    fun `Fetching app version info where no info is available returns valid`() = runTest {
        coEvery { mockVersionEnforcementRepository.fetchLatestHighPriority() } returns Ok(null)

        val info = useCase()

        assertTrue(info is AppVersionInfo.Valid)
    }

    @Test
    fun `Fetching app version info where repository has an error returns unknown`() = runTest {
        coEvery {
            mockVersionEnforcementRepository.fetchLatestHighPriority()
        } returns Err(VersionEnforcementError.Unexpected(null))

        val info = useCase()

        assertTrue(info is AppVersionInfo.Unknown)
    }

    private fun success() {
        every { mockBuildConfigProvider.appVersion } returns middleAppVersion

        every { mockVersionEnforcement.displays } returns mockDisplays
        coEvery { mockGetLocalizedDisplay(mockDisplays) } returns display
        mockVersions(
            minVersion = null,
            maxVersion = higherAppVersion,
            appVersion = middleAppVersion,
        )

        coEvery {
            mockVersionEnforcementRepository.fetchLatestHighPriority()
        } returns Ok(mockVersionEnforcement)
    }

    private fun mockVersions(minVersion: AppVersion?, maxVersion: AppVersion, appVersion: AppVersion) {
        every { mockVersionEnforcement.criteria.minAppVersionIncluded } returns minVersion
        every { mockVersionEnforcement.criteria.maxAppVersionExcluded } returns maxVersion
        every { mockBuildConfigProvider.appVersion } returns appVersion
    }

    private companion object {
        const val TITLE = "title"
        const val TEXT = "text"
        const val LOWER_APP_VERSION = "1.0.0"
        const val MIDDLE_APP_VERSION = "1.1.0"
        const val HIGHER_APP_VERSION = "1.2.0"

        val middleAppVersion = AppVersion(MIDDLE_APP_VERSION)
        val lowerAppVersion = AppVersion(LOWER_APP_VERSION)
        val higherAppVersion = AppVersion(HIGHER_APP_VERSION)

        val display = VersionEnforcement.Display(
            title = TITLE,
            text = TEXT,
            locale = "locale"
        )
    }
}

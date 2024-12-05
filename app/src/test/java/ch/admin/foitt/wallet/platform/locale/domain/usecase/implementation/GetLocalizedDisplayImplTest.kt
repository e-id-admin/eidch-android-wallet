package ch.admin.foitt.wallet.platform.locale.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetCurrentAppLocale
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedDisplay
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.Locale

class GetLocalizedDisplayImplTest {

    @MockK
    private lateinit var mockGetCurrentAppLocale: GetCurrentAppLocale
    private lateinit var getLocalizedDisplay: GetLocalizedDisplay

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
        getLocalizedDisplay = GetLocalizedDisplayImpl(getCurrentAppLocale = mockGetCurrentAppLocale)
    }

    @AfterEach
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `should return LocalizedDisplay with best matching locale for app locale with supported country code`() = runTest {
        coEvery { mockGetCurrentAppLocale() } returns Locale("de", "CH")

        assertEquals("de-CH", getLocalizedDisplay(LocalizedDisplayTestData.withSupportedLocaleWithCountryCode)?.locale)
        assertEquals("en", getLocalizedDisplay(LocalizedDisplayTestData.withSupportedLocaleNoCountryCode)?.locale)
        assertEquals("xx", getLocalizedDisplay(LocalizedDisplayTestData.noSupportedLocaleAndNoFallback)?.locale)
        assertEquals("fallback", getLocalizedDisplay(LocalizedDisplayTestData.withFallbackAndNoSupportedLocale)?.locale)
    }

    @Test
    fun `should return LocalizedDisplay with best matching locale for app locale with unsupported country code`() = runTest {
        coEvery { mockGetCurrentAppLocale() } returns Locale("de", "XX")

        assertEquals("de-CH", getLocalizedDisplay(LocalizedDisplayTestData.withSupportedLocaleWithCountryCode)?.locale)
        assertEquals("en", getLocalizedDisplay(LocalizedDisplayTestData.withSupportedLocaleNoCountryCode)?.locale)
        assertEquals("xx", getLocalizedDisplay(LocalizedDisplayTestData.noSupportedLocaleAndNoFallback)?.locale)
        assertEquals("fallback", getLocalizedDisplay(LocalizedDisplayTestData.withFallbackAndNoSupportedLocale)?.locale)
    }

    @Test
    fun `should return LocalizedDisplay with best matching locale for app locale without country code`() = runTest {
        coEvery { mockGetCurrentAppLocale() } returns Locale("de")

        assertEquals("de-CH", getLocalizedDisplay(LocalizedDisplayTestData.withSupportedLocaleWithCountryCode)?.locale)
        assertEquals("en", getLocalizedDisplay(LocalizedDisplayTestData.withSupportedLocaleNoCountryCode)?.locale)
        assertEquals("xx", getLocalizedDisplay(LocalizedDisplayTestData.noSupportedLocaleAndNoFallback)?.locale)
        assertEquals("fallback", getLocalizedDisplay(LocalizedDisplayTestData.withFallbackAndNoSupportedLocale)?.locale)
    }

    @Test
    fun `should return same LocalizedDisplay for different Collection types as input`() = runTest {
        coEvery { mockGetCurrentAppLocale() } returns Locale("de", "CH")

        assertEquals(
            getLocalizedDisplay(LocalizedDisplayTestData.withSupportedLocaleWithCountryCode.toList())?.locale,
            getLocalizedDisplay(LocalizedDisplayTestData.withSupportedLocaleWithCountryCode.toSet())?.locale
        )
        assertEquals(
            getLocalizedDisplay(LocalizedDisplayTestData.withSupportedLocaleNoCountryCode.toList())?.locale,
            getLocalizedDisplay(LocalizedDisplayTestData.withSupportedLocaleNoCountryCode.toSet())?.locale
        )
        assertEquals(
            getLocalizedDisplay(LocalizedDisplayTestData.noSupportedLocaleAndNoFallback.toList())?.locale,
            getLocalizedDisplay(LocalizedDisplayTestData.noSupportedLocaleAndNoFallback.toSet())?.locale
        )
        assertEquals(
            getLocalizedDisplay(LocalizedDisplayTestData.withFallbackAndNoSupportedLocale.toList())?.locale,
            getLocalizedDisplay(LocalizedDisplayTestData.withFallbackAndNoSupportedLocale.toSet())?.locale
        )
    }

    @Test
    fun `should return null for empty input collection`() = runTest {
        coEvery { mockGetCurrentAppLocale() } returns Locale("de", "CH")

        assertNull(getLocalizedDisplay(emptyList()))
        assertNull(getLocalizedDisplay(emptySet()))
    }
}

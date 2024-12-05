package ch.admin.foitt.wallet.platform.locale.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.database.domain.model.LocalizedDisplay

// Test data
internal data class MockLocalizedDisplay(
    override val locale: String
) : LocalizedDisplay

internal object LocalizedDisplayTestData {
    val withSupportedLocaleWithCountryCode = listOf("en", "fr", "de-CH", "fallback").map {
        MockLocalizedDisplay(locale = it)
    }
    val withSupportedLocaleNoCountryCode = listOf("en", "it", "fallback", "yy").map { MockLocalizedDisplay(locale = it) }
    val noSupportedLocaleAndNoFallback = listOf("xx", "yy").map { MockLocalizedDisplay(locale = it) }
    val withFallbackAndNoSupportedLocale = listOf("yy", "fallback").map { MockLocalizedDisplay(locale = it) }
}

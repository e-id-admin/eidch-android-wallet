package ch.admin.foitt.wallet.platform.locale.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.database.domain.model.DisplayLanguage
import ch.admin.foitt.wallet.platform.database.domain.model.LocalizedDisplay
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetCurrentAppLocale
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedDisplay
import javax.inject.Inject

class GetLocalizedDisplayImpl @Inject constructor(
    private val getCurrentAppLocale: GetCurrentAppLocale
) : GetLocalizedDisplay {

    override fun <T : LocalizedDisplay> invoke(
        displays: Collection<T>,
        preferredLocale: String?,
    ): T? {
        val appLocale = getCurrentAppLocale()
        val language = appLocale.language // e.g. "de"
        val country = appLocale.country // e.g. "CH"

        // Search for LocalizedDisplay with a perfect match of $language-$country, e.g. "de-CH"
        val displayWithPerfectLocaleMatch = displays.getPerfectMatch(
            language = language,
            country = country,
        )

        // If available, return the perfectly matching LocalizedDisplay
        // Otherwise return the LocalizedDisplay whose locale's $language part has the lowest index aka highest priority
        return displayWithPerfectLocaleMatch ?: bestMatchingLocale(
            language = language,
            preferredLocale = preferredLocale,
            displays = displays
        )
    }

    private fun <T : LocalizedDisplay> Collection<T>.getPerfectMatch(
        language: String,
        country: String,
    ) = firstOrNull { display ->
        display.locale.formatLocale().equals("$language-$country", ignoreCase = true)
    }

    private fun String.formatLocale() = replace("_", "-")

    private fun String.language() = split("-", "_").first()

    private fun <T : LocalizedDisplay> bestMatchingLocale(language: String, preferredLocale: String?, displays: Collection<T>): T? {
        // Create a map of preferred languages with the provided language in the first place.
        // The map value indicates the preference order (lower index => higher priority)

        val preferredLanguages = setOf(language)
            .plus(DisplayLanguage.PRIORITIES)
            .mapIndexed { index: Int, s: String -> s to index }
            .toMap()
        return displays.minByOrNull { display ->
            preferredLanguages.getOrDefault(
                display.locale.language(),
                Int.MAX_VALUE
            )
        } ?: displays.firstOrNull { display ->
            display.locale.language().equals(preferredLocale?.language(), ignoreCase = true)
        } ?: displays.firstOrNull() // return the first LocalizedDisplay if none contains a preferred language
    }
}

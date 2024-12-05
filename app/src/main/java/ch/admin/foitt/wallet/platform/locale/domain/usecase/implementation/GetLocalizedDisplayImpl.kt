package ch.admin.foitt.wallet.platform.locale.domain.usecase.implementation

import ch.admin.foitt.wallet.platform.database.domain.model.DisplayLanguage
import ch.admin.foitt.wallet.platform.database.domain.model.LocalizedDisplay
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetCurrentAppLocale
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetLocalizedDisplay
import javax.inject.Inject

class GetLocalizedDisplayImpl @Inject constructor(
    private val getCurrentAppLocale: GetCurrentAppLocale
) : GetLocalizedDisplay {

    override fun <T : LocalizedDisplay> invoke(displays: Collection<T>): T? {
        val appLocale = getCurrentAppLocale()
        val language = appLocale.language // e.g. "de"
        val country = appLocale.country // e.g. "CH"

        // Search for LocalizedDisplay with a perfect match of $language-$country, e.g. "de-CH"
        val displayWithPerfectLocaleMatch = displays.firstOrNull { display ->
            display.locale.replace("_", "-").equals("$language-$country", ignoreCase = true)
        }

        // If available, return the perfectly matching LocalizedDisplay
        // Otherwise return the LocalizedDisplay whose locale's $language part has the lowest index aka highest priority
        return displayWithPerfectLocaleMatch ?: bestMatchingLocale(language = language, displays = displays)
    }

    private fun <T : LocalizedDisplay> bestMatchingLocale(language: String, displays: Collection<T>): T? {
        // Create a map of preferred languages with the provided language in the first place.
        // The map value indicates the preference order (lower index => higher priority)
        val preferredLanguages = setOf(language)
            .plus(DisplayLanguage.PRIORITIES)
            .mapIndexed { index: Int, s: String -> s to index }
            .toMap()
        return displays.minByOrNull { display ->
            preferredLanguages.getOrDefault(
                display.locale.split("-", "_").first(),
                Int.MAX_VALUE
            )
        } ?: displays.firstOrNull() // return the first LocalizedDisplay if none contains a preferred language
    }
}

package ch.admin.foitt.wallet.platform.locale.domain.usecase.implementation

import android.content.res.Resources
import androidx.appcompat.app.AppCompatDelegate
import ch.admin.foitt.wallet.platform.database.domain.model.DisplayLanguage
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetCurrentAppLocale
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetSupportedAppLanguages
import ch.admin.foitt.wallet.platform.utils.toListOfLocales
import java.util.Locale
import javax.inject.Inject

class GetCurrentAppLocaleImpl @Inject constructor(
    private val getSupportedAppLanguages: GetSupportedAppLanguages
) : GetCurrentAppLocale {

    private val defaultLanguage = Locale(DisplayLanguage.DEFAULT)

    override fun invoke(): Locale {
        return if (AppCompatDelegate.getApplicationLocales().isEmpty) {
            // no specific app language was set
            // -> use one from the device if possible (but use the order as prioritization)
            useDeviceLanguageOrDefault()
        } else {
            useAppSpecificLanguage()
        }
    }

    private fun useDeviceLanguageOrDefault(): Locale {
        val supportedLanguages = getSupportedAppLanguages().map { it.language }.toSet()
        val systemLocales = Resources.getSystem().configuration.locales.toListOfLocales()
        val systemLanguages = systemLocales.map { it.language }
        val preferredLanguage = systemLanguages.intersect(supportedLanguages).firstOrNull() ?: defaultLanguage.language
        return Locale(preferredLanguage)
    }

    private fun useAppSpecificLanguage(): Locale {
        return AppCompatDelegate.getApplicationLocales()[0] ?: defaultLanguage
    }
}

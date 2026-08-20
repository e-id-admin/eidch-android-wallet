package ch.admin.foitt.wallet.platform.locale.domain.usecase.implementation

import android.app.LocaleConfig
import android.content.Context
import android.os.Build
import ch.admin.foitt.wallet.BuildConfig
import ch.admin.foitt.wallet.platform.locale.DebugLocale
import ch.admin.foitt.wallet.platform.locale.LocaleCompat
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetSupportedAppLocales
import ch.admin.foitt.wallet.platform.utils.toListOfLocales
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject

class GetSupportedAppLocalesImpl @Inject constructor(
    @param:ApplicationContext private val appContext: Context
) : GetSupportedAppLocales {

    // keep this in sync with the locales_config.xml
    private val supportedLanguagesDefault = listOf(
        LocaleCompat.of("de"),
        LocaleCompat.of("en"),
        LocaleCompat.of("fr"),
        LocaleCompat.of("it"),
        LocaleCompat.of("rm")
    )

    override fun invoke(): List<Locale> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return supportedLanguagesDefault.withDebugLocale()
        }

        // To enable supported locales, add android:localeConfig="@xml/locales_config" into AndroidManifest.xml
        val supportedLanguages = LocaleConfig(appContext).supportedLocales?.toListOfLocales() ?: emptyList()
        return supportedLanguages.ifEmpty {
            Timber.d("No supported languages found, using default list ${supportedLanguagesDefault.map { it.language }}")
            supportedLanguagesDefault
        }.withDebugLocale()
    }

    private fun List<Locale>.withDebugLocale(): List<Locale> =
        if (BuildConfig.DEBUG_LOCALE_ENABLED) this + LocaleCompat.of(DebugLocale.LANGUAGE) else this
}

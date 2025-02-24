package ch.admin.foitt.wallet.platform.locale.domain.usecase.implementation

import android.app.LocaleConfig
import android.content.Context
import android.os.Build
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetSupportedAppLanguages
import ch.admin.foitt.wallet.platform.utils.toListOfLocales
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.Locale
import javax.inject.Inject

class GetSupportedAppLanguagesImpl @Inject constructor(
    @ApplicationContext private val appContext: Context
) : GetSupportedAppLanguages {

    // keep this in sync with the locales_config.xml
    private val supportedLanguagesDefault = listOf(
        Locale("de"),
        Locale("en"),
        Locale("fr"),
        Locale("it"),
    )

    override fun invoke(): List<Locale> {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return supportedLanguagesDefault
        }

        // To enable supported locales, add android:localeConfig="@xml/locales_config" into AndroidManifest.xml
        val supportedLanguages = LocaleConfig(appContext).supportedLocales?.toListOfLocales() ?: emptyList()
        return supportedLanguages.ifEmpty {
            Timber.d("No supported languages found, using default list ${supportedLanguagesDefault.map { it.language }}")
            supportedLanguagesDefault
        }
    }
}

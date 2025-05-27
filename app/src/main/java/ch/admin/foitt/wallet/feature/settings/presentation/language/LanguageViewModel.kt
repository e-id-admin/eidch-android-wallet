package ch.admin.foitt.wallet.feature.settings.presentation.language

import android.content.res.Resources
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import ch.admin.foitt.wallet.platform.database.domain.model.DisplayLanguage
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetCurrentAppLocale
import ch.admin.foitt.wallet.platform.locale.domain.usecase.GetSupportedAppLanguages
import ch.admin.foitt.wallet.platform.navigation.NavigationManager
import ch.admin.foitt.wallet.platform.scaffold.domain.model.TopBarState
import ch.admin.foitt.wallet.platform.scaffold.domain.usecase.SetTopBarState
import ch.admin.foitt.wallet.platform.scaffold.presentation.ScreenViewModel
import ch.admin.foitt.wallet.platform.utils.toListOfLocales
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class LanguageViewModel @Inject constructor(
    private val navManager: NavigationManager,
    private val getSupportedAppLanguages: GetSupportedAppLanguages,
    private val getCurrentAppLocale: GetCurrentAppLocale,
    setTopBarState: SetTopBarState,
) : ScreenViewModel(setTopBarState) {

    override val topBarState = TopBarState.Details(navManager::popBackStack, null)

    private val defaultLanguage = Locale(DisplayLanguage.DEFAULT)

    private var _selectedLanguage = MutableStateFlow(defaultLanguage)
    val selectedLanguage = _selectedLanguage.asStateFlow()

    private var _supportedLanguages = MutableStateFlow(listOf(defaultLanguage))
    val supportedLanguages = _supportedLanguages.asStateFlow()

    private var _isSystemLanguage = MutableStateFlow(true)
    val isSystemLanguage = _isSystemLanguage.asStateFlow()

    init {
        _supportedLanguages.value = getSupportedAppLanguages()
        _selectedLanguage.value = getSelectedLanguage()
    }

    private fun getSelectedLanguage(): Locale {
        return if (AppCompatDelegate.getApplicationLocales().isEmpty) {
            // no specific app language was set
            // -> use one from the device if possible (but use the order as prioritization)
            useDeviceLanguageOrDefault()
        } else {
            useAppSpecificLanguage()
        }
    }

    private fun useDeviceLanguageOrDefault(): Locale {
        _isSystemLanguage.value = true
        val supportedLanguages = getSupportedAppLanguages().map { it.language }.toSet()
        val systemLocales = Resources.getSystem().configuration.locales.toListOfLocales()
        val systemLanguages = systemLocales.map { it.language }
        val preferredLanguage = systemLanguages.intersect(supportedLanguages).firstOrNull() ?: defaultLanguage.language
        return Locale(preferredLanguage)
    }

    private fun useAppSpecificLanguage(): Locale {
        _isSystemLanguage.value = false
        return AppCompatDelegate.getApplicationLocales()[0] ?: defaultLanguage
    }

    fun checkLanguageChangedInSettings() {
        val currentLanguage = getCurrentAppLocale()
        val isCurrentLanguageSystemDefault = AppCompatDelegate.getApplicationLocales().isEmpty
        if (currentLanguage.language != selectedLanguage.value.language || isSystemLanguage.value != isCurrentLanguageSystemDefault) {
            _isSystemLanguage.value = isCurrentLanguageSystemDefault
            _selectedLanguage.value = currentLanguage
        }
    }

    fun onUpdateLanguage(locale: Locale) {
        _isSystemLanguage.value = false
        _selectedLanguage.value = locale
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(locale.language))
    }

    fun useSystemDefaultLanguage() {
        _isSystemLanguage.value = true
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
    }
}

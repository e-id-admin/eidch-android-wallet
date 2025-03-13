package ch.admin.foitt.wallet.feature.settings.presentation.language

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.settings.presentation.composables.SettingsLanguageItem
import ch.admin.foitt.wallet.platform.composables.ScreenHeader
import ch.admin.foitt.wallet.platform.composables.presentation.addTopScaffoldPadding
import ch.admin.foitt.wallet.platform.composables.presentation.bottomSafeDrawing
import ch.admin.foitt.wallet.platform.composables.presentation.horizontalSafeDrawing
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTheme
import com.ramcosta.composedestinations.annotation.Destination
import java.util.Locale

@Destination
@Composable
fun LanguageScreen(
    viewModel: LanguageViewModel
) {
    viewModel.checkLanguageChangedInSettings()

    LanguageScreenContent(
        isSystemLanguage = viewModel.isSystemLanguage.collectAsStateWithLifecycle().value,
        language = viewModel.selectedLanguage.collectAsStateWithLifecycle().value.displayLanguage,
        supportedLanguages = viewModel.supportedLanguages.collectAsStateWithLifecycle().value,
        onUpdateLanguage = viewModel::onUpdateLanguage,
        onUseSystemDefaultLanguage = viewModel::useSystemDefaultLanguage,
    )
}

@Composable
private fun LanguageScreenContent(
    isSystemLanguage: Boolean,
    language: String,
    supportedLanguages: List<Locale>,
    onUpdateLanguage: (Locale) -> Unit,
    onUseSystemDefaultLanguage: () -> Unit,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .addTopScaffoldPadding()
            .verticalScroll(state = scrollState)
            .horizontalSafeDrawing()
            .bottomSafeDrawing()
            .padding(
                PaddingValues(
                    top = Sizes.s04,
                    bottom = Sizes.s24,
                    start = Sizes.s04,
                    end = Sizes.s04,
                )
            )
    ) {
        ScreenHeader(text = stringResource(id = R.string.settings_language))
        Spacer(modifier = Modifier.height(Sizes.s04))
        supportedLanguages.forEach { locale ->
            SettingsLanguageItem(
                title = locale.displayLanguage,
                isChecked = locale.displayLanguage == language && !isSystemLanguage
            ) {
                onUpdateLanguage(locale)
            }
        }
        SettingsLanguageItem(
            title = stringResource(id = R.string.languageSettings_systemDefaultLanguage_button),
            isChecked = isSystemLanguage
        ) {
            onUseSystemDefaultLanguage()
        }
    }
}

@WalletAllScreenPreview
@Composable
private fun SettingsScreenPreview() {
    WalletTheme {
        LanguageScreenContent(
            isSystemLanguage = false,
            language = "English",
            supportedLanguages = listOf(Locale("en"), Locale("de")),
            onUpdateLanguage = {},
            onUseSystemDefaultLanguage = {},
        )
    }
}

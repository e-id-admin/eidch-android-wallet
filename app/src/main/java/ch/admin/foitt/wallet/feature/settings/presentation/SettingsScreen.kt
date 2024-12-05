package ch.admin.foitt.wallet.feature.settings.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletListItems
import ch.admin.foitt.wallet.theme.WalletTheme
import com.ramcosta.composedestinations.annotation.Destination

@Destination
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel
) {
    SettingsScreenContent(
        onSecurityScreen = viewModel::onSecurityScreen,
        onLanguageScreen = viewModel::onLanguageScreen,
        onHelp = viewModel::onHelp,
        onContact = viewModel::onContact,
        onImpressumScreen = viewModel::onImpressumScreen,
        onLicencesScreen = viewModel::onLicencesScreen,
    )
}

@Composable
private fun SettingsScreenContent(
    onSecurityScreen: () -> Unit,
    onLanguageScreen: () -> Unit,
    onHelp: () -> Unit,
    onContact: () -> Unit,
    onImpressumScreen: () -> Unit,
    onLicencesScreen: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(state = rememberScrollState())
            .navigationBarsPadding()
            .padding(
                top = Sizes.s04,
                bottom = Sizes.s04,
            )
    ) {
        SettingsSection(onSecurityScreen, onLanguageScreen)
        Spacer(modifier = Modifier.height(Sizes.s10))

        SupportSection(onHelp, onContact)
        Spacer(modifier = Modifier.height(Sizes.s10))

        InfoSection(onImpressumScreen, onLicencesScreen)
    }
}

@Composable
private fun SettingsSection(
    onSecurityScreen: () -> Unit,
    onLanguageScreen: () -> Unit,
) {
    WalletListItems.SimpleListItem(
        leadingIcon = R.drawable.pilot_ic_security,
        title = stringResource(id = R.string.settings_security),
        onItemClick = onSecurityScreen,
        trailingIcon = R.drawable.pilot_ic_settings_next,
    )
    WalletListItems.SimpleListItem(
        leadingIcon = R.drawable.pilot_ic_language,
        title = stringResource(id = R.string.settings_language),
        onItemClick = onLanguageScreen,
        trailingIcon = R.drawable.pilot_ic_settings_next,
        showDivider = false,
    )
}

@Composable
private fun SupportSection(
    onHelp: () -> Unit,
    onContact: () -> Unit
) {
    WalletListItems.SimpleListItem(
        leadingIcon = R.drawable.pilot_ic_help,
        title = stringResource(id = R.string.settings_help),
        onItemClick = onHelp,
        trailingIcon = R.drawable.pilot_ic_settings_link,
    )
    WalletListItems.SimpleListItem(
        leadingIcon = R.drawable.pilot_ic_contact,
        title = stringResource(id = R.string.settings_contact),
        onItemClick = onContact,
        trailingIcon = R.drawable.pilot_ic_settings_link,
        showDivider = false,
    )
}

@Composable
private fun InfoSection(onImpressumScreen: () -> Unit, onLicencesScreen: () -> Unit) {
    WalletListItems.SimpleListItem(
        leadingIcon = R.drawable.pilot_ic_impressum,
        title = stringResource(id = R.string.settings_impressum),
        onItemClick = onImpressumScreen,
        trailingIcon = R.drawable.pilot_ic_settings_next,
    )
    WalletListItems.SimpleListItem(
        leadingIcon = R.drawable.pilot_ic_document,
        title = stringResource(id = R.string.settings_licences),
        onItemClick = onLicencesScreen,
        trailingIcon = R.drawable.pilot_ic_settings_next,
        showDivider = false,
    )
}

@WalletAllScreenPreview
@Composable
fun SettingsScreenPreview() {
    WalletTheme {
        SettingsScreenContent(
            onSecurityScreen = {},
            onLanguageScreen = {},
            onHelp = {},
            onContact = {},
            onImpressumScreen = {},
            onLicencesScreen = {},
        )
    }
}

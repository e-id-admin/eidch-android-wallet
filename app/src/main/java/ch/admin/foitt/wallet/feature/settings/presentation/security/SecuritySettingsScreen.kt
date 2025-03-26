package ch.admin.foitt.wallet.feature.settings.presentation.security

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.ToastAnimated
import ch.admin.foitt.wallet.platform.composables.presentation.addTopScaffoldPadding
import ch.admin.foitt.wallet.platform.composables.presentation.bottomSafeDrawing
import ch.admin.foitt.wallet.platform.composables.presentation.horizontalSafeDrawing
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.utils.OnPauseEventHandler
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletIcons
import ch.admin.foitt.wallet.theme.WalletListItems
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme
import com.ramcosta.composedestinations.annotation.Destination

@Destination
@Composable
fun SecuritySettingsScreen(
    viewModel: SecuritySettingsViewModel
) {
    OnPauseEventHandler {
        viewModel.hidePassphraseChangeSuccessToast()
    }

    SecuritySettingsScreenContent(
        biometricsHardwareIsAvailable = viewModel.biometricsHardwareIsAvailable.collectAsStateWithLifecycle(true).value,
        biometricsEnabled = viewModel.isBiometricsToggleEnabled.collectAsStateWithLifecycle(false).value,
        shareAnalysisEnabled = viewModel.shareAnalysisEnabled.collectAsStateWithLifecycle().value,
        showPassphraseDeletionMessage = viewModel.showPassphraseDeletionMessage.collectAsStateWithLifecycle(false).value,
        isToastVisible = viewModel.isToastVisible.collectAsStateWithLifecycle(false).value,
        onChangePin = viewModel::onChangePassphrase,
        onChangeBiometrics = viewModel::onChangeBiometrics,
        onDataProtection = viewModel::onDataProtection,
        onShareAnalysisChange = viewModel::onShareAnalysisChange,
        onDataAnalysis = viewModel::onDataAnalysis,
    )
}

@Composable
private fun SecuritySettingsScreenContent(
    biometricsHardwareIsAvailable: Boolean,
    biometricsEnabled: Boolean,
    shareAnalysisEnabled: Boolean,
    showPassphraseDeletionMessage: Boolean,
    isToastVisible: Boolean,
    onChangePin: () -> Unit,
    onChangeBiometrics: () -> Unit,
    onDataProtection: () -> Unit,
    onShareAnalysisChange: (Boolean) -> Unit,
    onDataAnalysis: () -> Unit,
) = Box(
    modifier = Modifier.fillMaxSize()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .addTopScaffoldPadding()
            .verticalScroll(state = rememberScrollState())
            .horizontalSafeDrawing()
            .bottomSafeDrawing()
            .padding(
                top = Sizes.s05,
                bottom = Sizes.s05,
            )
    ) {
        LoginSection(
            biometricsAvailableThroughDeviceSettings = biometricsHardwareIsAvailable,
            biometricsEnabled = biometricsEnabled,
            showPassphraseDeletionMessage = showPassphraseDeletionMessage,
            onChangePin = onChangePin,
            onChangeBiometrics = onChangeBiometrics,
        )
        Spacer(modifier = Modifier.height(Sizes.s10))
        AnalysisSection(
            shareAnalysisEnabled = shareAnalysisEnabled,
            onDataProtection = onDataProtection,
            onShareAnalysisChange = onShareAnalysisChange,
            onDataAnalysis = onDataAnalysis,
        )
    }
    ToastAnimated(
        isVisible = isToastVisible,
        isSnackBarDesign = false,
        messageToast = R.string.tk_changepassword_successful_notification,
        contentBottomPadding = Sizes.s10
    )
}

@Composable
private fun LoginSection(
    biometricsAvailableThroughDeviceSettings: Boolean,
    biometricsEnabled: Boolean,
    showPassphraseDeletionMessage: Boolean,
    onChangePin: () -> Unit,
    onChangeBiometrics: () -> Unit
) {
    SectionHeader(
        icon = R.drawable.pilot_ic_settings_hand,
        title = R.string.securitySettings_loginTitle,
    )
    Spacer(modifier = Modifier.height(Sizes.s04))
    WalletListItems.SimpleListItem(
        title = stringResource(id = R.string.securitySettings_changePin),
        onItemClick = onChangePin,
        trailingIcon = R.drawable.pilot_ic_settings_next,
    )
    WalletListItems.SwitchListItem(
        title = stringResource(id = R.string.securitySettings_biometrics),
        description = biometricsDescription(biometricsAvailableThroughDeviceSettings, showPassphraseDeletionMessage),
        isSwitchEnabled = biometricsAvailableThroughDeviceSettings,
        isSwitchChecked = biometricsEnabled,
        onSwitchChange = { onChangeBiometrics() },
        showDivider = false,
    )
}

@Composable
fun AnalysisSection(
    onDataProtection: () -> Unit,
    shareAnalysisEnabled: Boolean,
    onShareAnalysisChange: (Boolean) -> Unit,
    onDataAnalysis: () -> Unit
) {
    SectionHeader(
        icon = R.drawable.pilot_ic_analysis,
        title = R.string.securitySettings_analysisTitle,
    )
    Spacer(modifier = Modifier.height(Sizes.s04))
    WalletListItems.SimpleListItem(
        title = stringResource(id = R.string.securitySettings_dataProtection),
        onItemClick = onDataProtection,
        trailingIcon = R.drawable.pilot_ic_settings_link,
    )
    WalletListItems.SwitchListItem(
        title = stringResource(id = R.string.securitySettings_shareAnalysis),
        description = shareAnalysisDescription(),
        isSwitchChecked = shareAnalysisEnabled,
        onSwitchChange = onShareAnalysisChange,
    )
    WalletListItems.SimpleListItem(
        title = stringResource(id = R.string.securitySettings_dataAnalysis),
        onItemClick = onDataAnalysis,
        trailingIcon = R.drawable.pilot_ic_settings_next,
        showDivider = false,
    )
}

@Composable
private fun SectionHeader(@DrawableRes icon: Int, @StringRes title: Int) {
    Row(
        modifier = Modifier.padding(start = Sizes.s04, end = Sizes.s04),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        WalletIcons.IconWithBackground(
            icon = painterResource(id = icon)
        )
        Spacer(modifier = Modifier.width(Sizes.s04))
        WalletTexts.TitleSmall(text = stringResource(id = title))
    }
}

@Composable
private fun shareAnalysisDescription(): AnnotatedString {
    val boldText = stringResource(id = R.string.securitySettings_shareAnalysis_text)
    val description = stringResource(id = R.string.securitySettings_shareAnalysis_text, boldText)
    val start = description.indexOf(boldText)
    val spanStyles = listOf(
        AnnotatedString.Range(
            SpanStyle(fontWeight = FontWeight.Bold),
            start = start,
            end = start + boldText.length,
        )
    )
    return AnnotatedString(text = description, spanStyles = spanStyles)
}

@Composable
private fun biometricsDescription(biometricHardwareAvailable: Boolean, showPassphraseDeletionMessage: Boolean): AnnotatedString? {
    val stringId = when {
        !biometricHardwareAvailable -> R.string.securitySettings_biometrics_noHardware
        showPassphraseDeletionMessage -> R.string.securitySettings_biometrics_pinChanged
        else -> null
    }
    return stringId?.let { AnnotatedString(stringResource(id = stringId)) }
}

private class SecuritySettingsPreviewParams : PreviewParameterProvider<Pair<Boolean, Boolean>> {
    override val values = sequenceOf(
        Pair(false, false),
        Pair(true, false),
        Pair(false, true),
        Pair(true, true),
    )
}

@WalletAllScreenPreview
@Composable
private fun SecuritySettingsScreenPreview(
    @PreviewParameter(SecuritySettingsPreviewParams::class) previewParams: Pair<Boolean, Boolean>,
) {
    WalletTheme {
        SecuritySettingsScreenContent(
            biometricsHardwareIsAvailable = previewParams.first,
            biometricsEnabled = previewParams.second,
            shareAnalysisEnabled = true,
            isToastVisible = false,
            onChangeBiometrics = {},
            onChangePin = {},
            onDataProtection = {},
            onShareAnalysisChange = {},
            onDataAnalysis = {},
            showPassphraseDeletionMessage = false,
        )
    }
}

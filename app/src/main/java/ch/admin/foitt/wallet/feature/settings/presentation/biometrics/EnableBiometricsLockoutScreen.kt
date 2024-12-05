package ch.admin.foitt.wallet.feature.settings.presentation.biometrics

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.SimpleScreenContent
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.theme.WalletTheme
import com.ramcosta.composedestinations.annotation.Destination

@Destination
@Composable
fun EnableBiometricsLockoutScreen(viewModel: EnableBiometricsLockoutViewModel) {
    EnableBiometricsLockoutContent(
        onClose = viewModel::onClose,
    )
}

@Composable
private fun EnableBiometricsLockoutContent(
    onClose: () -> Unit,
) = SimpleScreenContent(
    icon = R.drawable.pilot_ic_lockout,
    titleText = stringResource(id = R.string.biometrics_lockout_title),
    mainText = stringResource(id = R.string.biometrics_lockout_text),
    bottomBlockContent = {
        Buttons.Outlined(
            text = stringResource(id = R.string.global_error_backToHome_button),
            startIcon = painterResource(id = R.drawable.pilot_ic_back_button),
            onClick = onClose,
        )
    }
)

@WalletAllScreenPreview
@Composable
private fun EnableBiometricsLockoutPreview() {
    WalletTheme {
        EnableBiometricsLockoutContent(
            onClose = {},
        )
    }
}

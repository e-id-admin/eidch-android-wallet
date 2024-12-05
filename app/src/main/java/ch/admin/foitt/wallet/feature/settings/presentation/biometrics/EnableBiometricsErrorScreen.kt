package ch.admin.foitt.wallet.feature.settings.presentation.biometrics

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.ResultScreenContent
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme
import ch.admin.foitt.wallet.theme.errorBackgroundDark
import ch.admin.foitt.wallet.theme.errorBackgroundLight
import com.ramcosta.composedestinations.annotation.Destination

@Composable
@Destination
fun EnableBiometricsErrorScreen(viewModel: EnableBiometricsErrorViewModel) {
    EnableBiometricsErrorContent(
        dateTime = viewModel.dateTime,
        onClose = viewModel::onClose,
    )
}

@Composable
private fun EnableBiometricsErrorContent(
    dateTime: String,
    onClose: () -> Unit,
) = ResultScreenContent(
    iconRes = R.drawable.pilot_ic_warning_big,
    dateTime = dateTime,
    message = stringResource(R.string.global_error_unexpected_title),
    topColor = MaterialTheme.colorScheme.errorBackgroundLight,
    bottomColor = MaterialTheme.colorScheme.errorBackgroundDark,
    bottomContent = {
        Buttons.TonalSecondary(
            text = stringResource(id = R.string.global_error_backToHome_button),
            onClick = onClose,
            startIcon = painterResource(id = R.drawable.pilot_ic_back_button),
        )
    },
    content = {
        WalletTexts.BodySmallCentered(
            text = stringResource(id = R.string.global_error_unexpected_message),
            color = MaterialTheme.colorScheme.onError,
        )
    }
)

@Composable
@WalletAllScreenPreview
private fun EnableBiometricsErrorPreview() {
    WalletTheme {
        EnableBiometricsErrorContent(
            dateTime = "10th December 1990 | 11:17",
            onClose = {},
        )
    }
}

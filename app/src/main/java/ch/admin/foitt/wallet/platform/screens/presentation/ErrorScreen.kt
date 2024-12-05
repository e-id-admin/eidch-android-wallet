package ch.admin.foitt.wallet.platform.screens.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
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

@Destination
@Composable
fun ErrorScreen(
    viewModel: ErrorViewModel,
) {
    ErrorScreenContent(
        dateTime = viewModel.dateTime,
        onBack = viewModel::onBack,
    )
}

@Composable
private fun ErrorScreenContent(
    dateTime: String,
    onBack: () -> Unit,
) {
    ResultScreenContent(
        iconRes = R.drawable.pilot_ic_warning_big,
        dateTime = dateTime,
        message = stringResource(id = R.string.presentation_error_title),
        topColor = MaterialTheme.colorScheme.errorBackgroundLight,
        bottomColor = MaterialTheme.colorScheme.errorBackgroundDark,
        bottomContent = {
            Buttons.TonalSecondary(
                text = stringResource(id = R.string.global_error_backToHome_button),
                onClick = onBack,
            )
        },
        content = {
            WalletTexts.BodySmallCentered(
                text = stringResource(id = R.string.presentation_error_message),
                color = MaterialTheme.colorScheme.onError,
            )
        },
    )
}

@WalletAllScreenPreview
@Composable
fun ErrorScreenPreview() {
    WalletTheme {
        ErrorScreenContent(
            dateTime = "14th December 2023 | 08:00",
            onBack = {},
        )
    }
}

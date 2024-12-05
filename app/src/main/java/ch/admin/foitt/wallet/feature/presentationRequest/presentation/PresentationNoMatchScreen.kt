package ch.admin.foitt.wallet.feature.presentationRequest.presentation

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
fun PresentationNoMatchScreen(viewModel: PresentationNoMatchViewModel) {
    PresentationNoMatchContent(
        onBack = viewModel::onBack,
    )
}

@Composable
private fun PresentationNoMatchContent(
    onBack: () -> Unit,
) {
    SimpleScreenContent(
        icon = R.drawable.pilot_ic_missing_credential,
        titleText = stringResource(R.string.presentation_error_no_compatible_credential_title),
        mainText = stringResource(R.string.presentation_error_no_compatible_credential_message),
        bottomBlockContent = {
            Buttons.Outlined(
                text = stringResource(id = R.string.global_error_backToHome_button),
                onClick = onBack,
                startIcon = painterResource(id = R.drawable.pilot_ic_back_button),
            )
        },
    )
}

@WalletAllScreenPreview
@Composable
private fun PresentationNoMatchPreview() {
    WalletTheme {
        PresentationNoMatchContent(
            onBack = {},
        )
    }
}

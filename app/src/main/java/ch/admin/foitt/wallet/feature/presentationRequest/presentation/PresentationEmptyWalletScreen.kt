package ch.admin.foitt.wallet.feature.presentationRequest.presentation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.SimpleScreenContent
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme
import com.ramcosta.composedestinations.annotation.Destination

@Destination
@Composable
fun PresentationEmptyWalletScreen(viewModel: PresentationEmptyWalletViewModel) {
    PresentationEmptyWalletContent(
        onSupport = viewModel::onSupport,
        onBack = viewModel::onBack,
    )
}

@Composable
private fun PresentationEmptyWalletContent(
    onSupport: () -> Unit,
    onBack: () -> Unit,
) {
    SimpleScreenContent(
        icon = R.drawable.pilot_ic_presentation_error_empty_wallet,
        titleText = stringResource(R.string.presentation_error_empty_wallet_title),
        mainContent = {
            WalletTexts.Body(
                text = stringResource(R.string.presentation_error_empty_wallet_message),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(modifier = Modifier.height(Sizes.s02))
            Buttons.TextLink(
                text = stringResource(id = R.string.presentation_error_empty_wallet_support_text),
                endIcon = painterResource(id = R.drawable.pilot_ic_link),
                onClick = onSupport,
            )
        },
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
private fun PresentationEmptyWalletPreview() {
    WalletTheme {
        PresentationEmptyWalletContent(
            onSupport = {},
            onBack = {},
        )
    }
}

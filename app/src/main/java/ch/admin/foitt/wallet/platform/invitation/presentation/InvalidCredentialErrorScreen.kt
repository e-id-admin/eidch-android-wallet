package ch.admin.foitt.wallet.platform.invitation.presentation

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
fun InvalidCredentialErrorScreen(
    viewModel: InvalidCredentialErrorViewModel,
) {
    InvalidCredentialErrorScreenContent(
        onMoreInformation = viewModel::onMoreInformation,
        onBack = viewModel::onBack,
    )
}

@Composable
private fun InvalidCredentialErrorScreenContent(
    onMoreInformation: () -> Unit,
    onBack: () -> Unit,
) {
    SimpleScreenContent(
        icon = R.drawable.pilot_ic_invalid_credential,
        titleText = stringResource(id = R.string.invitation_error_credential_expired_title),
        mainContent = { MainContent(onMoreInformation) },
        bottomBlockContent = {
            Buttons.Outlined(
                text = stringResource(id = R.string.credential_offer_error_back_button),
                startIcon = painterResource(id = R.drawable.pilot_ic_back_button),
                onClick = { onBack() },
            )
        }
    )
}

@Composable
private fun MainContent(onMoreInformation: () -> Unit) {
    WalletTexts.Body(
        text = stringResource(id = R.string.invitation_error_credential_expired_message),
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(modifier = Modifier.height(Sizes.s06))
    Buttons.TextLink(
        text = stringResource(id = R.string.invitation_error_credential_expired_more_info_title),
        onClick = onMoreInformation,
        endIcon = painterResource(id = R.drawable.pilot_ic_link),
    )
}

@WalletAllScreenPreview
@Composable
fun InvalidCredentialErrorScreenPreview() {
    WalletTheme {
        InvalidCredentialErrorScreenContent(
            onMoreInformation = {},
            onBack = {},
        )
    }
}

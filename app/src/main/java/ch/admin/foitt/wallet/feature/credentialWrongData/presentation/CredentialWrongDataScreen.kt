package ch.admin.foitt.wallet.feature.credentialWrongData.presentation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.presentation.ScreenMainImage
import ch.admin.foitt.wallet.platform.composables.presentation.layout.ScrollableColumnWithPicture
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme
import com.ramcosta.composedestinations.annotation.Destination

@Destination
@Composable
fun CredentialWrongDataScreen(viewModel: CredentialWrongDataViewModel) {
    CredentialWrongDataScreenContent(
        onMoreInfo = viewModel::onMoreInfo,
    )
}

@Composable
private fun CredentialWrongDataScreenContent(
    onMoreInfo: () -> Unit,
) = WalletLayouts.ScrollableColumnWithPicture(
    stickyStartContent = {
        ScreenMainImage(
            iconRes = R.drawable.wallet_ic_cross_circle_colored,
            backgroundColor = WalletTheme.colorScheme.surfaceContainerHigh
        )
    },
    stickyBottomContent = null
) {
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.TitleLarge(
        text = stringResource(id = R.string.tk_global_wrong_data),
    )
    Spacer(modifier = Modifier.height(Sizes.s05))
    WalletTexts.BodyLarge(
        text = stringResource(id = R.string.tk_receive_incorrectdata_body),
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(modifier = Modifier.height(Sizes.s05))
    Buttons.TextLink(
        text = stringResource(id = R.string.tk_receive_incorrectdata_link_text),
        endIcon = painterResource(id = R.drawable.wallet_ic_chevron),
        onClick = onMoreInfo
    )
}

@WalletAllScreenPreview
@Composable
private fun CredentialWrongDataScreenPreview() {
    WalletTheme {
        CredentialWrongDataScreenContent(
            onMoreInfo = {},
        )
    }
}

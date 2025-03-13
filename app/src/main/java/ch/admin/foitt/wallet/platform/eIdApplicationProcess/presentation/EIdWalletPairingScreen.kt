package ch.admin.foitt.wallet.platform.eIdApplicationProcess.presentation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
fun EIdWalletPairingScreen(
    viewModel: EIdWalletPairingViewModel,
) {
    EIdWalletPairingScreenContent(
        onSingleDeviceFlow = viewModel::onSingleDeviceFlow,
        onMultiDeviceFlow = viewModel::onMultiDeviceFlow,
    )
}

@Composable
private fun EIdWalletPairingScreenContent(
    onSingleDeviceFlow: () -> Unit,
    onMultiDeviceFlow: () -> Unit
) {
    WalletLayouts.ScrollableColumnWithPicture(
        stickyStartContent = {
            ScreenMainImage(
                iconRes = R.drawable.wallet_ic_pairing,
                backgroundColor = WalletTheme.colorScheme.surfaceContainerLow,
            )
        },
        stickyBottomBackgroundColor = Color.Transparent,
        stickyBottomContent = {
            Buttons.TonalSecondary(
                text = stringResource(R.string.tk_getEid_walletPairing1_secondaryButton),
                onClick = onMultiDeviceFlow,
            )
            Buttons.FilledPrimary(
                text = stringResource(R.string.tk_getEid_walletPairing1_primaryButton),
                onClick = onSingleDeviceFlow,
            )
        }
    ) {
        Spacer(modifier = Modifier.height(Sizes.s06))
        WalletTexts.TitleScreen(
            text = stringResource(R.string.tk_getEid_walletPairing1_title)
        )
        Spacer(modifier = Modifier.height(Sizes.s06))
        WalletTexts.BodyLarge(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.tk_getEid_walletPairing1_body)
        )
        Spacer(modifier = Modifier.height(Sizes.s06))
        WalletTexts.BodySmall(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(R.string.tk_getEid_walletPairing1_smallBody)
        )
    }
}

@WalletAllScreenPreview
@Composable
private fun EIdWalletPairingScreenPreview() {
    WalletTheme {
        EIdWalletPairingScreenContent(
            onSingleDeviceFlow = {},
            onMultiDeviceFlow = {},
        )
    }
}

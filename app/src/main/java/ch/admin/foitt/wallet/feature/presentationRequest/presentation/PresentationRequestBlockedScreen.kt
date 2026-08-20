package ch.admin.foitt.wallet.feature.presentationRequest.presentation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.AdaptiveBottomButtonBar
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.presentation.ScreenMainImage
import ch.admin.foitt.wallet.platform.composables.presentation.layout.ScrollableColumnWithPicture
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun PresentationRequestBlockedScreen(viewModel: PresentationRequestBlockedViewModel) {
    PresentationRequestBlockedScreenContent(
        onClose = viewModel::onClose
    )
}

@Composable
private fun PresentationRequestBlockedScreenContent(onClose: () -> Unit) {
    WalletLayouts.ScrollableColumnWithPicture(
        stickyStartContent = {
            ScreenMainImage(
                iconRes = R.drawable.wallet_ic_blocked_colored,
                backgroundColor = WalletTheme.colorScheme.surfaceContainerLow,
            )
        },
        stickyBottomContent = {
            AdaptiveBottomButtonBar(
                buttons = listOf(
                    {
                        Buttons.FilledPrimary(
                            text = stringResource(R.string.tk_present_blocked_button_close),
                            onClick = onClose,
                        )
                    }
                )
            )
        },
    ) {
        Spacer(modifier = Modifier.height(Sizes.s06))
        WalletTexts.TitleScreen(
            text = stringResource(R.string.tk_present_blocked_title)
        )
        Spacer(modifier = Modifier.height(Sizes.s06))
        WalletTexts.BodyLarge(
            text = stringResource(R.string.tk_present_blocked_body)
        )
    }
}

@Composable
@WalletAllScreenPreview
private fun PresentationRequestBlockedPreview() {
    WalletTheme {
        PresentationRequestBlockedScreenContent(
            onClose = {},
        )
    }
}

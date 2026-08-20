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
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun PresentationRequestReviewScreen(viewModel: PresentationRequestReviewViewModel) {
    PresentationRequestReviewScreenContent(
        onProceed = viewModel::onProceed,
        onCancel = viewModel::onCancel
    )
}

@Composable
private fun PresentationRequestReviewScreenContent(
    onProceed: () -> Unit,
    onCancel: () -> Unit
) {
    WalletLayouts.ScrollableColumnWithPicture(
        stickyStartContent = {
            ScreenMainImage(
                iconRes = R.drawable.wallet_ic_warning_colored,
                backgroundColor = WalletTheme.colorScheme.surfaceContainerLow,
            )
        },
        stickyBottomContent = {
            AdaptiveBottomButtonBar(
                buttons = listOf(
                    {
                        Buttons.FilledSecondary(
                            text = stringResource(R.string.tk_present_unregisteredRequest_secondaryButton),
                            onClick = onProceed,
                        )
                        Buttons.Text(
                            text = stringResource(R.string.tk_global_cancel),
                            onClick = onCancel,
                        )
                    }
                )
            )
        },
    ) {
        Spacer(modifier = Modifier.height(Sizes.s06))
        WalletTexts.TitleScreen(
            text = stringResource(R.string.tk_present_review_unregisteredRequestWarning_primary)
        )
        Spacer(modifier = Modifier.height(Sizes.s06))
        WalletTexts.BodyLarge(
            text = stringResource(R.string.tk_present_review_unregisteredRequestWarning_secondary)
        )
    }
}

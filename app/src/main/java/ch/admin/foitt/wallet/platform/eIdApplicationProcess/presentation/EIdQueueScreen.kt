package ch.admin.foitt.wallet.platform.eIdApplicationProcess.presentation

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.presentation.ScreenMainImage
import ch.admin.foitt.wallet.platform.composables.presentation.layout.ScrollableColumnWithPicture
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.navArgs.domain.model.EIdQueueNavArg
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme
import com.ramcosta.composedestinations.annotation.Destination

@Destination(
    navArgsDelegate = EIdQueueNavArg::class
)
@Composable
fun EIdQueueScreen(
    viewModel: EIdQueueViewModel,
) {
    val deadlineTime = viewModel.deadline.collectAsStateWithLifecycle().value

    EIdQueueScreenContent(
        onNext = viewModel::onNext,
        deadlineText = deadlineTime,
    )
}

@Composable
private fun EIdQueueScreenContent(
    onNext: () -> Unit,
    deadlineText: String,
) {
    WalletLayouts.ScrollableColumnWithPicture(
        stickyStartContent = {
            ScreenMainImage(
                iconRes = R.drawable.wallet_ic_queue_colored,
                backgroundColor = WalletTheme.colorScheme.surfaceContainerLow
            )
        },
        stickyBottomBackgroundColor = Color.Transparent,
        stickyBottomContent = {
            Buttons.FilledPrimary(
                text = stringResource(R.string.tk_global_continue),
                onClick = onNext,
            )
        }
    ) {
        Spacer(modifier = Modifier.height(Sizes.s06))
        WalletTexts.TitleScreen(
            text = stringResource(id = R.string.tk_getEid_queuing_title),
        )
        Spacer(modifier = Modifier.height(Sizes.s06))
        WalletTexts.BodyLarge(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(id = R.string.tk_getEid_queuing_body),
        )
        if (deadlineText.isNotEmpty()) {
            Spacer(modifier = Modifier.height(Sizes.s06))
            WalletTexts.BodyLarge(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(id = R.string.tk_getEid_queuing_body2_android),
            )
            WalletTexts.TitleMedium(
                modifier = Modifier.fillMaxWidth(),
                text = deadlineText,
            )
        }
    }
}

@WalletAllScreenPreview
@Composable
private fun EIdQueueScreenPreview() {
    WalletTheme {
        EIdQueueScreenContent(
            onNext = {},
            deadlineText = "25 January 2025"
        )
    }
}

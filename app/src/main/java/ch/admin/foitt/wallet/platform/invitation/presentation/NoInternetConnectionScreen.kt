package ch.admin.foitt.wallet.platform.invitation.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.LoadingOverlay
import ch.admin.foitt.wallet.platform.composables.SimpleScreenContent
import ch.admin.foitt.wallet.platform.navArgs.domain.model.NoInternetConnectionNavArg
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.theme.WalletTheme
import com.ramcosta.composedestinations.annotation.Destination

@Composable
@Destination(
    navArgsDelegate = NoInternetConnectionNavArg::class,
)
fun NoInternetConnectionScreen(
    viewModel: NoInternetConnectionViewModel,
) {
    NoInternetConnectionScreenContent(
        isLoading = viewModel.isLoading.collectAsStateWithLifecycle().value,
        onRetry = viewModel::retry,
        onClose = viewModel::close,
    )
}

@Composable
private fun NoInternetConnectionScreenContent(
    isLoading: Boolean,
    onRetry: () -> Unit,
    onClose: () -> Unit,
) {
    SimpleScreenContent(
        icon = R.drawable.pilot_ic_no_internet,
        titleText = stringResource(id = R.string.emptyState_offlineTitle),
        mainText = stringResource(id = R.string.emptyState_offlineMessage),
        bottomBlockContent = {
            Buttons.Outlined(
                text = stringResource(id = R.string.global_back_home),
                startIcon = painterResource(id = R.drawable.pilot_ic_back_button),
                onClick = onClose,
            )
            Buttons.FilledPrimary(
                text = stringResource(id = R.string.emptyState_offlineRetryButton),
                startIcon = painterResource(id = R.drawable.pilot_ic_retry),
                onClick = onRetry,
            )
        }
    )
    LoadingOverlay(showOverlay = isLoading)
}

@WalletAllScreenPreview
@Composable
private fun NoInternetConnectionScreenPreview() {
    WalletTheme {
        NoInternetConnectionScreenContent(
            isLoading = false,
            onRetry = {},
            onClose = {},
        )
    }
}

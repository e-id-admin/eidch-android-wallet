package ch.admin.foitt.wallet.feature.presentationRequest.presentation

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.ResultScreenContent
import ch.admin.foitt.wallet.platform.navArgs.domain.model.PresentationSuccessNavArg
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.theme.WalletTheme
import ch.admin.foitt.wallet.theme.successBackgroundDark
import ch.admin.foitt.wallet.theme.successBackgroundLight
import com.ramcosta.composedestinations.annotation.Destination

@Composable
@Destination(
    navArgsDelegate = PresentationSuccessNavArg::class,
)
fun PresentationSuccessScreen(viewModel: PresentationSuccessViewModel) {
    PresentationSuccessContent(
        dateTime = viewModel.dateTime,
        fields = viewModel.fields,
        onClose = viewModel::onClose,
    )
}

@Composable
private fun PresentationSuccessContent(
    dateTime: String,
    fields: List<String>,
    onClose: () -> Unit,
) {
    ResultScreenContent(
        iconRes = R.drawable.pilot_ic_checkmark_big,
        dateTime = dateTime,
        message = stringResource(id = R.string.presentation_result_title),
        topColor = MaterialTheme.colorScheme.successBackgroundLight,
        bottomColor = MaterialTheme.colorScheme.successBackgroundDark,
        bottomContent = {
            Buttons.TonalSecondary(
                text = stringResource(id = R.string.global_error_backToHome_button),
                onClick = onClose,
                endIcon = painterResource(id = R.drawable.pilot_ic_next_button),
            )
        },
        content = { SubmittedDataBox(fields = fields) },
    )
}

@Composable
@WalletAllScreenPreview
private fun PresentationSuccessPreview() {
    WalletTheme {
        PresentationSuccessContent(
            dateTime = "10th December 1990 | 11:17",
            fields = listOf("name", "firstname", "country", "age", "employment"),
            onClose = {},
        )
    }
}

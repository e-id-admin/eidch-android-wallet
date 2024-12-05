package ch.admin.foitt.wallet.feature.presentationRequest.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.presentation.layout.ScrollableColumn
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.navArgs.domain.model.PresentationValidationErrorNavArg
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme
import com.ramcosta.composedestinations.annotation.Destination

@Composable
@Destination(
    navArgsDelegate = PresentationValidationErrorNavArg::class,
)
fun PresentationValidationErrorScreen(viewModel: PresentationValidationErrorViewModel) {
    PresentationValidationErrorContent(
        fields = viewModel.fields,
        onClose = viewModel::onClose,
    )
}

@Composable
private fun PresentationValidationErrorContent(
    fields: List<String>,
    onClose: () -> Unit,
) = WalletLayouts.ScrollableColumn(
    stickyBottomContent = {
        Buttons.Outlined(
            text = stringResource(id = R.string.global_error_backToHome_button),
            endIcon = painterResource(id = R.drawable.pilot_ic_next_button),
            onClick = onClose,
        )
    },
    scrollableContent = { ScrollableContent(fields) },
)

@Composable
private fun ScrollableContent(fields: List<String>) {
    Column {
        Image(
            modifier = Modifier.fillMaxWidth(),
            painter = painterResource(id = R.drawable.pilote_ic_presentation_validation_error),
            contentDescription = null,
            alignment = Alignment.Center,
            contentScale = ContentScale.Fit,
        )
        Spacer(modifier = Modifier.height(Sizes.s04))
        WalletTexts.TitleScreen(
            text = stringResource(id = R.string.presentation_validationError_title),
        )
        Spacer(modifier = Modifier.height(Sizes.s06))
        WalletTexts.Body(
            text = stringResource(id = R.string.presentation_validationError_message),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(Sizes.s04))
        SubmittedDataBox(fields = fields)
    }
}

@Composable
@WalletAllScreenPreview
private fun PresentationValidationErrorPreview() {
    WalletTheme {
        PresentationValidationErrorContent(
            fields = listOf("name", "firstname", "country", "age", "employment"),
            onClose = {},
        )
    }
}

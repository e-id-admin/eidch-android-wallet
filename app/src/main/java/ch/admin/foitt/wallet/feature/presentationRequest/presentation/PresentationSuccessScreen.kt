package ch.admin.foitt.wallet.feature.presentationRequest.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.model.ActorUiState
import ch.admin.foitt.wallet.platform.credential.presentation.CredentialActionFeedbackCardSuccess
import ch.admin.foitt.wallet.platform.navArgs.domain.model.PresentationSuccessNavArg
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.theme.WalletTheme
import com.ramcosta.composedestinations.annotation.Destination

@Composable
@Destination(
    navArgsDelegate = PresentationSuccessNavArg::class,
)
fun PresentationSuccessScreen(viewModel: PresentationSuccessViewModel) {
    val verifierUiState = viewModel.verifierUiState.collectAsStateWithLifecycle().value

    PresentationSuccessContent(
        verifierUiState = verifierUiState,
        fields = viewModel.fields,
        onClose = viewModel::onClose,
    )
}

@Composable
private fun PresentationSuccessContent(
    verifierUiState: ActorUiState,
    fields: List<String>,
    onClose: () -> Unit,
) {
    CredentialActionFeedbackCardSuccess(
        issuer = verifierUiState,
        contentTextFirstParagraphText = R.string.tk_present_accept_title,
        iconAlwaysVisible = true,
        contentIcon = R.drawable.wallet_ic_check_circle_complete_thin,
        primaryButtonText = R.string.tk_global_close,
        onPrimaryButton = onClose,
        content = { SubmittedDataBoxTertiary(fields = fields) },
    )
}

@Composable
@WalletAllScreenPreview
private fun PresentationSuccessPreview() {
    WalletTheme {
        PresentationSuccessContent(
            verifierUiState = ActorUiState(
                name = "My Verfifier Name",
                painter = painterResource(id = R.drawable.ic_swiss_cross_small),
                trustStatus = TrustStatus.TRUSTED
            ),
            fields = listOf("name", "firstname", "country", "age", "employment"),
            onClose = {},
        )
    }
}

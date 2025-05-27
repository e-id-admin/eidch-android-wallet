package ch.admin.foitt.wallet.feature.presentationRequest.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorType
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.model.ActorUiState
import ch.admin.foitt.wallet.platform.credential.presentation.CredentialActionFeedbackCardError
import ch.admin.foitt.wallet.platform.navArgs.domain.model.PresentationInvalidCredentialErrorNavArg
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.theme.WalletTheme
import com.ramcosta.composedestinations.annotation.Destination

@Composable
@Destination(
    navArgsDelegate = PresentationInvalidCredentialErrorNavArg::class,
)
fun PresentationInvalidCredentialErrorScreen(viewModel: PresentationInvalidCredentialErrorViewModel) {
    val verifierUiState = viewModel.verifierUiState.collectAsStateWithLifecycle().value

    PresentationInvalidCredentialErrorContent(
        verifierUiState = verifierUiState,
        sentFields = viewModel.sentFields,
        onClose = viewModel::onClose,
    )
}

@Composable
private fun PresentationInvalidCredentialErrorContent(
    verifierUiState: ActorUiState,
    sentFields: List<String>,
    onClose: () -> Unit,
) {
    CredentialActionFeedbackCardError(
        issuer = verifierUiState,
        contentTextFirstParagraphText = R.string.tk_present_result_invalidCredential_primary,
        contentTextSecondParagraphText = R.string.tk_present_result_invalidCredential_secondary,
        contentIcon = R.drawable.wallet_ic_refused,
        iconAlwaysVisible = true,
        primaryButtonText = R.string.tk_global_close,
        onPrimaryButton = onClose,
        content = {
            SubmittedDataBox(
                fields = sentFields,
                tintColor = WalletTheme.colorScheme.onSurfaceVariant,
                backgroundColor = WalletTheme.colorScheme.surfaceContainerLow,
            )
        },
    )
}

@Composable
@WalletAllScreenPreview
private fun PresentationInvalidCredentialErrorPreview() {
    WalletTheme {
        PresentationInvalidCredentialErrorContent(
            verifierUiState = ActorUiState(
                name = "My Verifier Name",
                painter = painterResource(R.drawable.wallet_ic_error_general),
                trustStatus = TrustStatus.TRUSTED,
                actorType = ActorType.VERIFIER,
            ),
            sentFields = listOf("this field 01", "that field 02", "that other field 03"),
            onClose = {},
        )
    }
}

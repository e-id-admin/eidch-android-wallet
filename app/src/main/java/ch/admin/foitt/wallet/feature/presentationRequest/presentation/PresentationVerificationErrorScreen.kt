package ch.admin.foitt.wallet.feature.presentationRequest.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorType
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.model.ActorUiState
import ch.admin.foitt.wallet.platform.credential.presentation.CredentialActionFeedbackCardError
import ch.admin.foitt.wallet.platform.navArgs.domain.model.PresentationVerificationErrorNavArg
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.theme.WalletTheme
import com.ramcosta.composedestinations.annotation.Destination

@Composable
@Destination(
    navArgsDelegate = PresentationVerificationErrorNavArg::class,
)
fun PresentationVerificationErrorScreen(viewModel: PresentationVerificationErrorViewModel) {
    val verifierUiState = viewModel.verifierUiState.collectAsStateWithLifecycle().value

    PresentationVerificationErrorContent(
        verifierUiState = verifierUiState,
        onClose = viewModel::onClose,
    )
}

@Composable
private fun PresentationVerificationErrorContent(
    verifierUiState: ActorUiState,
    onClose: () -> Unit,
) {
    CredentialActionFeedbackCardError(
        issuer = verifierUiState,
        contentTextFirstParagraphText = R.string.tk_present_canceledverification_title,
        contentTextSecondParagraphText = R.string.tk_present_canceledverification_subtitle,
        contentIcon = R.drawable.wallet_ic_error_general,
        iconAlwaysVisible = true,
        primaryButtonText = R.string.tk_global_close,
        onPrimaryButton = onClose,
    )
}

@Composable
@WalletAllScreenPreview
private fun PresentationVerificationErrorPreview() {
    WalletTheme {
        PresentationVerificationErrorContent(
            verifierUiState = ActorUiState(
                name = "My Verifier Name",
                painter = painterResource(R.drawable.wallet_ic_error_general),
                trustStatus = TrustStatus.TRUSTED,
                actorType = ActorType.VERIFIER,
            ),
            onClose = {},
        )
    }
}

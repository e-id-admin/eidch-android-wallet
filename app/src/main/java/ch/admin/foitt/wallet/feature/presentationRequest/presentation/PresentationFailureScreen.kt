package ch.admin.foitt.wallet.feature.presentationRequest.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorType
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.model.ActorUiState
import ch.admin.foitt.wallet.platform.credential.presentation.CredentialActionFeedbackCardError
import ch.admin.foitt.wallet.platform.navArgs.domain.model.PresentationFailureNavArg
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.theme.WalletTheme
import com.ramcosta.composedestinations.annotation.Destination

@Composable
@Destination(
    navArgsDelegate = PresentationFailureNavArg::class,
)
fun PresentationFailureScreen(viewModel: PresentationFailureViewModel) {
    val verifierUiState = viewModel.verifierUiState.collectAsStateWithLifecycle().value

    PresentationFailureContent(
        verifierUiState = verifierUiState,
        onRetry = viewModel::onRetry,
        onClose = viewModel::onClose,
    )
}

@Composable
private fun PresentationFailureContent(
    verifierUiState: ActorUiState,
    onRetry: () -> Unit,
    onClose: () -> Unit,
) {
    CredentialActionFeedbackCardError(
        issuer = verifierUiState,
        contentTextFirstParagraphText = R.string.tk_present_error_title,
        contentTextSecondParagraphText = R.string.tk_present_error_subtitle,
        iconAlwaysVisible = true,
        contentIcon = R.drawable.wallet_ic_error_general,
        primaryButtonText = R.string.tk_global_repeat_primarybutton,
        secondaryButtonText = R.string.tk_global_cancel,
        onPrimaryButton = onRetry,
        onSecondaryButton = onClose,
    )
}

@Composable
@WalletAllScreenPreview
private fun PresentationFailurePreview() {
    WalletTheme {
        PresentationFailureContent(
            verifierUiState = ActorUiState(
                name = "My Verfifier Name",
                painter = painterResource(id = R.drawable.ic_swiss_cross_small),
                trustStatus = TrustStatus.TRUSTED,
                actorType = ActorType.VERIFIER,
            ),
            onRetry = {},
            onClose = {},
        )
    }
}

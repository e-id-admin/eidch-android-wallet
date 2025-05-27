package ch.admin.foitt.wallet.feature.presentationRequest.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorType
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.model.ActorUiState
import ch.admin.foitt.wallet.platform.credential.presentation.CredentialActionFeedbackCard
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.theme.WalletTheme
import com.ramcosta.composedestinations.annotation.Destination

@Destination
@Composable
fun PresentationDeclinedScreen(viewModel: PresentationDeclinedViewModel) {
    val verifierUiState = viewModel.verifierUiState.collectAsStateWithLifecycle().value

    PresentationDeclinedContent(
        verifierUiState = verifierUiState,
        onBack = viewModel::onBack,
    )
}

@Composable
private fun PresentationDeclinedContent(
    verifierUiState: ActorUiState,
    onBack: () -> Unit,
) {
    CredentialActionFeedbackCard(
        issuer = verifierUiState,
        contentTextFirstParagraphText = R.string.tk_present_result_declined_primary,
        iconAlwaysVisible = true,
        contentIcon = R.drawable.wallet_ic_circular_cross,
        primaryButtonText = R.string.tk_global_close,
        onPrimaryButton = onBack,
    )
}

@WalletAllScreenPreview
@Composable
private fun PresentationDeclinedPreview() {
    WalletTheme {
        PresentationDeclinedContent(
            onBack = {},
            verifierUiState = ActorUiState(
                name = "My Verfifier Name",
                painter = painterResource(id = R.drawable.ic_swiss_cross_small),
                trustStatus = TrustStatus.TRUSTED,
                actorType = ActorType.VERIFIER,
            ),
        )
    }
}

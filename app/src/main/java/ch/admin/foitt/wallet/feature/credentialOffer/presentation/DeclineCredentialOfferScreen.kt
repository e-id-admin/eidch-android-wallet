package ch.admin.foitt.wallet.feature.credentialOffer.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorType
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.model.ActorUiState
import ch.admin.foitt.wallet.platform.credential.presentation.CredentialActionFeedbackCard
import ch.admin.foitt.wallet.platform.navArgs.domain.model.DeclineCredentialOfferNavArg
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.theme.WalletTheme
import com.ramcosta.composedestinations.annotation.Destination

@Destination(
    navArgsDelegate = DeclineCredentialOfferNavArg::class,
)
@Composable
fun DeclineCredentialOfferScreen(
    viewModel: DeclineCredentialOfferViewModel,
) {
    DeclineCredentialOfferScreenContent(
        isLoading = viewModel.isLoading.collectAsStateWithLifecycle().value,
        issuer = viewModel.uiState.collectAsStateWithLifecycle().value.issuer,
        onCancel = viewModel::onCancel,
        onDecline = viewModel::onDecline,
    )
}

@Composable
private fun DeclineCredentialOfferScreenContent(
    isLoading: Boolean,
    issuer: ActorUiState,
    onCancel: () -> Unit,
    onDecline: () -> Unit,
) {
    CredentialActionFeedbackCard(
        isLoading = isLoading,
        issuer = issuer,
        contentTextFirstParagraphText = R.string.tk_receive_deny1_title,
        contentTextSecondParagraphText = R.string.tk_receive_deny1_body,
        iconAlwaysVisible = false,
        contentIcon = R.drawable.wallet_ic_circular_questionmark,
        primaryButtonText = R.string.tk_receive_deny1_primarybutton,
        secondaryButtonText = R.string.tk_global_cancel,
        onPrimaryButton = onDecline,
        onSecondaryButton = onCancel
    )
}

@WalletAllScreenPreview
@Composable
private fun DeclineCredentialOfferScreenContentPreview() {
    WalletTheme {
        DeclineCredentialOfferScreenContent(
            isLoading = false,
            issuer = ActorUiState(
                name = "Test Issuer",
                painter = painterResource(id = R.drawable.wallet_ic_scan_person),
                trustStatus = TrustStatus.TRUSTED,
                actorType = ActorType.ISSUER,
            ),
            onCancel = {},
            onDecline = {},
        )
    }
}

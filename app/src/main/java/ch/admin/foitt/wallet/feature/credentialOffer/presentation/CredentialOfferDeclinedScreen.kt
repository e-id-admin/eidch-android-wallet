package ch.admin.foitt.wallet.feature.credentialOffer.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorType
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.model.ActorUiState
import ch.admin.foitt.wallet.platform.credential.presentation.CredentialActionFeedbackCard
import ch.admin.foitt.wallet.platform.navArgs.domain.model.CredentialOfferDeclinedNavArg
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.theme.WalletTheme
import com.ramcosta.composedestinations.annotation.Destination

@Destination(
    navArgsDelegate = CredentialOfferDeclinedNavArg::class,
)
@Composable
fun CredentialOfferDeclinedScreen(
    viewModel: CredentialOfferDeclinedViewModel,
) {
    BackHandler {
        viewModel.navigateToHome()
    }

    CredentialOfferDeclinedScreenContent(
        isLoading = viewModel.isLoading.collectAsStateWithLifecycle().value,
        issuer = viewModel.uiState.collectAsStateWithLifecycle().value.issuer,
    )
}

@Composable
private fun CredentialOfferDeclinedScreenContent(
    isLoading: Boolean,
    issuer: ActorUiState,
) {
    CredentialActionFeedbackCard(
        isLoading = isLoading,
        issuer = issuer,
        contentTextFirstParagraphText = R.string.tk_receive_declinedOffer_primary,
        iconAlwaysVisible = true,
        contentIcon = R.drawable.wallet_ic_circular_cross,
    )
}

@WalletAllScreenPreview
@Composable
private fun CredentialOfferDeclinedScreenContentPreview() {
    WalletTheme {
        CredentialOfferDeclinedScreenContent(
            isLoading = false,
            issuer = ActorUiState(
                name = "Test Issuer",
                painter = painterResource(id = R.drawable.wallet_ic_scan_person),
                trustStatus = TrustStatus.TRUSTED,
                actorType = ActorType.ISSUER,
            ),
        )
    }
}

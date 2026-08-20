package ch.admin.foitt.wallet.feature.presentationRequest.presentation

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorType
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.model.ActorUiState
import ch.admin.foitt.wallet.platform.badges.presentation.BadgeBottomSheet
import ch.admin.foitt.wallet.platform.credential.presentation.CredentialActionFeedbackCard
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ActorComplianceState
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VcSchemaTrustStatus
import ch.admin.foitt.wallet.theme.WalletTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresentationDeclinedScreen(viewModel: PresentationDeclinedViewModel) {
    val badgeBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val badgeBottomSheet = viewModel.badgeBottomSheet.collectAsStateWithLifecycle().value
    if (badgeBottomSheet != null) {
        BadgeBottomSheet(
            sheetState = badgeBottomSheetState,
            badgeBottomSheetUiState = badgeBottomSheet,
            onDismiss = viewModel::onDismissBottomSheet
        )
    }
    PresentationDeclinedContent(
        verifierUiState = viewModel.verifierUiState.collectAsStateWithLifecycle().value,
        redirectUri = viewModel.redirectUri,
        onBack = viewModel::onBack,
        onActorNameTap = viewModel::onActorNameTap,
        onReportedActorInfo = viewModel::onReportedActorInfo
    )
}

@Composable
private fun PresentationDeclinedContent(
    verifierUiState: ActorUiState,
    redirectUri: String?,
    onBack: () -> Unit,
    onActorNameTap: () -> Unit,
    onReportedActorInfo: () -> Unit,
) {
    CredentialActionFeedbackCard(
        issuer = verifierUiState,
        contentTextFirstParagraphText = if (redirectUri == null) {
            stringResource(R.string.tk_present_result_declined_primary)
        } else {
            stringResource(R.string.tk_present_result_declined_redirect_primary, verifierName(verifierUiState))
        },
        iconAlwaysVisible = true,
        contentIcon = R.drawable.wallet_ic_circular_cross,
        primaryButtonText = if (redirectUri == null) {
            stringResource(R.string.tk_global_close)
        } else {
            stringResource(R.string.tk_global_close_redirect, verifierName(verifierUiState))
        },
        onPrimaryButton = onBack,
        onActorNameTap = onActorNameTap,
        onReportedActorInfo = onReportedActorInfo
    )
}

@Composable
private fun verifierName(
    verifierUiState: ActorUiState
) = verifierUiState.name ?: stringResource(R.string.presentation_verifier_name_unknown)

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
                vcSchemaTrustStatus = VcSchemaTrustStatus.TRUSTED,
                actorType = ActorType.VERIFIER,
                actorComplianceState = ActorComplianceState.REPORTED,
                nonComplianceReason = "report reason",
            ),
            redirectUri = "redirectUri",
            onActorNameTap = {},
            onReportedActorInfo = {}
        )
    }
}

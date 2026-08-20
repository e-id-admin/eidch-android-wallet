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
import ch.admin.foitt.wallet.platform.credential.presentation.CredentialActionFeedbackCardError
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ActorComplianceState
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VcSchemaTrustStatus
import ch.admin.foitt.wallet.theme.WalletTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresentationVerificationErrorScreen(viewModel: PresentationVerificationErrorViewModel) {
    val badgeBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val badgeBottomSheet = viewModel.badgeBottomSheet.collectAsStateWithLifecycle().value
    if (badgeBottomSheet != null) {
        BadgeBottomSheet(
            sheetState = badgeBottomSheetState,
            badgeBottomSheetUiState = badgeBottomSheet,
            onDismiss = viewModel::onDismissBottomSheet
        )
    }
    PresentationVerificationErrorContent(
        verifierUiState = viewModel.verifierUiState.collectAsStateWithLifecycle().value,
        onClose = viewModel::onClose,
        onActorNameTap = viewModel::onActorNameTap,
        onReportedActorInfo = viewModel::onReportedActorInfo
    )
}

@Composable
private fun PresentationVerificationErrorContent(
    verifierUiState: ActorUiState,
    onClose: () -> Unit,
    onActorNameTap: () -> Unit,
    onReportedActorInfo: () -> Unit,
) {
    CredentialActionFeedbackCardError(
        issuer = verifierUiState,
        contentTextFirstParagraphText = stringResource(R.string.tk_present_result_canceledVerification_primary),
        contentTextSecondParagraphText = stringResource(R.string.tk_present_result_canceledVerification_secondary),
        contentIcon = R.drawable.wallet_ic_error_general,
        iconAlwaysVisible = true,
        primaryButtonText = stringResource(R.string.tk_global_close),
        onPrimaryButton = onClose,
        onActorNameTap = onActorNameTap,
        onReportedActorInfo = onReportedActorInfo
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
                vcSchemaTrustStatus = VcSchemaTrustStatus.TRUSTED,
                actorType = ActorType.VERIFIER,
                actorComplianceState = ActorComplianceState.REPORTED,
                nonComplianceReason = "report reason",
            ),
            onClose = {},
            onActorNameTap = {},
            onReportedActorInfo = {}
        )
    }
}

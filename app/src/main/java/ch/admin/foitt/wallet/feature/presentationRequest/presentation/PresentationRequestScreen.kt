package ch.admin.foitt.wallet.feature.presentationRequest.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.InvitationHeader
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.model.ActorUiState
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.LoadingOverlay
import ch.admin.foitt.wallet.platform.composables.SpacerBottom
import ch.admin.foitt.wallet.platform.composables.SpacerTop
import ch.admin.foitt.wallet.platform.credential.presentation.CredentialClaimsScreenContent
import ch.admin.foitt.wallet.platform.credential.presentation.mock.CredentialMocks
import ch.admin.foitt.wallet.platform.credential.presentation.model.CredentialCardState
import ch.admin.foitt.wallet.platform.navArgs.domain.model.PresentationRequestNavArg
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimData
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTheme
import com.ramcosta.composedestinations.annotation.Destination

@Destination(
    navArgsDelegate = PresentationRequestNavArg::class
)
@Composable
fun PresentationRequestScreen(viewModel: PresentationRequestViewModel) {
    BackHandler(onBack = viewModel::onDecline)

    val presentationRequestUiState = viewModel.presentationRequestUiState.collectAsStateWithLifecycle().value
    val verifierUiState = viewModel.verifierUiState.collectAsStateWithLifecycle().value

    PresentationRequestContent(
        verifierUiState = verifierUiState,
        requestedClaims = presentationRequestUiState.requestedClaims,
        credentialCardState = presentationRequestUiState.credential,
        isLoading = viewModel.isLoading.collectAsStateWithLifecycle().value,
        isSubmitting = viewModel.isSubmitting.collectAsStateWithLifecycle().value,
        onSubmit = viewModel::submit,
        onDecline = viewModel::onDecline,
    )
}

@Composable
private fun PresentationRequestContent(
    verifierUiState: ActorUiState,
    requestedClaims: List<CredentialClaimData>,
    credentialCardState: CredentialCardState,
    isLoading: Boolean,
    isSubmitting: Boolean,
    onSubmit: () -> Unit,
    onDecline: () -> Unit,
) = Box(modifier = Modifier.fillMaxSize()) {
    SpacerTop(
        backgroundColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.align(Alignment.TopStart),
        useStatusBarInsets = true,
    )
    CredentialClaimsScreenContent(
        topContent = {
            Spacer(modifier = Modifier.height(Sizes.s06))
            InvitationHeader(
                inviterName = verifierUiState.name,
                inviterImage = verifierUiState.painter,
                trustStatus = verifierUiState.trustStatus,
                modifier = Modifier
                    .statusBarsPadding()
                    .padding(horizontal = Sizes.s04)
            )
            Spacer(modifier = Modifier.height(Sizes.s06))
        },
        bottomContent = {
            PresentationRequestButtons(
                onAccept = onSubmit,
                onDecline = onDecline,
                isSubmitting = isSubmitting,
            )
        },
        title = R.string.presentation_attributes_title,
        claims = requestedClaims,
        credentialCardState = credentialCardState,
    )
    SpacerBottom(
        backgroundColor = MaterialTheme.colorScheme.background,
        modifier = Modifier.align(Alignment.BottomStart),
        useNavigationBarInsets = true,
    )

    LoadingOverlay(showOverlay = isLoading)
}

@Composable
private fun PresentationRequestButtons(
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    isSubmitting: Boolean,
) = Column(
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = Sizes.s04)
) {
    Spacer(modifier = Modifier.height(Sizes.s10))
    Buttons.Outlined(
        text = stringResource(id = R.string.presentation_deny_button_text),
        startIcon = painterResource(id = R.drawable.pilot_ic_cross),
        enabled = !isSubmitting,
        onClick = onDecline,
    )
    Spacer(modifier = Modifier.size(Sizes.s04))
    Buttons.FilledTertiary(
        text = stringResource(id = R.string.presentation_accept_button_text),
        onClick = onAccept,
        startIcon = painterResource(id = R.drawable.pilot_ic_checkmark_button),
        isActive = isSubmitting,
        enabled = !isSubmitting,
        activeText = stringResource(id = R.string.presentation_send_button_text)
    )
    Spacer(
        modifier = Modifier
            .padding(bottom = Sizes.s06)
            .navigationBarsPadding()
    )
}

@WalletAllScreenPreview
@Composable
private fun PresentationRequestScreenPreview() {
    WalletTheme {
        PresentationRequestContent(
            verifierUiState = ActorUiState(
                name = "My Verfifier Name",
                painter = painterResource(id = R.drawable.pilot_ic_strassenverkehrsamt),
                trustStatus = TrustStatus.TRUSTED
            ),
            requestedClaims = CredentialMocks.claimList,
            isLoading = false,
            onSubmit = {},
            onDecline = {},
            credentialCardState = CredentialMocks.cardState01,
            isSubmitting = false,
        )
    }
}

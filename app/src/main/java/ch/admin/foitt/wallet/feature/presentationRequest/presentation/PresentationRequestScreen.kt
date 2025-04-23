package ch.admin.foitt.wallet.feature.presentationRequest.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowWidthSizeClass
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorType
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.InvitationHeader
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.model.ActorUiState
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.LoadingOverlay
import ch.admin.foitt.wallet.platform.composables.presentation.layout.ScrollableColumnSimple
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.composables.presentation.verticalSafeDrawing
import ch.admin.foitt.wallet.platform.credential.presentation.CredentialCardSmall
import ch.admin.foitt.wallet.platform.credential.presentation.CredentialClaimItemsColumn
import ch.admin.foitt.wallet.platform.credential.presentation.MediumCredentialBox
import ch.admin.foitt.wallet.platform.credential.presentation.mock.CredentialMocks
import ch.admin.foitt.wallet.platform.credential.presentation.model.CredentialCardState
import ch.admin.foitt.wallet.platform.navArgs.domain.model.PresentationRequestNavArg
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.ssi.domain.model.CredentialClaimData
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
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
        showDelayReason = viewModel.showDelayReason.collectAsStateWithLifecycle().value,
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
    showDelayReason: Boolean,
) = Box(modifier = Modifier.fillMaxSize()) {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    if (windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT) {
        CompactContent(
            verifierUiState = verifierUiState,
            requestedClaims = requestedClaims,
            credentialCardState = credentialCardState,
            onSubmit = onSubmit,
            onDecline = onDecline,
            isSubmitting = isSubmitting,
            showDelayReason = showDelayReason
        )
    } else {
        LargeContent(
            verifierUiState = verifierUiState,
            requestedClaims = requestedClaims,
            credentialCardState = credentialCardState,
            onSubmit = onSubmit,
            onDecline = onDecline,
            isSubmitting = isSubmitting,
            showDelayReason = showDelayReason
        )
    }
    LoadingOverlay(showOverlay = isLoading)
}

@Composable
private fun CompactContent(
    verifierUiState: ActorUiState,
    requestedClaims: List<CredentialClaimData>,
    credentialCardState: CredentialCardState,
    onSubmit: () -> Unit,
    onDecline: () -> Unit,
    isSubmitting: Boolean,
    showDelayReason: Boolean,
) {
    if (isSubmitting) {
        IsSubmittingCompact(verifierUiState, credentialCardState, showDelayReason)
    } else {
        WalletLayouts.ScrollableColumnSimple(
            modifier = Modifier.fillMaxWidth(),
            useCompactScalable = false,
            isStickyStartScrollable = true,
            stickyStartContent = {
                Spacer(modifier = Modifier.height(Sizes.s06))
                InvitationHeader(verifierUiState = verifierUiState)
            },
            stickyBottomHorizontalArrangement = Arrangement.spacedBy(Sizes.s02, Alignment.CenterHorizontally),
            stickyBottomContent = {
                StickyButtons(
                    onDecline,
                    onSubmit,
                )
            },
        ) {
            MediumCredentialBox(
                credentialCardState = credentialCardState,
            )
            Spacer(modifier = Modifier.height(Sizes.s04))
            CredentialClaimItemsColumn(
                R.string.tk_present_approval_subtitle_affectedDetails_android,
                requestedClaims,
            )
        }
    }
}

@Composable
private fun LargeContent(
    verifierUiState: ActorUiState,
    requestedClaims: List<CredentialClaimData>,
    credentialCardState: CredentialCardState,
    onSubmit: () -> Unit,
    onDecline: () -> Unit,
    isSubmitting: Boolean,
    showDelayReason: Boolean,
) {
    WalletLayouts.ScrollableColumnSimple(
        modifier = Modifier.fillMaxWidth(),
        isStickyStartScrollable = true,
        stickyStartContent = {
            MediumCredentialBox(
                modifier = Modifier
                    .padding(top = Sizes.s06, start = Sizes.s04, end = Sizes.s04)
                    .verticalSafeDrawing(),
                credentialCardState = credentialCardState,
            )
        },
        stickyBottomHorizontalArrangement = Arrangement.spacedBy(Sizes.s02, Alignment.CenterHorizontally),
        stickyBottomContent = {
            StickyButtons(
                onDecline,
                onSubmit,
            )
        },
        centerContent = isSubmitting,
    ) {
        if (isSubmitting) {
            LoadingIndicator(
                modifier = Modifier.fillMaxSize(),
                showDelayReason = showDelayReason,
            )
        } else {
            Spacer(modifier = Modifier.height(Sizes.s06))
            InvitationHeader(verifierUiState = verifierUiState)
            CredentialClaimItemsColumn(
                R.string.tk_present_approval_subtitle_affectedDetails_android,
                requestedClaims,
            )
        }
    }
}

@Composable
private fun InvitationHeader(modifier: Modifier = Modifier, verifierUiState: ActorUiState) {
    InvitationHeader(
        inviterName = verifierUiState.name,
        inviterImage = verifierUiState.painter,
        trustStatus = verifierUiState.trustStatus,
        actorType = verifierUiState.actorType,
        modifier = modifier
            .statusBarsPadding()
            .padding(horizontal = Sizes.s04)
    )
    Spacer(modifier = Modifier.height(Sizes.s06))
}

@Composable
private fun IsSubmittingCompact(
    verifierUiState: ActorUiState,
    credentialCardState: CredentialCardState,
    showDelayReason: Boolean,
) {
    Column {
        Spacer(Modifier.windowInsetsTopHeight(WindowInsets.safeDrawing))
        InvitationHeader(
            verifierUiState = verifierUiState,
            modifier = Modifier
                .padding(bottom = Sizes.s06),
        )
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = Sizes.boxCornerSize, topEnd = Sizes.boxCornerSize))
                .fillMaxSize()
                .background(WalletTheme.colorScheme.surfaceContainerLow),
            contentAlignment = Alignment.Center
        ) {
            ConstraintLayout(modifier = Modifier.fillMaxSize()) {
                val (
                    credentialCard,
                    loading,
                ) = createRefs()

                CredentialCardSmall(
                    credentialState = credentialCardState,
                    modifier = Modifier
                        .constrainAs(credentialCard) {
                            top.linkTo(parent.top)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            bottom.linkTo(parent.bottom)
                        },
                )

                LoadingIndicator(
                    showDelayReason = showDelayReason,
                    modifier = Modifier
                        .constrainAs(loading) {
                            top.linkTo(credentialCard.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            bottom.linkTo(parent.bottom)
                        }
                        .padding(top = Sizes.s06)
                )
            }
        }
    }
}

@Composable
private fun LoadingIndicator(
    modifier: Modifier = Modifier,
    showDelayReason: Boolean
) = Box(
    modifier = modifier,
    contentAlignment = Alignment.BottomCenter,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            color = WalletTheme.colorScheme.primary,
            modifier = Modifier
                .padding(bottom = Sizes.s02)
                .size(Sizes.s12),
            strokeWidth = Sizes.line02,
        )
        if (showDelayReason) {
            WalletTexts.Body(text = stringResource(R.string.tk_global_pleasewait))
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun StickyButtons(
    onDecline: () -> Unit,
    onAccept: () -> Unit,
) {
    FlowRow(
        modifier = Modifier
            .clip(RoundedCornerShape(corner = CornerSize(Sizes.s16)))
            .background(WalletTheme.colorScheme.background)
            .padding(vertical = Sizes.s03, horizontal = Sizes.s04)
            .focusGroup(),
        horizontalArrangement = Arrangement.spacedBy(Sizes.s02, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(Sizes.s02, Alignment.Bottom),
        maxItemsInEachRow = 2,
    ) {
        Buttons.FilledPrimary(
            text = stringResource(id = R.string.tk_receive_credentialOffer_button_decline),
            startIcon = painterResource(id = R.drawable.wallet_ic_cross),
            onClick = onDecline,
        )
        Buttons.FilledTertiary(
            text = stringResource(id = R.string.tk_global_allow_primarybutton),
            startIcon = painterResource(id = R.drawable.wallet_ic_checkmark),
            onClick = onAccept,
        )
    }
}

@WalletAllScreenPreview
@Composable
private fun PresentationRequestScreenPreview() {
    WalletTheme {
        PresentationRequestContent(
            verifierUiState = ActorUiState(
                name = "My Verfifier Name",
                painter = painterResource(id = R.drawable.ic_swiss_cross_small),
                trustStatus = TrustStatus.TRUSTED,
                actorType = ActorType.VERIFIER,
            ),
            requestedClaims = CredentialMocks.claimList,
            isLoading = false,
            onSubmit = {},
            onDecline = {},
            credentialCardState = CredentialMocks.cardState01,
            isSubmitting = false,
            showDelayReason = false,
        )
    }
}

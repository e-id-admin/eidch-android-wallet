package ch.admin.foitt.wallet.feature.presentationRequest.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorType
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.InvitationHeader
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.model.ActorUiState
import ch.admin.foitt.wallet.platform.badges.presentation.BadgeBottomSheet
import ch.admin.foitt.wallet.platform.composables.AdaptiveBottomButtonBar
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.LoadingOverlay
import ch.admin.foitt.wallet.platform.composables.presentation.HeightReportingLayout
import ch.admin.foitt.wallet.platform.composables.presentation.layout.LazyColumn
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.credential.presentation.credentialCardListItems
import ch.admin.foitt.wallet.platform.credential.presentation.mock.CredentialMocks
import ch.admin.foitt.wallet.platform.credential.presentation.model.CredentialCardState
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.ActorComplianceState
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.VcSchemaTrustStatus
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresentationCredentialListScreen(viewModel: PresentationCredentialListViewModel) {
    val presentationCredentialListUiState = viewModel.presentationCredentialListUiState.stateFlow.collectAsStateWithLifecycle().value
    val verifierUiState = viewModel.verifierUiState.collectAsStateWithLifecycle().value

    val uiMode = LocalConfiguration.current.uiMode
    LaunchedEffect(uiMode) {
        viewModel.presentationCredentialListUiState.refreshData()
    }
    val badgeBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val badgeBottomSheet = viewModel.badgeBottomSheet.collectAsStateWithLifecycle().value
    if (badgeBottomSheet != null) {
        BadgeBottomSheet(
            sheetState = badgeBottomSheetState,
            badgeBottomSheetUiState = badgeBottomSheet,
            onDismiss = viewModel::onDismissBottomSheet
        )
    }
    PresentationCredentialListScreenContent(
        verifierUiState = verifierUiState,
        credentialCardStates = presentationCredentialListUiState.credentials,
        isLoading = viewModel.isLoading.collectAsStateWithLifecycle().value,
        onCredentialSelected = viewModel::onCredentialSelected,
        onBack = viewModel::onBack,
        onActorNameTap = viewModel::onActorNameTap,
        onReportedActorInfo = viewModel::onReportedActorInfo
    )
}

@Composable
private fun PresentationCredentialListScreenContent(
    verifierUiState: ActorUiState,
    credentialCardStates: List<CredentialCardState>,
    isLoading: Boolean,
    onCredentialSelected: (Long) -> Unit,
    onBack: () -> Unit,
    onActorNameTap: () -> Unit,
    onReportedActorInfo: () -> Unit
) {
    var bottomHeightDp by remember { mutableStateOf(0.dp) }

    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(WalletTheme.colorScheme.surfaceContainerLow)
    ) {
        val (
            mainContentRef,
            buttonRef,
        ) = createRefs()

        CompactCredentialList(
            modifier = Modifier
                .fillMaxWidth()
                .constrainAs(mainContentRef) {
                    top.linkTo(parent.top)
                    bottom.linkTo(parent.bottom)
                    height = Dimension.fillToConstraints
                },
            contentPadding = PaddingValues(bottom = Sizes.s04 + bottomHeightDp),
            credentialStates = credentialCardStates,
            onCredentialSelected = onCredentialSelected,
            headerContent = {
                ListHeader(
                    verifierUiState = verifierUiState,
                    onActorNameTap = onActorNameTap,
                    onReportedActorInfo = onReportedActorInfo
                )
            },
        )

        CancelButton(
            onBack = onBack,
            onHeightMeasured = { bottomHeightDp = it },
            modifier = Modifier
                .constrainAs(buttonRef) {
                    bottom.linkTo(parent.bottom)
                    end.linkTo(parent.end)
                    start.linkTo(parent.start)
                },
        )

        LoadingOverlay(showOverlay = isLoading)
    }
}

@Composable
private fun ListHeader(
    verifierUiState: ActorUiState,
    onActorNameTap: () -> Unit,
    onReportedActorInfo: () -> Unit
) {
    InvitationHeader(
        actorUiState = verifierUiState,
        onActorNameTap = onActorNameTap,
        onReportedActorInfo = onReportedActorInfo
    )
    Spacer(modifier = Modifier.height(Sizes.s04))
    WalletTexts.BodyLarge(
        modifier = Modifier
            .padding(start = Sizes.s04, end = Sizes.s04),
        text = stringResource(id = R.string.tk_present_compatibleCredentials_primary)
    )
    Spacer(modifier = Modifier.height(Sizes.s04))
}

@Composable
private fun CompactCredentialList(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    credentialStates: List<CredentialCardState>,
    onCredentialSelected: (Long) -> Unit,
    headerContent: @Composable () -> Unit,
) {
    WalletLayouts.LazyColumn(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
        useTopInsets = false,
        contentPadding = contentPadding,
    ) {
        item {
            headerContent()
        }

        credentialCardListItems(
            credentialCardStates = credentialStates,
            paddingValues = PaddingValues(horizontal = Sizes.s04),
            onClick = { state -> onCredentialSelected(state.credentialId) },
        )
    }
}

@Composable
private fun CancelButton(
    onBack: () -> Unit,
    onHeightMeasured: (Dp) -> Unit,
    modifier: Modifier = Modifier,
) {
    HeightReportingLayout(
        modifier = modifier,
        onContentHeightMeasured = onHeightMeasured,
    ) {
        AdaptiveBottomButtonBar(
            buttons = listOf(
                {
                    Buttons.FilledPrimary(
                        text = stringResource(R.string.global_cancel),
                        onClick = onBack,
                        startIcon = painterResource(R.drawable.wallet_ic_cross),
                    )
                }
            )
        )
    }
}

@WalletAllScreenPreview
@Composable
private fun PresentationCredentialListScreenPreview() {
    WalletTheme {
        PresentationCredentialListScreenContent(
            verifierUiState = ActorUiState(
                name = "My verifier name",
                painter = painterResource(R.drawable.ic_swiss_cross_small),
                trustStatus = TrustStatus.TRUSTED,
                vcSchemaTrustStatus = VcSchemaTrustStatus.TRUSTED,
                actorType = ActorType.VERIFIER,
                actorComplianceState = ActorComplianceState.REPORTED,
                nonComplianceReason = "report reason",
            ),
            credentialCardStates = CredentialMocks.cardStates.toList().map { it.value() },
            isLoading = false,
            onCredentialSelected = {},
            onBack = {},
            onReportedActorInfo = {},
            onActorNameTap = {}
        )
    }
}

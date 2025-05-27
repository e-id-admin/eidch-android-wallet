package ch.admin.foitt.wallet.feature.presentationRequest.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.LoadingOverlay
import ch.admin.foitt.wallet.platform.composables.presentation.HeightReportingLayout
import ch.admin.foitt.wallet.platform.composables.presentation.layout.LazyColumn
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.credential.presentation.CredentialListRow
import ch.admin.foitt.wallet.platform.credential.presentation.mock.CredentialMocks
import ch.admin.foitt.wallet.platform.credential.presentation.model.CredentialCardState
import ch.admin.foitt.wallet.platform.navArgs.domain.model.PresentationCredentialListNavArg
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme
import com.ramcosta.composedestinations.annotation.Destination

@Destination(
    navArgsDelegate = PresentationCredentialListNavArg::class,
)
@Composable
fun PresentationCredentialListScreen(viewModel: PresentationCredentialListViewModel) {
    val presentationCredentialListUiState = viewModel.presentationCredentialListUiState.collectAsStateWithLifecycle().value
    val verifierUiState = viewModel.verifierUiState.collectAsStateWithLifecycle().value

    PresentationCredentialListScreenContent(
        verifierUiState = verifierUiState,
        credentialCardStates = presentationCredentialListUiState.credentials,
        isLoading = viewModel.isLoading.collectAsStateWithLifecycle().value,
        onCredentialSelected = viewModel::onCredentialSelected,
        onBack = viewModel::onBack,
    )
}

@Composable
private fun PresentationCredentialListScreenContent(
    verifierUiState: ActorUiState,
    credentialCardStates: List<CredentialCardState>,
    isLoading: Boolean,
    onCredentialSelected: (Int) -> Unit,
    onBack: () -> Unit,
) {
    val bottomHeightDp = remember { mutableStateOf(0.dp) }

    ConstraintLayout(
        modifier = Modifier.fillMaxSize()
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
            contentPadding = PaddingValues(bottom = Sizes.s06 + bottomHeightDp.value),
            credentialStates = credentialCardStates,
            onCredentialSelected = onCredentialSelected,
            headerContent = {
                ListHeader(verifierUiState = verifierUiState)
            },
        )
        CancelButton(
            modifier = Modifier
                .padding(bottom = Sizes.s06)
                .constrainAs(buttonRef) {
                    bottom.linkTo(parent.bottom, margin = Sizes.s04)
                    end.linkTo(parent.end)
                    start.linkTo(parent.start)
                },
            onBack = onBack,
            stickyBottomHeight = bottomHeightDp
        )
        LoadingOverlay(showOverlay = isLoading)
    }
}

@Composable
private fun ListHeader(verifierUiState: ActorUiState) {
    Spacer(modifier = Modifier.height(Sizes.s06))
    InvitationHeader(
        modifier = Modifier
            .padding(start = Sizes.s04, end = Sizes.s04),
        inviterName = verifierUiState.name,
        inviterImage = verifierUiState.painter,
        trustStatus = verifierUiState.trustStatus,
        actorType = verifierUiState.actorType,
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
    onCredentialSelected: (Int) -> Unit,
    headerContent: @Composable () -> Unit,
) {
    WalletLayouts.LazyColumn(
        modifier = modifier
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
        contentPadding = contentPadding,
    ) {
        item {
            headerContent()
        }
        itemsIndexed(credentialStates) { index, state ->
            CredentialListRow(
                onClick = { onCredentialSelected(index) },
                credentialState = state,
                showDivider = index != credentialStates.lastIndex,
            )
        }
    }
}

@Composable
private fun CancelButton(
    modifier: Modifier = Modifier,
    stickyBottomHeight: MutableState<Dp>,
    onBack: () -> Unit,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomCenter,
    ) {
        HeightReportingLayout(
            modifier = modifier,
            onContentHeightMeasured = { height -> stickyBottomHeight.value = height },
        ) {
            Buttons.FilledPrimary(
                modifier = Modifier
                    .clip(RoundedCornerShape(corner = CornerSize(Sizes.s16)))
                    .background(WalletTheme.colorScheme.background)
                    .padding(horizontal = Sizes.s04, vertical = Sizes.s03),
                text = stringResource(R.string.global_cancel),
                startIcon = painterResource(R.drawable.wallet_ic_cross),
                onClick = onBack,
            )
        }
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
                actorType = ActorType.VERIFIER,
            ),
            credentialCardStates = CredentialMocks.cardStates.toList().map { it.value() },
            isLoading = false,
            onCredentialSelected = {},
            onBack = {},
        )
    }
}

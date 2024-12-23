package ch.admin.foitt.wallet.feature.presentationRequest.presentation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.InvitationHeader
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.model.ActorUiState
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.LoadingOverlay
import ch.admin.foitt.wallet.platform.composables.SpacerBottom
import ch.admin.foitt.wallet.platform.composables.SpacerTop
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
    ConstraintLayout(
        modifier = Modifier.fillMaxSize()
    ) {
        val (
            topSpacerRef,
            mainContentRef,
            bottomSpacerRef,
            buttonRef,
        ) = createRefs()

        SpacerTop(
            backgroundColor = MaterialTheme.colorScheme.background,
            modifier = Modifier.constrainAs(topSpacerRef) {
                top.linkTo(parent.top)
            },
            useStatusBarInsets = true,
        )
        CompactCredentialList(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = Sizes.s04, end = Sizes.s04)
                .constrainAs(mainContentRef) {
                    top.linkTo(parent.top)
                    bottom.linkTo(buttonRef.top)
                    height = Dimension.fillToConstraints
                },
            contentPadding = PaddingValues(top = Sizes.s04, bottom = Sizes.s04), // add Spacers padding
            credentialStates = credentialCardStates,
            onCredentialSelected = onCredentialSelected,
            headerContent = {
                ListHeader(verifierUiState = verifierUiState)
            },
        )
        Buttons.Outlined(
            modifier = Modifier
                .padding(start = Sizes.s04, end = Sizes.s04, bottom = Sizes.s04)
                .navigationBarsPadding()
                .constrainAs(buttonRef) {
                    bottom.linkTo(parent.bottom)
                },
            text = stringResource(id = R.string.global_back_home),
            startIcon = painterResource(id = R.drawable.pilot_ic_back_button),
            onClick = onBack,
        )
        SpacerBottom(
            backgroundColor = MaterialTheme.colorScheme.background,
            modifier = Modifier.constrainAs(bottomSpacerRef) {
                bottom.linkTo(buttonRef.top)
            },
            useNavigationBarInsets = false,
        )
        LoadingOverlay(showOverlay = isLoading)
    }
}

@Composable
private fun ListHeader(verifierUiState: ActorUiState) {
    Spacer(modifier = Modifier.height(Sizes.s06))
    InvitationHeader(
        inviterName = verifierUiState.name,
        inviterImage = verifierUiState.painter,
        trustStatus = verifierUiState.trustStatus,
    )
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.BodyLarge(text = stringResource(id = R.string.tk_present_multiplecredentials_title))
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
    LazyColumn(
        modifier = modifier,
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

@WalletAllScreenPreview
@Composable
private fun PresentationCredentialListScreenPreview() {
    WalletTheme {
        PresentationCredentialListScreenContent(
            verifierUiState = ActorUiState(
                name = "My verifier name",
                painter = painterResource(R.drawable.pilot_ic_strassenverkehrsamt),
                trustStatus = TrustStatus.TRUSTED,
            ),
            credentialCardStates = CredentialMocks.cardStates.toList().map { it.value() },
            isLoading = false,
            onCredentialSelected = {},
            onBack = {},
        )
    }
}

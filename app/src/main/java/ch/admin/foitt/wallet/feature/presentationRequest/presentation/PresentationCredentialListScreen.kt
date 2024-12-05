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
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.InvitationHeader
import ch.admin.foitt.wallet.platform.composables.LoadingOverlay
import ch.admin.foitt.wallet.platform.composables.SpacerBottom
import ch.admin.foitt.wallet.platform.composables.SpacerTop
import ch.admin.foitt.wallet.platform.credential.presentation.CredentialListRow
import ch.admin.foitt.wallet.platform.credential.presentation.mock.CredentialMocks
import ch.admin.foitt.wallet.platform.credential.presentation.model.CredentialCardState
import ch.admin.foitt.wallet.platform.navArgs.domain.model.PresentationCredentialListNavArg
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme
import com.ramcosta.composedestinations.annotation.Destination

@Destination(
    navArgsDelegate = PresentationCredentialListNavArg::class,
)
@Composable
fun PresentationCredentialListScreen(viewModel: PresentationCredentialListViewModel) {
    PresentationCredentialListScreenContent(
        verifierName = viewModel.verifierName.collectAsStateWithLifecycle().value,
        verifierImage = viewModel.verifierLogo.collectAsStateWithLifecycle().value,
        credentialCardStates = viewModel.presentationCredentialListUiState.collectAsStateWithLifecycle().value,
        isLoading = viewModel.isLoading.collectAsStateWithLifecycle().value,
        onCredentialSelected = viewModel::onCredentialSelected,
        onBack = viewModel::onBack,
    )
}

@Composable
private fun PresentationCredentialListScreenContent(
    verifierName: String?,
    verifierImage: Painter?,
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
                ListHeader(
                    verifierName = verifierName,
                    verifierImage = verifierImage,
                )
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
private fun ListHeader(verifierName: String?, verifierImage: Painter?) {
    InvitationHeader(
        modifier = Modifier.padding(vertical = Sizes.s02, horizontal = Sizes.s06),
        inviterName = verifierName ?: stringResource(id = R.string.presentation_verifier_name_unknown),
        inviterImage = verifierImage,
        message = stringResource(id = R.string.presentation_verifier_text)
    )
    WalletTexts.TitleSmall(text = stringResource(id = R.string.presentation_select_credential_title))
    Spacer(modifier = Modifier.height(Sizes.s02))
    WalletTexts.Body(text = stringResource(id = R.string.presentation_select_credential_subtitle))
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
            verifierName = "My Verifier Name",
            verifierImage = painterResource(id = R.drawable.pilot_ic_strassenverkehrsamt),
            credentialCardStates = CredentialMocks.cardStates.toList().map { it.value() },
            isLoading = false,
            onCredentialSelected = {},
            onBack = {},
        )
    }
}

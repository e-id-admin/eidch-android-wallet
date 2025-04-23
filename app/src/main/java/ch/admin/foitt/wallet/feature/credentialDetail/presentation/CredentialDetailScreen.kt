package ch.admin.foitt.wallet.feature.credentialDetail.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.sharp.ArrowBack
import androidx.compose.material.icons.sharp.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SheetState
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowWidthSizeClass
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.credentialDetail.presentation.composables.CredentialDeleteBottomSheet
import ch.admin.foitt.wallet.feature.credentialDetail.presentation.composables.MenuBottomSheet
import ch.admin.foitt.wallet.feature.credentialDetail.presentation.composables.VisibleBottomSheet
import ch.admin.foitt.wallet.feature.credentialDetail.presentation.model.CredentialDetailUiState
import ch.admin.foitt.wallet.platform.actorMetadata.domain.model.ActorType
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.model.ActorUiState
import ch.admin.foitt.wallet.platform.composables.HiddenScrollToButton
import ch.admin.foitt.wallet.platform.composables.HiddenScrollToTopButton
import ch.admin.foitt.wallet.platform.composables.LoadingOverlay
import ch.admin.foitt.wallet.platform.composables.calculateVerticalPadding
import ch.admin.foitt.wallet.platform.composables.presentation.HeightReportingLayout
import ch.admin.foitt.wallet.platform.composables.presentation.bottomSafeDrawing
import ch.admin.foitt.wallet.platform.composables.presentation.horizontalSafeDrawing
import ch.admin.foitt.wallet.platform.composables.presentation.layout.LazyColumn
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.composables.presentation.spaceBarKeyClickable
import ch.admin.foitt.wallet.platform.composables.presentation.topSafeDrawing
import ch.admin.foitt.wallet.platform.credential.presentation.LargeCredentialCard
import ch.admin.foitt.wallet.platform.credential.presentation.credentialClaimItems
import ch.admin.foitt.wallet.platform.credential.presentation.mock.CredentialMocks
import ch.admin.foitt.wallet.platform.navArgs.domain.model.CredentialDetailNavArg
import ch.admin.foitt.wallet.platform.preview.AllCompactScreensPreview
import ch.admin.foitt.wallet.platform.preview.AllLargeScreensPreview
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTheme
import com.ramcosta.composedestinations.annotation.Destination
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Destination(
    navArgsDelegate = CredentialDetailNavArg::class
)
@Composable
fun CredentialDetailScreen(
    viewModel: CredentialDetailViewModel,
) {
    val scope = rememberCoroutineScope()

    val menuSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val deleteSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val visibleBottomSheet = viewModel.visibleBottomSheet.collectAsStateWithLifecycle().value
    if (visibleBottomSheet == VisibleBottomSheet.MENU) {
        MenuBottomSheet(
            sheetState = menuSheetState,
            onDismiss = {
                scope.hideModalSheet(menuSheetState, onHidden = viewModel::onBottomSheetDismiss)
            },
            onDelete = {
                scope.hideModalSheet(menuSheetState, onHidden = viewModel::onDelete)
            },
            onWrongData = {
                scope.hideModalSheet(menuSheetState, onHidden = viewModel::onWrongData)
            }
        )
    }
    if (visibleBottomSheet == VisibleBottomSheet.DELETE) {
        CredentialDeleteBottomSheet(
            sheetState = deleteSheetState,
            onDismiss = {
                scope.hideModalSheet(deleteSheetState, onHidden = viewModel::onBottomSheetDismiss)
            },
            onDeleteCredential = {
                scope.hideModalSheet(deleteSheetState, onHidden = viewModel::onDeleteCredential)
            },
        )
    }

    CredentialDetailScreenContent(
        isLoading = viewModel.isLoading.collectAsStateWithLifecycle().value,
        credentialDetail = viewModel.credentialDetailUiState.collectAsStateWithLifecycle().value,
        onWrongData = viewModel::onWrongData,
        onBack = viewModel::onBack,
        onMenu = viewModel::onMenu,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
private fun CoroutineScope.hideModalSheet(
    sheetState: SheetState,
    onHidden: () -> Unit,
) {
    launch { sheetState.hide() }.invokeOnCompletion {
        if (!sheetState.isVisible) {
            onHidden()
        }
    }
}

@Composable
private fun CredentialDetailScreenContent(
    isLoading: Boolean,
    credentialDetail: CredentialDetailUiState,
    windowWidthClass: WindowWidthSizeClass = currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass,
    onWrongData: () -> Unit,
    onBack: () -> Unit,
    onMenu: () -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        when (windowWidthClass) {
            WindowWidthSizeClass.COMPACT -> CredentialDetailCompact(
                credentialDetail = credentialDetail,
                onWrongData = onWrongData,
                onBack = onBack,
                onMenu = onMenu,
            )
            else -> CredentialDetailLarge(
                credentialDetail = credentialDetail,
                onWrongData = onWrongData,
                onBack = onBack,
                onMenu = onMenu,
            )
        }
        LoadingOverlay(showOverlay = isLoading)
    }
}

@Composable
private fun BoxWithConstraintsScope.CredentialDetailCompact(
    credentialDetail: CredentialDetailUiState,
    onWrongData: () -> Unit,
    onBack: () -> Unit,
    onMenu: () -> Unit,
) {
    val lazyListState = rememberLazyListState()
    HiddenScrollToButton(
        text = stringResource(id = R.string.tk_receive_jump_to_details),
        lazyListState = lazyListState,
        index = 1,
    )
    HiddenScrollToTopButton(
        text = stringResource(id = R.string.tk_global_hiddenGoToTop),
        lazyListState = lazyListState
    )
    WalletLayouts.LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        state = lazyListState,
        contentPadding = PaddingValues(bottom = Sizes.s04),
        useTopInsets = false,
    ) {
        item {
            CredentialWithTopBar(
                credentialDetail = credentialDetail,
                credentialMinHeight = maxHeight * 0.7f,
                onBack = onBack,
                onMenu = onMenu
            )
            Spacer(Modifier.height(Sizes.s04))
        }
        credentialClaimItems(
            title = R.string.tk_displaydelete_displaycredential1_title2,
            claims = credentialDetail.claims,
            showIssuer = true,
            issuer = credentialDetail.issuer.name,
            issuerIcon = credentialDetail.issuer.painter,
            onWrongData = onWrongData,
        )
    }
}

@Composable
private fun BoxWithConstraintsScope.CredentialDetailLarge(
    credentialDetail: CredentialDetailUiState,
    onWrongData: () -> Unit,
    onBack: () -> Unit,
    onMenu: () -> Unit,
) {
    Row(
        modifier = Modifier
            .horizontalSafeDrawing()
    ) {
        val verticalSafeDrawing = WindowInsets.safeDrawing.asPaddingValues().calculateVerticalPadding()
        CredentialWithTopBar(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .topSafeDrawing()
                .bottomSafeDrawing()
                .padding(start = Sizes.s02, bottom = Sizes.s02),
            credentialDetail = credentialDetail,
            credentialMinHeight = this@CredentialDetailLarge.maxHeight - Sizes.s02 - verticalSafeDrawing,
            onBack = onBack,
            onMenu = onMenu,
        )
        Spacer(modifier = Modifier.width(Sizes.s04))
        Box(modifier = Modifier.weight(1f)) {
            val lazyListState = rememberLazyListState()
            WalletLayouts.LazyColumn(
                state = lazyListState,
                contentPadding = PaddingValues(bottom = Sizes.s02),
            ) {
                credentialClaimItems(
                    title = R.string.tk_displaydelete_displaycredential1_title2,
                    claims = credentialDetail.claims,
                    issuer = credentialDetail.issuer.name,
                    issuerIcon = credentialDetail.issuer.painter,
                    onWrongData = onWrongData,
                )
            }
            HiddenScrollToTopButton(
                text = stringResource(id = R.string.tk_global_hiddenGoToTop),
                lazyListState = lazyListState
            )
        }
    }
}

@Composable
private fun CredentialWithTopBar(
    modifier: Modifier = Modifier,
    credentialMinHeight: Dp,
    credentialDetail: CredentialDetailUiState,
    onBack: () -> Unit,
    onMenu: () -> Unit
) {
    Box(modifier = modifier) {
        var topBarHeight by remember { mutableStateOf(0.dp) }
        LargeCredentialCard(
            contentPaddingValues = PaddingValues(
                start = Sizes.s04,
                top = Sizes.s03 + topBarHeight,
                end = Sizes.s04,
                bottom = Sizes.s06,
            ),
            minHeight = credentialMinHeight,
            credentialCardState = credentialDetail.credential,
        )
        HeightReportingLayout(onContentHeightMeasured = { topBarHeight = it }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Sizes.s03)
                    .topSafeDrawing(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                BackButton(onBack)
                MenuButton(onMenu)
            }
        }
    }
}

@Composable
private fun BackButton(onBack: () -> Unit) {
    FilledIconButton(
        onClick = onBack,
        modifier = Modifier
            .size(Sizes.s10)
            .spaceBarKeyClickable(onBack)
    ) {
        Icon(
            modifier = Modifier.size(Sizes.s06),
            imageVector = Icons.AutoMirrored.Sharp.ArrowBack,
            contentDescription = stringResource(id = R.string.tk_global_back_alt),
            tint = WalletTheme.colorScheme.onPrimary,
        )
    }
}

@Composable
private fun MenuButton(onBack: () -> Unit) {
    FilledIconButton(
        onClick = onBack,
        modifier = Modifier
            .size(Sizes.s10)
            .spaceBarKeyClickable(onBack)
    ) {
        Icon(
            modifier = Modifier.size(Sizes.s06),
            imageVector = Icons.Sharp.MoreVert,
            contentDescription = stringResource(id = R.string.tk_global_moreoptions_alt),
            tint = WalletTheme.colorScheme.onPrimary,
        )
    }
}

@AllCompactScreensPreview
@Composable
private fun CredentialDetailScreenCompactPreview() {
    WalletTheme {
        ExampleScreen(windowWidthClass = WindowWidthSizeClass.COMPACT)
    }
}

@AllLargeScreensPreview
@Composable
private fun CredentialDetailScreenLargePreview() {
    WalletTheme {
        ExampleScreen(windowWidthClass = WindowWidthSizeClass.EXPANDED)
    }
}

@Composable
private fun ExampleScreen(windowWidthClass: WindowWidthSizeClass) {
    CredentialDetailScreenContent(
        isLoading = false,
        credentialDetail = CredentialDetailUiState(
            credential = CredentialMocks.cardState01,
            claims = CredentialMocks.claimList,
            issuer = ActorUiState(
                name = "Issuer",
                painter = painterResource(id = R.drawable.ic_swiss_cross_small),
                trustStatus = TrustStatus.TRUSTED,
                actorType = ActorType.ISSUER,
            )
        ),
        windowWidthClass = windowWidthClass,
        onWrongData = {},
        onBack = {},
        onMenu = {},
    )
}

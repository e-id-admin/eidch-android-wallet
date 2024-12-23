package ch.admin.foitt.wallet.feature.home.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshDefaults
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowWidthSizeClass
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.presentation.layout.LazyColumn
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.composables.presentation.nonFocusableAccessibilityAnchor
import ch.admin.foitt.wallet.platform.credential.presentation.CredentialListRow
import ch.admin.foitt.wallet.platform.credential.presentation.mock.CredentialMocks
import ch.admin.foitt.wallet.platform.credential.presentation.model.CredentialCardState
import ch.admin.foitt.wallet.platform.preview.AllCompactScreensPreview
import ch.admin.foitt.wallet.platform.preview.AllLargeScreensPreview
import ch.admin.foitt.wallet.platform.preview.ComposableWrapper
import ch.admin.foitt.wallet.platform.utils.TestTags
import ch.admin.foitt.wallet.platform.utils.TraversalIndex
import ch.admin.foitt.wallet.platform.utils.setIsTraversalGroup
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme
import com.ramcosta.composedestinations.annotation.Destination

@Destination
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
) {
    HomeScreenContent(
        screenState = viewModel.screenState.collectAsStateWithLifecycle().value,
        isRefreshing = viewModel.isRefreshing.collectAsStateWithLifecycle().value,
        onQrScan = viewModel::onQrScan,
        onMenu = viewModel::onMenu,
        onRefresh = viewModel::onRefresh,
        onClickBetaId = viewModel::onClickBetaId
    )
}

@Composable
private fun HomeScreenContent(
    screenState: HomeScreenState,
    isRefreshing: Boolean,
    onQrScan: () -> Unit,
    onMenu: () -> Unit,
    onRefresh: () -> Unit,
    onClickBetaId: () -> Unit,
    windowWidthClass: WindowWidthSizeClass = currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass,
) = WalletLayouts.HomeContainer(
    onScan = onQrScan,
    onMenu = onMenu,
    windowWidthClass = windowWidthClass,
) { stickyBottomHeightDp ->
    when (screenState) {
        HomeScreenState.Initial -> {
        }
        is HomeScreenState.CredentialList -> Credentials(
            credentialsState = screenState.credentials,
            isRefreshing = isRefreshing,
            contentBottomPadding = stickyBottomHeightDp,
            onCredentialClick = screenState.onCredentialClick,
            onRefresh = onRefresh,
        )
        HomeScreenState.NoCredential -> NoCredentialContent(
            stickyBottomHeightDp,
            onClickBetaId = onClickBetaId
        )
    }
}

@Composable
private fun BoxScope.NoCredentialContent(
    contentBottomPadding: Dp,
    onClickBetaId: () -> Unit,
) = Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
        .align(Alignment.Center)
        .wrapContentHeight()
        .verticalScroll(rememberScrollState())
        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
        .padding(bottom = contentBottomPadding)
        .padding(start = Sizes.s06, end = Sizes.s06, top = Sizes.s04, bottom = Sizes.s06)
        .widthIn(max = Sizes.maxTextWidth)
        .setIsTraversalGroup(index = TraversalIndex.HIGH1)
) {
    NoCredentialIcon()
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.TitleLarge(
        text = stringResource(id = R.string.tk_getBetaId_firstUse_title),
        textAlign = TextAlign.Center,
        modifier = Modifier
            .nonFocusableAccessibilityAnchor()
    )
    Spacer(modifier = Modifier.height(Sizes.s02))
    WalletTexts.Body(
        text = stringResource(id = R.string.tk_getBetaId_firstUse_body),
        textAlign = TextAlign.Center,
    )
    Spacer(modifier = Modifier.height(Sizes.s10))
    Buttons.FilledTertiary(
        text = stringResource(R.string.tk_global_getbetaid_primarybutton),
        onClick = onClickBetaId,
        startIcon = painterResource(id = R.drawable.wallet_ic_next_button)
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun Credentials(
    credentialsState: List<CredentialCardState>,
    isRefreshing: Boolean,
    contentBottomPadding: Dp,
    onCredentialClick: (id: Long) -> Unit,
    onRefresh: () -> Unit,
) {
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        refreshingOffset = pullToRefreshTopPadding(),
        onRefresh = onRefresh,
    )
    WalletLayouts.LazyColumn(
        useBottomInsets = false,
        modifier = Modifier
            .setIsTraversalGroup()
            .fillMaxHeight()
            .pullRefresh(
                state = pullRefreshState,
            ),
        contentPadding = PaddingValues(
            top = Sizes.s06,
            bottom = contentBottomPadding + Sizes.s06
        )
    ) {
        itemsIndexed(credentialsState) { _, credentialState ->
            CredentialListRow(
                showDivider = true,
                credentialState = credentialState,
                onClick = { onCredentialClick(credentialState.credentialId) },
            )
        }
    }
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter,
    ) {
        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
        )
    }
}

@Composable
private fun NoCredentialIcon() = Box(
    contentAlignment = Alignment.Center,
) {
    Image(
        painter = painterResource(id = R.drawable.wallet_ic_nocredential_bg),
        contentDescription = null,
        modifier = Modifier.width(Sizes.noCredentialThumbnailWidth)
            .testTag(TestTags.NO_CREDENTIAL_ICON.name)
    )
    Image(
        painter = painterResource(id = R.drawable.wallet_ic_nocredential_line),
        contentDescription = null,
        modifier = Modifier.width(Sizes.s20)
    )
}

private class HomePreviewParams : PreviewParameterProvider<ComposableWrapper<HomeScreenState>> {
    override val values: Sequence<ComposableWrapper<HomeScreenState>> = sequenceOf(
        ComposableWrapper {
            HomeScreenState.CredentialList(
                credentials = CredentialMocks.cardStates.toList().map { it.value() },
                onCredentialClick = {},
            )
        },
        ComposableWrapper { HomeScreenState.NoCredential },
    )
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun pullToRefreshTopPadding() = PullRefreshDefaults.RefreshingOffset +
    WindowInsets.safeDrawing.only(WindowInsetsSides.Top).asPaddingValues().calculateTopPadding()

@AllCompactScreensPreview
@Composable
private fun HomeScreenCompactPreview(
    @PreviewParameter(HomePreviewParams::class) state: ComposableWrapper<HomeScreenState>,
) {
    WalletTheme {
        HomeScreenContent(
            screenState = state.value(),
            windowWidthClass = WindowWidthSizeClass.COMPACT,
            isRefreshing = true,
            onQrScan = {},
            onMenu = {},
            onRefresh = {},
            onClickBetaId = {},
        )
    }
}

@AllLargeScreensPreview
@Composable
private fun HomeScreenLargePreview(
    @PreviewParameter(HomePreviewParams::class) state: ComposableWrapper<HomeScreenState>,
) {
    WalletTheme {
        HomeScreenContent(
            screenState = state.value(),
            windowWidthClass = WindowWidthSizeClass.EXPANDED,
            isRefreshing = false,
            onQrScan = {},
            onMenu = {},
            onRefresh = {},
            onClickBetaId = {},
        )
    }
}

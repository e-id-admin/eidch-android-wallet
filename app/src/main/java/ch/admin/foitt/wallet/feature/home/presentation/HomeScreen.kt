package ch.admin.foitt.wallet.feature.home.presentation

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshDefaults
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowWidthSizeClass
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.home.domain.model.EIdRequest
import ch.admin.foitt.wallet.feature.home.presentation.composables.EIdRequestCard
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.ToastAnimated
import ch.admin.foitt.wallet.platform.composables.presentation.layout.LazyColumn
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.composables.presentation.nonFocusableAccessibilityAnchor
import ch.admin.foitt.wallet.platform.credential.presentation.CredentialListRow
import ch.admin.foitt.wallet.platform.credential.presentation.mock.CredentialMocks
import ch.admin.foitt.wallet.platform.credential.presentation.model.CredentialCardState
import ch.admin.foitt.wallet.platform.eIdApplicationProcess.domain.model.EIdRequestQueueState
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
        onStartOnlineIdentification = viewModel::onStartOnlineIdentification,
        eventMessage = viewModel.eventMessage.collectAsStateWithLifecycle().value,
        onCloseToast = viewModel::onCloseToast,
        onQrScan = viewModel::onQrScan,
        onMenu = viewModel::onMenu,
        onRefresh = viewModel::onRefresh,
        onGetBetaId = viewModel::onGetBetaId,
        onGetEId = viewModel::onGetEId,
    )
}

@Composable
private fun HomeScreenContent(
    screenState: HomeScreenState,
    isRefreshing: Boolean,
    onStartOnlineIdentification: () -> Unit,
    @StringRes eventMessage: Int?,
    onCloseToast: () -> Unit,
    onQrScan: () -> Unit,
    onMenu: () -> Unit,
    onRefresh: () -> Unit,
    onGetBetaId: () -> Unit,
    onGetEId: () -> Unit,
    windowWidthClass: WindowWidthSizeClass = currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass,
) = WalletLayouts.HomeContainer(
    onMenu = onMenu,
    onScan = onQrScan,
    windowWidthClass = windowWidthClass,
) { stickyBottomHeightDp ->

    when (screenState) {
        HomeScreenState.Initial -> {
        }

        is HomeScreenState.CredentialList -> Credentials(
            credentialsState = screenState.credentials,
            isRefreshing = isRefreshing,
            ongoingEIdRequests = screenState.eIdRequests,
            onStartOnlineIdentification = onStartOnlineIdentification,
            contentBottomPadding = stickyBottomHeightDp,
            onCredentialClick = screenState.onCredentialClick,
            onRefresh = onRefresh,
            messageToast = eventMessage,
            onCloseToast = onCloseToast,
        )

        is HomeScreenState.NoCredential -> NoCredentialContent(
            contentBottomPadding = stickyBottomHeightDp,
            isRefreshing = isRefreshing,
            ongoingEIdRequests = screenState.eIdRequests,
            onStartOnlineIdentification = onStartOnlineIdentification,
            showBetaIdRequestButton = screenState.showBetaIdRequestButton,
            showEIdRequestButton = screenState.showEIdRequestButton,
            onRequestBetaId = onGetBetaId,
            onRequestEId = onGetEId,
            onRefresh = onRefresh,
        )
    }
}

@Composable
private fun BoxScope.NoCredentialContent(
    contentBottomPadding: Dp,
    isRefreshing: Boolean,
    ongoingEIdRequests: List<EIdRequest>,
    onStartOnlineIdentification: () -> Unit,
    showEIdRequestButton: Boolean,
    showBetaIdRequestButton: Boolean,
    onRequestEId: () -> Unit,
    onRequestBetaId: () -> Unit,
    onRefresh: () -> Unit,
) {
    if (ongoingEIdRequests.isEmpty()) {
        WalletEmptyContainer(
            contentBottomPadding = contentBottomPadding,
            showEIdRequestButton = showEIdRequestButton,
            showBetaIdRequestButton = showBetaIdRequestButton,
            onRequestEId = onRequestEId,
            onRequestBetaId = onRequestBetaId,
        )
    } else {
        WalletEmptyWithEIdRequestsContainer(
            contentBottomPadding = contentBottomPadding,
            isRefreshing = isRefreshing,
            ongoingEIdRequests = ongoingEIdRequests,
            onStartOnlineIdentification = onStartOnlineIdentification,
            showEIdRequestButton = showEIdRequestButton,
            showBetaIdRequestButton = showBetaIdRequestButton,
            onRequestEId = onRequestEId,
            onRequestBetaId = onRequestBetaId,
            onRefresh = onRefresh,
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun WalletEmptyWithEIdRequestsContainer(
    contentBottomPadding: Dp,
    isRefreshing: Boolean,
    ongoingEIdRequests: List<EIdRequest>,
    onStartOnlineIdentification: () -> Unit,
    showEIdRequestButton: Boolean,
    showBetaIdRequestButton: Boolean,
    onRequestEId: () -> Unit,
    onRequestBetaId: () -> Unit,
    onRefresh: () -> Unit,
) {
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        refreshingOffset = pullToRefreshTopPadding(),
        onRefresh = onRefresh,
    )
    LazyColumn(
        state = rememberLazyListState(),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
            .padding(bottom = contentBottomPadding)
            .pullRefresh(
                state = pullRefreshState,
            )
            .setIsTraversalGroup(index = TraversalIndex.HIGH1),
    ) {
        item {
            Spacer(modifier = Modifier.height(Sizes.s04))
        }

        items(ongoingEIdRequests) { eIdRequest: EIdRequest ->
            Box(modifier = Modifier.padding(horizontal = Sizes.s03)) {
                EIdRequestCard(
                    eIdRequest = eIdRequest,
                    onStartOnlineIdentification = onStartOnlineIdentification,
                )
            }
            Spacer(modifier = Modifier.height(Sizes.s02))
        }

        item {
            Column(
                modifier = Modifier.padding(vertical = Sizes.s04),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(80.dp))
                WalletEmptyContent(
                    showEIdRequestButton = showEIdRequestButton,
                    showBetaIdRequestButton = showBetaIdRequestButton,
                    onRequestEId = onRequestEId,
                    onRequestBetaId = onRequestBetaId,
                )
                Spacer(modifier = Modifier.height(80.dp))
            }
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
fun BoxScope.WalletEmptyContainer(
    contentBottomPadding: Dp,
    showEIdRequestButton: Boolean,
    showBetaIdRequestButton: Boolean,
    onRequestEId: () -> Unit,
    onRequestBetaId: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .align(Alignment.Center)
            .wrapContentHeight()
            .verticalScroll(rememberScrollState())
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
            .padding(bottom = contentBottomPadding)
            .padding(start = Sizes.s06, end = Sizes.s06, top = Sizes.s04, bottom = Sizes.s06)
            .widthIn(max = Sizes.maxTextWidth)
            .setIsTraversalGroup(index = TraversalIndex.HIGH1),
    ) {
        WalletEmptyContent(
            showEIdRequestButton = showEIdRequestButton,
            showBetaIdRequestButton = showBetaIdRequestButton,
            onRequestEId = onRequestEId,
            onRequestBetaId = onRequestBetaId,
        )
    }
}

@Composable
fun WalletEmptyContent(
    showEIdRequestButton: Boolean,
    showBetaIdRequestButton: Boolean,
    onRequestEId: () -> Unit,
    onRequestBetaId: () -> Unit,
) {
    NoCredentialIcon()
    Spacer(modifier = Modifier.height(Sizes.s06))
    WalletTexts.TitleLarge(
        text = stringResource(id = R.string.tk_getBetaId_firstUse_title),
        textAlign = TextAlign.Center,
        modifier = Modifier
            .nonFocusableAccessibilityAnchor()
    )
    Spacer(modifier = Modifier.height(Sizes.s01))
    WalletTexts.Body(
        text = stringResource(id = R.string.tk_getBetaId_firstUse_body),
        textAlign = TextAlign.Center,
    )
    if (showBetaIdRequestButton || showEIdRequestButton) {
        Spacer(modifier = Modifier.height(Sizes.s06))
        Column(
            verticalArrangement = Arrangement.spacedBy(Sizes.s04),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (showEIdRequestButton) {
                Buttons.FilledTertiary(
                    text = stringResource(R.string.tk_global_getEid_greenButton),
                    onClick = onRequestEId,
                    startIcon = painterResource(id = R.drawable.wallet_ic_next_button)
                )
            }
            if (showBetaIdRequestButton) {
                Buttons.FilledTertiary(
                    text = stringResource(R.string.tk_global_getbetaid_primarybutton),
                    onClick = onRequestBetaId,
                    startIcon = painterResource(id = R.drawable.wallet_ic_next_button)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun Credentials(
    credentialsState: List<CredentialCardState>,
    isRefreshing: Boolean,
    @StringRes messageToast: Int?,
    onCloseToast: () -> Unit,
    contentBottomPadding: Dp,
    ongoingEIdRequests: List<EIdRequest>,
    onStartOnlineIdentification: () -> Unit,
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
            .pullRefresh(state = pullRefreshState)
            .testTag(TestTags.CREDENTIAL_LIST.name),
        contentPadding = PaddingValues(
            top = Sizes.s06,
            bottom = contentBottomPadding + Sizes.s06
        )
    ) {
        if (ongoingEIdRequests.isNotEmpty()) {
            items(ongoingEIdRequests) { eIdRequest: EIdRequest ->
                Box(modifier = Modifier.padding(horizontal = Sizes.s03)) {
                    EIdRequestCard(
                        eIdRequest = eIdRequest,
                        onStartOnlineIdentification = onStartOnlineIdentification,
                    )
                }
                Spacer(modifier = Modifier.height(Sizes.s02))
            }

            item {
                HorizontalDivider()
            }
        }

        items(credentialsState) { credentialState ->
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

    ToastAnimated(
        isVisible = messageToast != null,
        isSnackBarDesign = true,
        messageToast = messageToast,
        onCloseToast = onCloseToast,
        iconEnd = R.drawable.wallet_ic_cross,
        contentBottomPadding = contentBottomPadding + Sizes.s06
    )
}

@Composable
private fun NoCredentialIcon() = Box(
    contentAlignment = Alignment.Center,
) {
    Image(
        painter = painterResource(id = R.drawable.wallet_ic_nocredential_bg),
        contentDescription = null,
        modifier = Modifier
            .width(Sizes.noCredentialThumbnailWidth)
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
                eIdRequests = emptyList(),
                credentials = CredentialMocks.cardStates.toList().map { it.value() },
                onCredentialClick = {},
            )
        },
        ComposableWrapper {
            HomeScreenState.CredentialList(
                eIdRequests = listOf(
                    EIdRequest(
                        state = EIdRequestQueueState.IN_QUEUING,
                        firstName = "Seraina",
                        lastName = "Muster"
                    ),
                    EIdRequest(
                        state = EIdRequestQueueState.READY_FOR_ONLINE_SESSION,
                        firstName = "Seraina",
                        lastName = "Muster"
                    )
                ),
                credentials = CredentialMocks.cardStates.toList().map { it.value() },
                onCredentialClick = {},
            )
        },
        ComposableWrapper {
            HomeScreenState.NoCredential(
                eIdRequests = emptyList(),
                showBetaIdRequestButton = true,
                showEIdRequestButton = true
            )
        },
        ComposableWrapper {
            HomeScreenState.NoCredential(
                eIdRequests = listOf(
                    EIdRequest(
                        state = EIdRequestQueueState.IN_QUEUING,
                        firstName = "Seraina",
                        lastName = "Muster"
                    ),
                    EIdRequest(
                        state = EIdRequestQueueState.READY_FOR_ONLINE_SESSION,
                        firstName = "Seraina",
                        lastName = "Muster"
                    )
                ),
                showBetaIdRequestButton = true,
                showEIdRequestButton = true
            )
        },
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
            onStartOnlineIdentification = {},
            onQrScan = {},
            onMenu = {},
            onRefresh = {},
            onGetBetaId = {},
            onGetEId = {},
            onCloseToast = {},
            eventMessage = null,
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
            onStartOnlineIdentification = {},
            isRefreshing = false,
            onQrScan = {},
            onMenu = {},
            onRefresh = {},
            onGetBetaId = {},
            onGetEId = {},
            onCloseToast = {},
            eventMessage = null,
        )
    }
}

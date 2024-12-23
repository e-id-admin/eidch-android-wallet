package ch.admin.foitt.wallet.feature.credentialOffer.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowWidthSizeClass
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.InvitationHeader
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.model.ActorUiState
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.LoadingOverlay
import ch.admin.foitt.wallet.platform.composables.presentation.HeightReportingLayout
import ch.admin.foitt.wallet.platform.navArgs.domain.model.DeclineCredentialOfferNavArg
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletButtonColors
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme
import com.ramcosta.composedestinations.annotation.Destination

@Destination(
    navArgsDelegate = DeclineCredentialOfferNavArg::class,
)
@Composable
fun DeclineCredentialOfferScreen(
    viewModel: DeclineCredentialOfferViewModel,
) {
    DeclineCredentialOfferScreenContent(
        isLoading = viewModel.isLoading.collectAsStateWithLifecycle().value,
        issuer = viewModel.uiState.collectAsStateWithLifecycle().value.issuer,
        onCancel = viewModel::onCancel,
        onDecline = viewModel::onDecline,
    )
}

@Composable
private fun DeclineCredentialOfferScreenContent(
    isLoading: Boolean,
    issuer: ActorUiState,
    onCancel: () -> Unit,
    onDecline: () -> Unit,
) {
    val headerHeight = remember { mutableStateOf(0.dp) }
    val stickyBottomHeight = remember { mutableStateOf(0.dp) }

    BoxWithConstraints(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
        ) {
            Header(
                issuer = issuer,
                headerHeight = headerHeight
            )

            val topInsets = WindowInsets.safeDrawing.only(WindowInsetsSides.Top).asPaddingValues()
            val minHeight = this@BoxWithConstraints.maxHeight - headerHeight.value - topInsets.calculateTopPadding()
            Sheet(
                modifier = Modifier.heightIn(min = minHeight),
                stickyBottomHeight = stickyBottomHeight.value,
            )
        }
        StickyBottomButtons(
            modifier = Modifier.align(Alignment.BottomCenter),
            stickyBottomHeight = stickyBottomHeight,
            onDecline = onDecline,
            onCancel = onCancel,
        )

        LoadingOverlay(showOverlay = isLoading)
    }
}

@Composable
private fun Header(
    issuer: ActorUiState,
    headerHeight: MutableState<Dp>,
) = HeightReportingLayout(
    onContentHeightMeasured = { height -> headerHeight.value = height }
) {
    Column {
        Spacer(modifier = Modifier.height(Sizes.s06))
        InvitationHeader(
            modifier = Modifier.padding(horizontal = Sizes.s04),
            inviterName = issuer.name,
            inviterImage = issuer.painter,
            trustStatus = issuer.trustStatus,
        )
        Spacer(modifier = Modifier.height(Sizes.s06))
    }
}

@Composable
private fun Sheet(
    modifier: Modifier = Modifier,
    stickyBottomHeight: Dp,
) = Box(
    modifier = modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(topStart = Sizes.boxCornerSize, topEnd = Sizes.boxCornerSize))
        .background(WalletTheme.colorScheme.primary)
        .padding(top = Sizes.s06, start = Sizes.s06, end = Sizes.s06),
    contentAlignment = Alignment.Center
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = Sizes.s06 + stickyBottomHeight),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
        if (windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT) {
            Icon(
                painter = painterResource(id = R.drawable.wallet_ic_circular_questionmark),
                contentDescription = null,
                tint = WalletTheme.colorScheme.lightPrimary,
            )
            Spacer(modifier = Modifier.height(Sizes.s01))
        }
        WalletTexts.BodyLarge(
            text = stringResource(id = R.string.tk_receive_deny1_title),
            color = WalletTheme.colorScheme.lightPrimary,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(Sizes.s01))
        WalletTexts.BodyLarge(
            text = stringResource(id = R.string.tk_receive_deny1_body),
            color = WalletTheme.colorScheme.lightPrimary,
            textAlign = TextAlign.Center,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StickyBottomButtons(
    modifier: Modifier,
    stickyBottomHeight: MutableState<Dp>,
    onDecline: () -> Unit,
    onCancel: () -> Unit,
) = HeightReportingLayout(
    modifier = modifier,
    onContentHeightMeasured = { height -> stickyBottomHeight.value = height }
) {
    FlowRow(
        modifier = Modifier
            .padding(bottom = Sizes.s02)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
            .focusGroup(),
        horizontalArrangement = Arrangement.spacedBy(Sizes.s02, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(Sizes.s02, Alignment.Top),
        maxItemsInEachRow = 2,
    ) {
        Buttons.FilledLightPrimary(
            text = stringResource(id = R.string.tk_receive_deny1_primarybutton),
            onClick = onDecline,
        )
        Buttons.Text(
            text = stringResource(id = R.string.tk_global_cancel),
            onClick = onCancel,
            colors = WalletButtonColors.text().copy(contentColor = WalletTheme.colorScheme.onPrimary)
        )
    }
}

@WalletAllScreenPreview
@Composable
private fun DeclineCredentialOfferScreenContentPreview() {
    WalletTheme {
        DeclineCredentialOfferScreenContent(
            isLoading = false,
            issuer = ActorUiState(
                name = "Test Issuer",
                painter = painterResource(id = R.drawable.wallet_ic_scan_person),
                trustStatus = TrustStatus.TRUSTED,
            ),
            onCancel = {},
            onDecline = {},
        )
    }
}

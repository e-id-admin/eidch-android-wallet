package ch.admin.foitt.wallet.feature.credentialDetail.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.VisibilityThreshold
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.credentialDetail.presentation.model.BatchIssuanceInfoUiState
import ch.admin.foitt.wallet.feature.credentialDetail.presentation.model.BatchIssuanceToastEvent
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.LoadingOverlay
import ch.admin.foitt.wallet.platform.composables.Toast
import ch.admin.foitt.wallet.platform.composables.presentation.clusterLazyListItem
import ch.admin.foitt.wallet.platform.composables.presentation.horizontalSafeDrawing
import ch.admin.foitt.wallet.platform.composables.presentation.layout.LazyColumn
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.preview.WalletDefaultPreview
import ch.admin.foitt.wallet.platform.scaffold.presentation.LocalScaffoldPaddings
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlin.time.Duration.Companion.milliseconds

private const val ToastDisplayTimeMillis = 4000L

@Composable
fun BatchIssuanceInfoScreen(
    viewModel: BatchIssuanceInfoViewModel
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val isRenewing = viewModel.isRenewing.collectAsStateWithLifecycle().value

    var toastEvent by remember { mutableStateOf<BatchIssuanceToastEvent?>(null) }
    LaunchedEffect(Unit) {
        // collectLatest so a new event restarts the auto-dismiss timer.
        viewModel.toastEvents.collectLatest { event ->
            toastEvent = event
            delay(ToastDisplayTimeMillis.milliseconds)
            toastEvent = null
        }
    }

    BatchIssuanceInfoScreen(
        uiState = uiState,
        isRenewing = isRenewing,
        toastEvent = toastEvent,
        onMoreInfo = viewModel::onOpenMoreInfo,
        onRenew = viewModel::onRenew,
    )
}

@Composable
private fun BatchIssuanceInfoScreen(
    uiState: BatchIssuanceInfoUiState,
    isRenewing: Boolean,
    toastEvent: BatchIssuanceToastEvent?,
    onMoreInfo: () -> Unit,
    onRenew: () -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        BatchIssuanceInfoScreenContent(
            readyState = uiState as? BatchIssuanceInfoUiState.Ready,
            onMoreInfo = onMoreInfo,
            onRenew = onRenew,
        )

        BatchIssuanceToast(toastEvent = toastEvent)

        LoadingOverlay(showOverlay = uiState is BatchIssuanceInfoUiState.Initial || isRenewing)
    }
}

@Composable
private fun BatchIssuanceInfoScreenContent(
    readyState: BatchIssuanceInfoUiState.Ready?,
    onMoreInfo: () -> Unit,
    onRenew: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(color = WalletTheme.colorScheme.surfaceContainerLow)
    ) {
        WalletLayouts.LazyColumn(
            modifier = Modifier
                .widthIn(max = Sizes.contentMaxWidth)
                .horizontalSafeDrawing()
                .align(Alignment.TopCenter),
            contentPadding = PaddingValues(
                horizontal = Sizes.s04,
                vertical = Sizes.s06,
            ),
            useTopInsets = false,
        ) {
            item {
                WalletLayouts.TopInsetSpacer(
                    shouldScrollUnderTopBar = true,
                    scaffoldPaddings = LocalScaffoldPaddings.current,
                )
            }

            batchIssuanceInfoItem(
                onMoreInfo = onMoreInfo
            )

            item {
                Spacer(Modifier.height(Sizes.s06))
            }

            if (readyState != null) {
                batchUsageDetails(
                    readyState = readyState,
                    onRenew = onRenew,
                )
            }
        }
    }
}

private fun LazyListScope.batchIssuanceInfoItem(
    onMoreInfo: () -> Unit
) {
    clusterLazyListItem(
        isFirstItem = true,
        isLastItem = true,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = Sizes.s04,
                    vertical = Sizes.s06
                ),
        ) {
            Image(
                modifier = Modifier
                    .size(Sizes.s14),
                painter = painterResource(R.drawable.wallet_ic_batch_credential),
                contentDescription = null,
            )

            Spacer(Modifier.height(Sizes.s04))

            WalletTexts.TitleMedium(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { heading() },
                text = stringResource(R.string.tk_credentialDetail_batchIssuanceInfo_title),
                textAlign = TextAlign.Start,
            )

            Spacer(Modifier.height(Sizes.s01))

            WalletTexts.BodyMedium(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.tk_credentialDetail_batchIssuanceInfo_body),
                textAlign = TextAlign.Start,
            )

            Spacer(Modifier.height(Sizes.s04))

            Buttons.TextLink(
                text = stringResource(R.string.tk_credentialDetail_issuanceInfo_moreInfo_button),
                onClick = onMoreInfo,
                endIcon = painterResource(id = R.drawable.wallet_ic_external_link)
            )
        }
    }
}

private fun LazyListScope.batchUsageDetails(
    readyState: BatchIssuanceInfoUiState.Ready,
    onRenew: () -> Unit,
) {
    item {
        WalletTexts.TitleMediumEmphasized(
            text = stringResource(R.string.tk_credentialDetail_batchIssuanceInfo_usageDetails_title),
            modifier = Modifier.padding(horizontal = Sizes.s04)
        )
    }

    item { Spacer(Modifier.height(Sizes.s01)) }

    when (readyState) {
        is BatchIssuanceInfoUiState.Ready.Exhausted -> exhaustedUsageDetails(
            availableUsages = readyState.availableUsages,
            onRenew = onRenew,
        )

        is BatchIssuanceInfoUiState.Ready.Normal -> usageDetails(
            availableUsages = readyState.availableUsages,
            refreshThreshold = readyState.refreshThreshold,
        )
    }

    clusterLazyListItem(
        isFirstItem = false,
        isLastItem = true
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = Sizes.s04,
                    vertical = Sizes.s02
                )
        ) {
            WalletTexts.LabelMedium(
                text = stringResource(R.string.tk_credentialDetail_batchIssuanceInfo_lastRenewal_label),
            )

            WalletTexts.BodyLarge(
                text = readyState.lastRenewal,
                color = WalletTheme.colorScheme.onSurface
            )
        }
    }
}

private fun LazyListScope.usageDetails(
    availableUsages: String,
    refreshThreshold: Int,
) {
    clusterLazyListItem(
        isFirstItem = true,
        isLastItem = false
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = Sizes.s04,
                    vertical = Sizes.s02
                )
        ) {
            WalletTexts.LabelMedium(
                text = stringResource(R.string.tk_credentialDetail_batchIssuanceInfo_availableUsages_label),
            )

            WalletTexts.BodyLarge(
                text = availableUsages,
                color = WalletTheme.colorScheme.onSurface
            )

            WalletTexts.BodyMedium(
                text = stringResource(R.string.tk_credential_issuanceType_refreshHint, refreshThreshold),
            )
        }
    }
}

private fun LazyListScope.exhaustedUsageDetails(
    availableUsages: String,
    onRenew: () -> Unit
) {
    clusterLazyListItem(
        isFirstItem = true,
        isLastItem = false
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = Sizes.s04,
                    top = Sizes.s02,
                    end = Sizes.s04,
                    bottom = Sizes.s04
                )
        ) {
            Row(
                modifier = Modifier.padding(end = Sizes.s02),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    WalletTexts.LabelMedium(
                        text = stringResource(R.string.tk_credentialDetail_batchIssuanceInfo_availableUsages_label)
                    )

                    WalletTexts.BodyLarge(
                        text = availableUsages,
                        color = WalletTheme.colorScheme.onSurface
                    )

                    WalletTexts.BodyMedium(
                        text = stringResource(R.string.tk_credential_issuanceType_renewUsages_hint),
                    )
                }

                Spacer(Modifier.width(Sizes.s04))

                Icon(
                    painter = painterResource(R.drawable.wallet_ic_warning),
                    contentDescription = null,
                    modifier = Modifier.size(Sizes.s06),
                    tint = WalletTheme.colorScheme.onLightOrange
                )
            }

            Spacer(Modifier.height(Sizes.s04))

            Buttons.FilledPrimary(
                text = stringResource(R.string.tk_credential_issuanceType_renewUsages_button),
                onClick = onRenew,
                modifier = Modifier.fillMaxWidth(),
                isSmall = true
            )
        }
    }
}

@Composable
private fun BoxScope.BatchIssuanceToast(
    toastEvent: BatchIssuanceToastEvent?,
) {
    AnimatedVisibility(
        visible = toastEvent != null,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .horizontalSafeDrawing(),
        label = "batchIssuanceToast",
        enter = slideInVertically(
            animationSpec = spring(
                stiffness = Spring.StiffnessMediumLow,
                visibilityThreshold = IntOffset.VisibilityThreshold,
            ),
            initialOffsetY = { fullHeight -> fullHeight },
        ),
        exit = slideOutVertically(
            animationSpec = spring(
                stiffness = Spring.StiffnessMediumLow,
                visibilityThreshold = IntOffset.VisibilityThreshold,
            ),
            targetOffsetY = { fullHeight -> fullHeight },
        ),
    ) {
        // Keep the last non-null event so the toast keeps its content while sliding out.
        val event = remember(this) { toastEvent }
        Box(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                .padding(start = Sizes.s08, end = Sizes.s08, bottom = Sizes.s06)
        ) {
            when (event) {
                BatchIssuanceToastEvent.RENEWAL_SUCCESS -> RenewalSuccessToast()
                BatchIssuanceToastEvent.RENEWAL_FAILURE -> RenewalErrorToast()
                null -> Unit
            }
        }
    }
}

@Composable
private fun RenewalSuccessToast() {
    Toast(
        text = R.string.tk_credential_issuanceType_renewUsages_successToast_title,
        backgroundColor = WalletTheme.colorScheme.tertiary,
        textColor = WalletTheme.colorScheme.onTertiary,
        iconStart = R.drawable.wallet_ic_circled_checkmark,
        iconStartColor = WalletTheme.colorScheme.onTertiary,
    )
}

@Composable
private fun RenewalErrorToast() {
    Toast(
        text = R.string.tk_credential_issuanceType_renewUsages_failureToast_title,
        backgroundColor = WalletTheme.colorScheme.lightErrorFixed,
        textColor = WalletTheme.colorScheme.onLightErrorFixed,
        iconStart = R.drawable.wallet_ic_error,
        iconStartColor = WalletTheme.colorScheme.onLightErrorFixed,
    )
}

private val previewNormalUsage = BatchIssuanceInfoUiState.Ready.Normal(
    availableUsages = "12",
    lastRenewal = "05.12.2025, 10:34",
    refreshThreshold = 2,
)
private val previewExhaustedUsage = BatchIssuanceInfoUiState.Ready.Exhausted(
    availableUsages = "0",
    lastRenewal = "05.12.2025, 10:34",
)

private data class BatchIssuanceInfoPreviewState(
    val uiState: BatchIssuanceInfoUiState,
    val isRenewing: Boolean = false,
    val toastEvent: BatchIssuanceToastEvent? = null,
)

private class BatchIssuanceInfoPreviewParams : PreviewParameterProvider<BatchIssuanceInfoPreviewState> {
    override val values: Sequence<BatchIssuanceInfoPreviewState> = sequenceOf(
        BatchIssuanceInfoPreviewState(uiState = BatchIssuanceInfoUiState.Initial),
        BatchIssuanceInfoPreviewState(uiState = previewNormalUsage),
        BatchIssuanceInfoPreviewState(uiState = previewExhaustedUsage, isRenewing = true),
        BatchIssuanceInfoPreviewState(
            uiState = previewExhaustedUsage,
            toastEvent = BatchIssuanceToastEvent.RENEWAL_FAILURE,
        ),
        BatchIssuanceInfoPreviewState(
            uiState = previewNormalUsage,
            toastEvent = BatchIssuanceToastEvent.RENEWAL_SUCCESS,
        ),
    )
}

@WalletDefaultPreview
@Composable
private fun BatchIssuanceInfoScreenPreview(
    @PreviewParameter(BatchIssuanceInfoPreviewParams::class) state: BatchIssuanceInfoPreviewState,
) {
    WalletTheme {
        BatchIssuanceInfoScreen(
            uiState = state.uiState,
            isRenewing = state.isRenewing,
            toastEvent = state.toastEvent,
            onMoreInfo = {},
            onRenew = {},
        )
    }
}

@WalletAllScreenPreview
@Composable
private fun BatchIssuanceInfoScreenExhaustedPreview() {
    WalletTheme {
        BatchIssuanceInfoScreen(
            uiState = previewExhaustedUsage,
            isRenewing = false,
            toastEvent = null,
            onMoreInfo = {},
            onRenew = {},
        )
    }
}

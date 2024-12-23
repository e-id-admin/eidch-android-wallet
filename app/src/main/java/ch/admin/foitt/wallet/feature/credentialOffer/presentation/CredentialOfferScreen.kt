package ch.admin.foitt.wallet.feature.credentialOffer.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.core.layout.WindowWidthSizeClass
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.feature.credentialOffer.presentation.model.CredentialOfferUiState
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.InvitationHeader
import ch.admin.foitt.wallet.platform.actorMetadata.presentation.model.ActorUiState
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.HiddenScrollToButton
import ch.admin.foitt.wallet.platform.composables.HiddenScrollToTopButton
import ch.admin.foitt.wallet.platform.composables.LoadingOverlay
import ch.admin.foitt.wallet.platform.composables.presentation.HeightReportingLayout
import ch.admin.foitt.wallet.platform.composables.presentation.horizontalSafeDrawing
import ch.admin.foitt.wallet.platform.composables.presentation.verticalSafeDrawing
import ch.admin.foitt.wallet.platform.credential.presentation.MediumCredentialCard
import ch.admin.foitt.wallet.platform.credential.presentation.credentialClaimItems
import ch.admin.foitt.wallet.platform.credential.presentation.mock.CredentialMocks
import ch.admin.foitt.wallet.platform.credential.presentation.model.CredentialCardState
import ch.admin.foitt.wallet.platform.navArgs.domain.model.CredentialOfferNavArg
import ch.admin.foitt.wallet.platform.preview.AllCompactScreensPreview
import ch.admin.foitt.wallet.platform.preview.AllLargeScreensPreview
import ch.admin.foitt.wallet.platform.trustRegistry.domain.model.TrustStatus
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme
import com.ramcosta.composedestinations.annotation.Destination

@Composable
@Destination(
    navArgsDelegate = CredentialOfferNavArg::class
)
fun CredentialOfferScreen(
    viewModel: CredentialOfferViewModel,
) {
    BackHandler {
        viewModel.onDeclineClicked()
    }

    CredentialOfferScreenContent(
        isLoading = viewModel.isLoading.collectAsStateWithLifecycle().value,
        credentialOfferUiState = viewModel.credentialOfferUiState.collectAsStateWithLifecycle().value,
        onAccept = viewModel::onAcceptClicked,
        onDecline = viewModel::onDeclineClicked,
        onWrongData = viewModel::onWrongDataClicked,
    )
}

@Composable
private fun CredentialOfferScreenContent(
    isLoading: Boolean,
    credentialOfferUiState: CredentialOfferUiState,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onWrongData: () -> Unit,
) = Box(modifier = Modifier.fillMaxSize()) {
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    if (windowSizeClass.windowWidthSizeClass == WindowWidthSizeClass.COMPACT) {
        CompactContent(
            credentialOffer = credentialOfferUiState,
            onAccept = onAccept,
            onDecline = onDecline,
            onWrongData = onWrongData,
        )
    } else {
        LargeContent(
            credentialOffer = credentialOfferUiState,
            onAccept = onAccept,
            onDecline = onDecline,
            onWrongData = onWrongData,
        )
    }
    LoadingOverlay(showOverlay = isLoading)
}

@Composable
private fun CompactContent(
    credentialOffer: CredentialOfferUiState,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onWrongData: () -> Unit,
) {
    val lazyListState = rememberLazyListState()
    HiddenScrollToButton(
        text = stringResource(id = R.string.tk_receive_approval_hiddenlink_text),
        lazyListState = lazyListState,
        index = 3,
    )
    HiddenScrollToTopButton(
        text = stringResource(id = R.string.tk_receive_jump_to_top),
        lazyListState = lazyListState,
    )
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        state = lazyListState,
        contentPadding = PaddingValues(bottom = Sizes.s06),
    ) {
        item {
            Spacer(Modifier.windowInsetsTopHeight(WindowInsets.safeDrawing))
        }
        item {
            Spacer(modifier = Modifier.height(Sizes.s06))
            InvitationHeader(
                modifier = Modifier.padding(horizontal = Sizes.s04),
                inviterName = credentialOffer.issuer.name,
                inviterImage = credentialOffer.issuer.painter,
                trustStatus = credentialOffer.issuer.trustStatus,
            )
            Spacer(modifier = Modifier.height(Sizes.s06))
        }
        item {
            WalletTexts.BodyLarge(
                text = pluralStringResource(id = R.plurals.tk_receive_approval_android_subtitle, count = 1, 1),
                modifier = Modifier.padding(horizontal = Sizes.s04)
            )
            Spacer(modifier = Modifier.height(Sizes.s04))
        }

        item {
            CredentialBoxWithButtons(
                credential = credentialOffer.credential,
                onAccept = onAccept,
                onDecline = onDecline,
            )
            Spacer(modifier = Modifier.height(Sizes.s06))
        }

        credentialClaimItems(
            title = R.string.tk_displaydelete_displaycredential1_title2,
            claims = credentialOffer.claims,
            issuer = credentialOffer.issuer.name,
            issuerIcon = credentialOffer.issuer.painter,
            onWrongData = onWrongData,
        )

        item {
            CredentialOfferButtons(
                modifier = Modifier
                    .padding(horizontal = Sizes.s04)
                    .padding(top = Sizes.s10),
                onAccept = onAccept,
                onDecline = onDecline,
            )
        }

        item {
            Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.safeDrawing))
        }
    }
}

@Composable
private fun CredentialBoxWithButtons(
    credential: CredentialCardState,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = WalletTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(Sizes.credentialCardCorner),
            )
            .padding(vertical = Sizes.s10, horizontal = Sizes.s04),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        MediumCredentialCard(
            modifier = Modifier.padding(horizontal = Sizes.s10),
            credentialCardState = credential,
        )
        Spacer(modifier = Modifier.height(Sizes.s06))
        CredentialOfferButtons(
            onAccept = onAccept,
            onDecline = onDecline,
        )
    }
}

@Composable
private fun LargeContent(
    credentialOffer: CredentialOfferUiState,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onWrongData: () -> Unit,
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.horizontalSafeDrawing()) {
            Spacer(modifier = Modifier.width(Sizes.s04))
            CredentialBox(
                modifier = Modifier.width(this@BoxWithConstraints.maxWidth * 0.33f),
                credentialOffer = credentialOffer,
            )
            Spacer(modifier = Modifier.width(Sizes.s04))
            DetailsWithHeader(
                credentialOffer = credentialOffer,
                onAccept = onAccept,
                onDecline = onDecline,
                onWrongData = onWrongData,
            )
        }
    }
}

@Composable
private fun CredentialBox(
    modifier: Modifier = Modifier,
    credentialOffer: CredentialOfferUiState,
) {
    Box(
        modifier = modifier
            .verticalSafeDrawing()
            .padding(vertical = Sizes.s02),
    ) {
        Box(
            modifier = Modifier
                .background(
                    color = WalletTheme.colorScheme.surfaceContainerHigh,
                    shape = RoundedCornerShape(Sizes.credentialCardCorner),
                )
                .padding(Sizes.s04),
        ) {
            MediumCredentialCard(
                credentialCardState = credentialOffer.credential,
                isScrollingEnabled = true,
            )
        }
    }
}

@Composable
private fun DetailsWithHeader(
    credentialOffer: CredentialOfferUiState,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    onWrongData: () -> Unit,
) {
    val lazyListState = rememberLazyListState()
    val stickyBottomHeight = remember { mutableStateOf(0.dp) }
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            state = lazyListState,
            contentPadding = PaddingValues(bottom = Sizes.s02 + stickyBottomHeight.value),
        ) {
            item {
                Column {
                    // Only working way to add top insets, windowInsetPaddings did not work
                    Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.safeDrawing))
                    InvitationHeader(
                        modifier = Modifier.padding(
                            start = Sizes.s04,
                            top = Sizes.s02,
                            end = Sizes.s04,
                            bottom = Sizes.s06,
                        ),
                        inviterName = credentialOffer.issuer.name,
                        inviterImage = credentialOffer.issuer.painter,
                        trustStatus = credentialOffer.issuer.trustStatus,
                    )
                }
            }

            item {
                WalletTexts.BodyLarge(
                    text = pluralStringResource(id = R.plurals.tk_receive_approval_android_subtitle, count = 1, 1),
                    modifier = Modifier.padding(horizontal = Sizes.s04)
                )
                Spacer(modifier = Modifier.height(Sizes.s04))
            }

            credentialClaimItems(
                title = R.string.tk_displaydelete_displaycredential1_title2,
                claims = credentialOffer.claims,
                issuer = credentialOffer.issuer.name,
                issuerIcon = credentialOffer.issuer.painter,
                onWrongData = onWrongData,
            )
            item {
                Spacer(modifier = Modifier.height(Sizes.s06))
            }
        }
        StickyButtons(
            modifier = Modifier.align(Alignment.BottomCenter),
            stickyBottomHeight,
            onDecline,
            onAccept
        )
        HiddenScrollToTopButton(
            text = stringResource(id = R.string.tk_receive_jump_to_top),
            lazyListState = lazyListState,
        )
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun StickyButtons(
    modifier: Modifier = Modifier,
    stickyBottomHeight: MutableState<Dp>,
    onDecline: () -> Unit,
    onAccept: () -> Unit
) {
    HeightReportingLayout(
        modifier = modifier,
        onContentHeightMeasured = { height -> stickyBottomHeight.value = height },
    ) {
        FlowRow(
            modifier = Modifier
                .background(WalletTheme.colorScheme.surface.copy(alpha = 0.85f))
                .fillMaxWidth()
                .padding(top = Sizes.s04, end = Sizes.s04, bottom = Sizes.s02)
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                .focusGroup(),
            horizontalArrangement = Arrangement.spacedBy(Sizes.s02, Alignment.End),
            verticalArrangement = Arrangement.spacedBy(Sizes.s02, Alignment.Top),
            maxItemsInEachRow = 2,
        ) {
            Buttons.FilledPrimary(
                modifier = Modifier.weight(1f),
                text = stringResource(id = R.string.tk_global_decline_secondarybutton),
                startIcon = painterResource(id = R.drawable.wallet_ic_cross),
                onClick = onDecline,
            )
            Buttons.FilledTertiary(
                modifier = Modifier.weight(1f),
                text = stringResource(id = R.string.tk_global_add_primarybutton),
                startIcon = painterResource(id = R.drawable.wallet_ic_checkmark),
                onClick = onAccept,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CredentialOfferButtons(
    modifier: Modifier = Modifier,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
) {
    FlowRow(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Sizes.s02, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(Sizes.s02, Alignment.Top),
        maxItemsInEachRow = 2,
    ) {
        Buttons.FilledPrimary(
            modifier = Modifier.weight(1f),
            text = stringResource(id = R.string.tk_global_decline_secondarybutton),
            startIcon = painterResource(id = R.drawable.wallet_ic_cross),
            onClick = onDecline,
        )
        Buttons.FilledTertiary(
            modifier = Modifier.weight(1f),
            text = stringResource(id = R.string.tk_global_add_primarybutton),
            startIcon = painterResource(id = R.drawable.wallet_ic_checkmark),
            onClick = onAccept,
        )
    }
}

@AllCompactScreensPreview
@Composable
private fun CredentialOfferScreenPreview() {
    WalletTheme {
        CredentialOfferScreenContent(
            isLoading = false,
            credentialOfferUiState = CredentialOfferUiState(
                issuer = ActorUiState(
                    name = "Test Issuer",
                    painter = painterResource(id = R.drawable.pilot_ic_strassenverkehrsamt),
                    trustStatus = TrustStatus.TRUSTED,
                ),
                credential = CredentialMocks.cardState01,
                claims = CredentialMocks.claimList,
            ),
            onAccept = {},
            onDecline = {},
            onWrongData = {},
        )
    }
}

@AllLargeScreensPreview
@Composable
private fun CredentialOfferLargeContentPreview() {
    WalletTheme {
        LargeContent(
            credentialOffer = CredentialOfferUiState(
                issuer = ActorUiState(
                    name = "Test Issuer",
                    painter = painterResource(id = R.drawable.pilot_ic_strassenverkehrsamt),
                    trustStatus = TrustStatus.TRUSTED,
                ),
                credential = CredentialMocks.cardState01,
                claims = CredentialMocks.claimList,
            ),
            onAccept = {},
            onDecline = {},
            onWrongData = {},
        )
    }
}

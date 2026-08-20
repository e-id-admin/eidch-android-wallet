package ch.admin.foitt.wallet.feature.credentialDetail.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.Buttons
import ch.admin.foitt.wallet.platform.composables.presentation.clusterLazyListItem
import ch.admin.foitt.wallet.platform.composables.presentation.horizontalSafeDrawing
import ch.admin.foitt.wallet.platform.composables.presentation.layout.LazyColumn
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.platform.scaffold.presentation.LocalScaffoldPaddings
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun IssuanceInfoScreen(
    viewModel: IssuanceInfoViewModel
) {
    IssuanceInfoScreenContent(
        onMoreInfo = viewModel::onOpenMoreInfo,
    )
}

@Composable
private fun IssuanceInfoScreenContent(
    onMoreInfo: () -> Unit
) {
    Box(
        modifier = Modifier
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

            standardIssuanceInfoItem(
                onMoreInfo = onMoreInfo
            )
        }
    }
}

private fun LazyListScope.standardIssuanceInfoItem(
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
                painter = painterResource(R.drawable.wallet_ic_id_card),
                contentDescription = null,
            )

            Spacer(Modifier.height(Sizes.s04))

            WalletTexts.TitleMedium(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { heading() },
                text = stringResource(R.string.tk_credentialDetail_standardIssuanceInfo_title),
                textAlign = TextAlign.Start,
            )

            Spacer(Modifier.height(Sizes.s01))

            WalletTexts.BodyMedium(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.tk_credentialDetail_standardIssuanceInfo_body),
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

@WalletAllScreenPreview
@Composable
private fun IssuanceInfoScreenContentPreview() {
    WalletTheme {
        IssuanceInfoScreenContent(
            onMoreInfo = {}
        )
    }
}

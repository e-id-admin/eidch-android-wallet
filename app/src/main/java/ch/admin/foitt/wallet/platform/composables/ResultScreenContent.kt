package ch.admin.foitt.wallet.platform.composables

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme
import ch.admin.foitt.wallet.theme.errorBackgroundDark
import ch.admin.foitt.wallet.theme.errorBackgroundLight

@Composable
internal fun ResultScreenContent(
    @DrawableRes iconRes: Int,
    dateTime: String,
    message: String,
    topColor: Color,
    bottomColor: Color,
    bottomContent: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
            .background(topColor)
    ) {
        val (
            topSpacer,
            mainContent,
            bottomSpacer,
            stickyBottom,
        ) = createRefs()

        SpacerTop(
            backgroundColor = topColor,
            useStatusBarInsets = true,
            modifier = Modifier
                .constrainAs(topSpacer) {
                    top.linkTo(parent.top)
                }
        )
        MainBlock(
            iconRes = iconRes,
            dateTime = dateTime,
            message = message,
            topColor = topColor,
            bottomColor = bottomColor,
            modifier = Modifier
                .constrainAs(mainContent) {
                    top.linkTo(parent.top)
                    bottom.linkTo(stickyBottom.top)
                    height = Dimension.fillToConstraints
                },
            content = content,
        )
        SpacerBottom(
            backgroundColor = bottomColor,
            modifier = Modifier
                .constrainAs(bottomSpacer) {
                    bottom.linkTo(stickyBottom.top)
                }
        )
        BottomBlock(
            backgroundColor = bottomColor,
            modifier = Modifier
                .constrainAs(stickyBottom) {
                    bottom.linkTo(parent.bottom)
                },
            content = bottomContent,
        )
    }
}

@Composable
private fun MainBlock(
    @DrawableRes iconRes: Int,
    dateTime: String,
    message: String,
    topColor: Color,
    bottomColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .background(
                brush = Brush.verticalGradient(
                    listOf(topColor, bottomColor,)
                )
            )
            .verticalScroll(rememberScrollState())
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = Sizes.s04, end = Sizes.s04, top = Sizes.s16, bottom = Sizes.s06),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onError,
            modifier = Modifier.size(Sizes.s20),
        )
        Spacer(modifier = Modifier.size(Sizes.s04))
        WalletTexts.BodySmallCentered(
            text = dateTime,
            color = MaterialTheme.colorScheme.onError,
        )
        Spacer(modifier = Modifier.size(Sizes.s01))
        WalletTexts.TitleScreen(
            text = message,
            color = MaterialTheme.colorScheme.onError,
        )
        Spacer(modifier = Modifier.size(Sizes.s06))
        content()
    }
}

@Composable
private fun BottomBlock(
    modifier: Modifier,
    backgroundColor: Color,
    content: @Composable () -> Unit,
) = Column(
    modifier = modifier
        .background(backgroundColor)
        .fillMaxWidth()
        .padding(start = Sizes.s04, end = Sizes.s04, bottom = Sizes.s04)
        .navigationBarsPadding()
) {
    content()
}

private val longText by lazy {
    "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, " +
        "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna " +
        "aliquyam erat, sed diam voluptua. Lorem ipsum dolor sit amet, " +
        "consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt " +
        "ut labore et dolore magna aliquyam erat, sed diam voluptua. " +
        "Lorem ipsum dolor sit amet, consetetur sadipscing elitr, " +
        "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, " +
        "sed diam voluptua. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, " +
        "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, " +
        "sed diam voluptua. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, " +
        "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, " +
        "sed diam voluptua. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, " +
        "sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, " +
        "sed diam voluptua."
}

@Composable
@WalletAllScreenPreview
private fun ResultScreenPreview() {
    WalletTheme {
        ResultScreenContent(
            iconRes = R.drawable.pilot_ic_warning_big,
            dateTime = "10th December 1990 | 11:17",
            message = longText,
            content = {
                WalletTexts.BodySmallCentered(
                    text = stringResource(id = R.string.presentation_error_message),
                    color = MaterialTheme.colorScheme.onError,
                )
            },
            bottomContent = {
                Buttons.FilledPrimary(
                    text = stringResource(id = R.string.global_error_retry_button),
                    onClick = { },
                    startIcon = painterResource(id = R.drawable.pilot_ic_retry)
                )
            },
            topColor = MaterialTheme.colorScheme.errorBackgroundLight,
            bottomColor = MaterialTheme.colorScheme.errorBackgroundDark,
        )
    }
}

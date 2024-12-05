package ch.admin.foitt.wallet.platform.composables

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.preview.WalletAllScreenPreview
import ch.admin.foitt.wallet.theme.Gradients
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTexts
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
internal fun SimpleScreenContent(
    @DrawableRes icon: Int,
    titleText: String,
    mainText: String,
    bottomBlockContent: @Composable ColumnScope.() -> Unit,
) {
    SimpleScreenContent(
        icon = icon,
        titleText = titleText,
        mainContent = {
            WalletTexts.Body(
                text = mainText,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        bottomBlockContent = bottomBlockContent
    )
}

@Composable
internal fun SimpleScreenContent(
    @DrawableRes icon: Int,
    titleText: String,
    mainContent: @Composable ColumnScope.() -> Unit,
    bottomBlockContent: @Composable ColumnScope.() -> Unit,
) {
    ConstraintLayout(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val (
            topSpacerRef,
            mainContentRef,
            bottomSpacerRef,
            stickyBottomRef,
        ) = createRefs()

        Spacer(
            modifier = Modifier
                .zIndex(1.0f)
                .fillMaxWidth()
                .background(
                    brush = Gradients.topFadingBrush(MaterialTheme.colorScheme.background)
                )
                .defaultMinSize(minHeight = Sizes.s06)
                .statusBarsPadding()
                .constrainAs(topSpacerRef) {
                    top.linkTo(parent.top)
                }
        )
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(start = Sizes.s06, end = Sizes.s06, top = Sizes.s16, bottom = Sizes.s06)
                .navigationBarsPadding()
                .constrainAs(mainContentRef) {
                    top.linkTo(parent.top)
                    bottom.linkTo(stickyBottomRef.top)
                    height = Dimension.fillToConstraints
                },
        ) {
            Image(
                modifier = Modifier.fillMaxWidth(),
                painter = painterResource(id = icon),
                contentDescription = null,
                alignment = Alignment.Center,
                contentScale = ContentScale.Fit,
            )
            Spacer(modifier = Modifier.height(Sizes.s04))
            WalletTexts.TitleScreen(
                text = titleText,
            )
            Spacer(modifier = Modifier.height(Sizes.s06))
            mainContent()
        }
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Gradients.bottomFadingBrush(MaterialTheme.colorScheme.background)
                )
                .defaultMinSize(minHeight = Sizes.s04)
                .constrainAs(bottomSpacerRef) {
                    bottom.linkTo(stickyBottomRef.top)
                }
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Sizes.s04, end = Sizes.s04, bottom = Sizes.s04)
                .constrainAs(stickyBottomRef) {
                    bottom.linkTo(parent.bottom)
                }
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(Sizes.s04),
        ) {
            bottomBlockContent()
        }
    }
}

@WalletAllScreenPreview
@Composable
private fun SimpleScreenContentPreview() {
    WalletTheme {
        SimpleScreenContent(
            icon = R.drawable.wallet_ic_qr,
            titleText = "My Title",
            mainText = "The main text Donec ullamcorper augue quis egestas ultricies. " +
                "Phasellus nec tortor et risus ullamcorper volutpat eget nec metus. " +
                "Pellentesque ut dui pellentesque, cursus nunc mollis, convallis sapien. " +
                "Nam id quam nunc. Donec ut mauris mi. Duis sem mi, luctus sed vehicula ut, " +
                "rutrum at augue. Excepteur sint occaecat cupidatat non proident, " +
                "sunt in culpa qui officia deserunt mollit anim id est laborum." +
                "Phasellus nec tortor et risus ullamcorper volutpat eget nec metus.",
            bottomBlockContent = {
                Buttons.Outlined(
                    text = "I got it",
                    onClick = {},
                )
            }
        )
    }
}

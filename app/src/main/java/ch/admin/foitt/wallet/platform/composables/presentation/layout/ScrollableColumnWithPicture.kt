package ch.admin.foitt.wallet.platform.composables.presentation.layout

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowWidthSizeClass
import ch.admin.foitt.wallet.platform.composables.ScalableContentLayout
import ch.admin.foitt.wallet.platform.composables.presentation.scrollingBehavior
import ch.admin.foitt.wallet.theme.Sizes

/**
 * Standard Wallet Layout that wraps content in a scrollable [Column].
 * It handles:
 * * the scrolling behavior, including the various paddings
 * * the orientation changes
 * * the various insets (the status, navigation and sticky contents)
 */
@Composable
fun WalletLayouts.ScrollableColumnWithPicture(
    modifier: Modifier = Modifier,
    windowSizeClass: WindowWidthSizeClass = currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass,
    stickyBottomContent: (@Composable () -> Unit)?,
    stickyStartContent: @Composable ColumnScope.() -> Unit,
    contentScrollState: ScrollState = rememberScrollState(),
    contentPadding: PaddingValues = PaddingValues(
        start = Sizes.s04,
        end = Sizes.s04,
        bottom = Sizes.s06
    ),
    content: @Composable ColumnScope.() -> Unit,
) {
    var bottomBlockHeightDp by remember {
        mutableStateOf(0.dp)
    }
    when (windowSizeClass) {
        WindowWidthSizeClass.COMPACT -> CompactContainer(
            onBottomHeightMeasured = { height -> bottomBlockHeightDp = height },
            modifier = modifier,
            stickyBottomContent = stickyBottomContent,
            useStatusBarInsets = false,
            useNavigationBarInsets = stickyBottomContent == null,
        ) {
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize()
            ) {
                Column(
                    modifier = Modifier
                        .scrollingBehavior(
                            useStatusBarInsets = false,
                            contentPadding = PaddingValues(),
                            scrollState = contentScrollState,
                        )
                        .padding(bottom = bottomBlockHeightDp),
                ) {
                    ScalableContentLayout(
                        height = this@BoxWithConstraints.maxHeight,
                        scalableContentIndex = 0,
                        minScalableHeight = Sizes.mainCardMinHeight,
                        maxScalableHeight = this@BoxWithConstraints.maxHeight * cardLargeScreenRatio,
                        stickyContentHeight = bottomBlockHeightDp,
                    ) {
                        stickyStartContent()
                        Column(
                            modifier = Modifier.padding(contentPadding)
                        ) {
                            content()
                        }
                    }
                }
            }
        }
        else -> LargeContainer(
            modifier = modifier,
            onBottomHeightMeasured = { height -> bottomBlockHeightDp = height },
            stickyBottomContent = stickyBottomContent,
            stickyStartContent = stickyStartContent,
            contentScrollState = contentScrollState,
        ) {
            Column(
                modifier = Modifier
                    .scrollingBehavior(
                        useStatusBarInsets = false,
                        contentPadding = contentPadding,
                        scrollState = contentScrollState,
                    )
                    .padding(bottom = bottomBlockHeightDp),
            ) {
                content()
            }
        }
    }
}

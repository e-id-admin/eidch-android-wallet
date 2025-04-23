package ch.admin.foitt.wallet.platform.composables.presentation.layout

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.constraintlayout.compose.Dimension
import androidx.window.core.layout.WindowWidthSizeClass
import ch.admin.foitt.wallet.platform.composables.ScalableContentLayout
import ch.admin.foitt.wallet.theme.Sizes

/**
 * Standard Wallet Layout that wraps content in a scrollable [Column].
 * It handles:
 * * the scrolling behavior, including the various paddings
 * * the orientation changes
 * * the various insets (the status, navigation and sticky contents)
 */
@Composable
fun WalletLayouts.ScrollableColumnSimple(
    modifier: Modifier = Modifier,
    windowSizeClass: WindowWidthSizeClass = currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass,
    useCompactScalable: Boolean = true,
    isStickyStartScrollable: Boolean,
    stickyStartContent: @Composable ColumnScope.() -> Unit,
    stickyBottomHorizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(Sizes.s02, Alignment.CenterHorizontally),
    stickyBottomBackgroundColor: Color = Color.Transparent,
    stickyBottomContent: (@Composable RowScope.() -> Unit)?,
    centerContent: Boolean = false,
    contentScrollState: ScrollState = rememberScrollState(),
    contentPadding: PaddingValues = PaddingValues(
        bottom = Sizes.s06
    ),
    content: @Composable ColumnScope.() -> Unit,
) {
    var bottomBlockHeightDp by remember {
        mutableStateOf(Sizes.s00)
    }
    val contentHeightDimension = if (centerContent) {
        Dimension.wrapContent
    } else {
        Dimension.fillToConstraints
    }

    when (windowSizeClass) {
        WindowWidthSizeClass.COMPACT -> CompactContainer(
            modifier = modifier,
            contentHeightDimension = contentHeightDimension,
            stickyBottomHorizontalArrangement = stickyBottomHorizontalArrangement,
            stickyBottomBackgroundColor = stickyBottomBackgroundColor,
            stickyBottomContent = stickyBottomContent,
            scrollState = contentScrollState,
            onBottomHeightMeasured = { height -> bottomBlockHeightDp = height },
        ) { boxWithConstraintsScope ->
            if (useCompactScalable) {
                ScalableContentLayout(
                    height = boxWithConstraintsScope.maxHeight,
                    scalableContentIndex = 0,
                    minScalableHeight = Sizes.mainCardMinHeight,
                    maxScalableHeight = boxWithConstraintsScope.maxHeight * cardLargeScreenRatio,
                    stickyContentHeight = bottomBlockHeightDp,
                ) {
                    stickyStartContent()
                    Column(
                        modifier = Modifier.padding(contentPadding)
                    ) {
                        content()
                    }
                }
            } else {
                stickyStartContent()
                Column(
                    modifier = Modifier.padding(contentPadding)
                ) {
                    content()
                }
            }
        }

        else -> LargeContainer(
            onBottomHeightMeasured = { height -> bottomBlockHeightDp = height },
            isStickyStartScrollable = isStickyStartScrollable,
            stickyBottomHorizontalArrangement = stickyBottomHorizontalArrangement,
            stickyBottomBackgroundColor = stickyBottomBackgroundColor,
            stickyBottomContent = stickyBottomContent,
            stickyStartContent = stickyStartContent,
            contentHeightDimension = contentHeightDimension,
            contentScrollState = contentScrollState,
            contentPadding = PaddingValues(
                top = (
                    if (centerContent) {
                        bottomBlockHeightDp
                    } else {
                        Sizes.s00
                    }
                    ),
                start = Sizes.s04,
            )
        ) {
            content()
        }
    }
}

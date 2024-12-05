package ch.admin.foitt.wallet.feature.home.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowWidthSizeClass
import ch.admin.foitt.wallet.feature.home.presentation.composables.HomeBarHorizontal
import ch.admin.foitt.wallet.feature.home.presentation.composables.HomeBarVertical
import ch.admin.foitt.wallet.platform.composables.presentation.HeightReportingLayout
import ch.admin.foitt.wallet.platform.composables.presentation.bottomSafeDrawing
import ch.admin.foitt.wallet.platform.composables.presentation.endSafeDrawing
import ch.admin.foitt.wallet.platform.composables.presentation.layout.WalletLayouts
import ch.admin.foitt.wallet.platform.composables.presentation.startSafeDrawing
import ch.admin.foitt.wallet.platform.composables.presentation.verticalSafeDrawing
import ch.admin.foitt.wallet.platform.utils.TraversalIndex
import ch.admin.foitt.wallet.platform.utils.setIsTraversalGroup
import ch.admin.foitt.wallet.theme.Sizes

@Composable
fun WalletLayouts.HomeContainer(
    onMenu: () -> Unit,
    onScan: () -> Unit,
    windowWidthClass: WindowWidthSizeClass,
    content: @Composable BoxScope.(stickyBottomHeightDp: Dp) -> Unit,
) = when (windowWidthClass) {
    WindowWidthSizeClass.COMPACT -> HomeCompactContainer(
        onScan = onScan,
        onMenu = onMenu,
        content = content,
    )
    else -> HomeLargeContainer(
        onScan = onScan,
        onMenu = onMenu,
        content = content,
    )
}

@Composable
private fun HomeCompactContainer(
    onScan: () -> Unit,
    onMenu: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.(stickyBottomHeightDp: Dp) -> Unit,
) = Box(
    modifier = modifier.fillMaxSize()
) {
    var reportedBlockHeight by remember {
        mutableStateOf(0.dp)
    }

    content(reportedBlockHeight)

    HomeCompactStickyBottom(
        onContentHeightMeasured = { height -> reportedBlockHeight = height },
        onScan = onScan,
        onMenu = onMenu,
        modifier = Modifier
            .align(Alignment.BottomCenter)
    )
}

@Composable
private fun HomeCompactStickyBottom(
    onContentHeightMeasured: (stickyBottomHeight: Dp) -> Unit,
    onScan: () -> Unit,
    onMenu: () -> Unit,
    modifier: Modifier,
) = HeightReportingLayout(
    modifier = modifier,
    onContentHeightMeasured = onContentHeightMeasured,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(bottom = Sizes.s06)
            .bottomSafeDrawing(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        HomeBarHorizontal(
            onScan = onScan,
            onMenu = onMenu,
            modifier = Modifier.setIsTraversalGroup(index = TraversalIndex.HIGH3)
        )
    }
}

@Composable
private fun HomeLargeContainer(
    onScan: () -> Unit,
    onMenu: () -> Unit,
    content: @Composable BoxScope.(stickyBottomHeightDp: Dp) -> Unit,
) = Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.Center,
    modifier = Modifier.fillMaxSize()
) {
    HomeBarVertical(
        onScan = onScan,
        onMenu = onMenu,
        modifier = Modifier
            .setIsTraversalGroup(index = TraversalIndex.HIGH3)
            .padding(horizontal = Sizes.s02)
            .verticalSafeDrawing()
            .startSafeDrawing()
    )
    Box(
        modifier = Modifier
            .weight(1f)
            .endSafeDrawing()
    ) {
        content(0.dp)
    }
}

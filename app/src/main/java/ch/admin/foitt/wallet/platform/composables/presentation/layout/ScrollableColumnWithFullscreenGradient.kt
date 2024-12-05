package ch.admin.foitt.wallet.platform.composables.presentation.layout

import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import ch.admin.foitt.wallet.platform.composables.presentation.bottomSafeDrawing
import ch.admin.foitt.wallet.platform.composables.presentation.topSafeDrawing
import ch.admin.foitt.wallet.platform.scaffold.presentation.FullscreenGradient
import ch.admin.foitt.wallet.theme.Sizes

@Composable
fun WalletLayouts.ScrollableColumnWithFullscreenGradient(
    modifier: Modifier = Modifier,
    stickyBottomPadding: PaddingValues = PaddingValues(
        start = paddingStickyMedium,
        end = paddingStickyMedium,
        bottom = paddingStickyMedium,
    ),
    stickyBottomContent: (@Composable () -> Unit)?,
    scrollableContent: @Composable ColumnScope.() -> Unit,
) {
    FullscreenGradient()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x3618132B))
    ) // darken gradient to make text better accessible

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Sizes.s06)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .topSafeDrawing()
                .padding(vertical = Sizes.s04)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
        ) {
            scrollableContent()
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(stickyBottomPadding)
                .bottomSafeDrawing()
                .focusGroup(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            stickyBottomContent?.invoke()
        }
    }
}

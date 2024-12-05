package ch.admin.foitt.wallet.platform.composables.presentation.layout

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ch.admin.foitt.wallet.platform.composables.presentation.scrollingBehavior
import ch.admin.foitt.wallet.theme.Sizes

@Composable
fun WalletLayouts.ScrollableColumn(
    modifier: Modifier = Modifier,
    useStatusBarInsets: Boolean = true,
    useNavigationBarInsets: Boolean = false,
    stickyBottomPadding: PaddingValues = stickyBottomPaddingValuesPortrait,
    stickyBottomContent: (@Composable () -> Unit)?,
    contentPadding: PaddingValues = PaddingValues(
        bottom = Sizes.s06
    ),
    contentScrollState: ScrollState = rememberScrollState(),
    scrollableContent: @Composable ColumnScope.() -> Unit,
) = CompactContainer(
    modifier = modifier,
    onBottomHeightMeasured = null,
    stickyBottomPadding = stickyBottomPadding,
    stickyBottomContent = stickyBottomContent,
    useStatusBarInsets = useStatusBarInsets,
    useNavigationBarInsets = useNavigationBarInsets,
) {
    Column(
        modifier = Modifier.scrollingBehavior(
            useStatusBarInsets = useStatusBarInsets,
            contentPadding = contentPadding,
            scrollState = contentScrollState,
        ),
    ) {
        scrollableContent()
    }
}

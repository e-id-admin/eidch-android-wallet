package ch.admin.foitt.wallet.platform.composables.presentation.layout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTheme

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
    stickyBottomBackgroundColor: Color = WalletTheme.colorScheme.surface.copy(alpha = 0.85f),
    stickyBottomContent: (@Composable () -> Unit)?,
    stickyStartContent: @Composable ColumnScope.() -> Unit,
    contentPadding: PaddingValues = PaddingValues(
        start = Sizes.s04,
        end = Sizes.s04,
        bottom = Sizes.s06
    ),
    content: @Composable ColumnScope.() -> Unit,
) = ScrollableColumnSimple(
    modifier = modifier,
    stickyBottomHorizontalArrangement = Arrangement.spacedBy(Sizes.s02, Alignment.End),
    stickyBottomBackgroundColor = stickyBottomBackgroundColor,
    stickyBottomContent = stickyBottomContent,
    isStickyStartScrollable = false,
    stickyStartContent = stickyStartContent,
    contentPadding = contentPadding,
    content = content,
)

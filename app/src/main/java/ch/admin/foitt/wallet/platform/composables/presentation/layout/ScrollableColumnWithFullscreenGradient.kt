package ch.admin.foitt.wallet.platform.composables.presentation.layout

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.window.core.layout.WindowWidthSizeClass
import ch.admin.foitt.wallet.platform.composables.LoadingOverlay
import ch.admin.foitt.wallet.platform.scaffold.presentation.FullscreenGradient
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun WalletLayouts.ScrollableColumnWithFullscreenGradient(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    stickyBottomContent: (@Composable () -> Unit)?,
    scrollableContent: @Composable ColumnScope.() -> Unit,
) {
    FullscreenGradient()
    when (currentWindowAdaptiveInfo().windowSizeClass.windowWidthSizeClass) {
        WindowWidthSizeClass.COMPACT -> CompactContainerFloatingBottom(
            modifier = modifier,
            content = {
                scrollableContent()
            },
            stickyBottomContent = {
                stickyBottomContent?.let {
                    stickyBottomContent()
                }
            }
        )
        else -> LargeContainerFloatingBottom(
            modifier = modifier,
            content = {
                scrollableContent()
            },
            stickyBottomContent = {
                stickyBottomContent?.let {
                    stickyBottomContent()
                }
            }
        )
    }

    LoadingOverlay(
        showOverlay = isLoading,
        color = WalletTheme.colorScheme.primaryFixed,
    )
}

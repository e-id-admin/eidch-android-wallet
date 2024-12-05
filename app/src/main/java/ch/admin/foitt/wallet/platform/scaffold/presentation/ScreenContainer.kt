package ch.admin.foitt.wallet.platform.scaffold.presentation

import android.content.res.Configuration
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalConfiguration
import ch.admin.foitt.wallet.platform.scaffold.domain.model.FullscreenState
import ch.admin.foitt.wallet.platform.utils.TraversalIndex
import ch.admin.foitt.wallet.platform.utils.setIsTraversalGroup
import ch.admin.foitt.wallet.theme.WalletTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun ScreenContainer(
    fullscreenState: FullscreenState,
    content: @Composable BoxScope.() -> Unit,
) {
    val currentConfig = LocalConfiguration.current
    val scrollBehavior = if (currentConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        TopAppBarDefaults.enterAlwaysScrollBehavior(
            state = rememberTopAppBarState(),
        )
    } else {
        TopAppBarDefaults.pinnedScrollBehavior()
    }

    val (focus01Ref, focus02Ref) = FocusRequester.createRefs()

    Scaffold(
        modifier = Modifier
            .then(
                if (fullscreenState == FullscreenState.Fullscreen) {
                    Modifier
                } else {
                    Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
                }
            )
            .setIsTraversalGroup(),
        topBar = {
            when (fullscreenState) {
                FullscreenState.Fullscreen -> {}
                FullscreenState.Insets -> {
                    Box(
                        modifier = Modifier
                            .setIsTraversalGroup(index = TraversalIndex.LOW1)
                            .focusGroup()
                            .focusRequester(focus02Ref)
                            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                    ) {
                        WalletTopBar(scrollBehavior = scrollBehavior)
                    }
                }
            }
        },
        content = { paddingValues ->
            val modifier = when (fullscreenState) {
                FullscreenState.Fullscreen -> Modifier.fillMaxSize()
                FullscreenState.Insets -> {
                    Modifier
                        .setIsTraversalGroup(index = TraversalIndex.HIGH5)
                        .focusGroup()
                        .focusRequester(focus01Ref)
                        .focusProperties { next = focus02Ref }
                        .fillMaxSize()
                        .padding(paddingValues = paddingValues)
                        .consumeWindowInsets(paddingValues)
                        .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))
                }
            }
            Box(
                modifier = modifier
            ) {
                content()
            }
        },
        containerColor = WalletTheme.colorScheme.background,
    )
    ErrorDialog()
}

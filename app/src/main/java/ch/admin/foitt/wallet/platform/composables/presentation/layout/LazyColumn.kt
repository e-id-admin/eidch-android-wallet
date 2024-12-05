package ch.admin.foitt.wallet.platform.composables.presentation.layout

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@SuppressLint(
    "ComposableLambdaParameterNaming",
    "ComposableLambdaParameterPosition"
)
@Composable
fun WalletLayouts.LazyColumn(
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    useTopInsets: Boolean = true,
    useBottomInsets: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(
        bottom = paddingContentBottom,
    ),
    lazyListContent: LazyListScope.() -> Unit,
) = androidx.compose.foundation.lazy.LazyColumn(
    modifier = modifier,
    state = state,
    contentPadding = contentPadding,
) {
    if (useTopInsets) {
        item {
            Spacer(
                Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
            )
        }
    }
    lazyListContent()
    if (useBottomInsets) {
        item {
            Spacer(
                Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
            )
        }
    }
}

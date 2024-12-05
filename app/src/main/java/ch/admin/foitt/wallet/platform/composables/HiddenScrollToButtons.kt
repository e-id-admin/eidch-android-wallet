package ch.admin.foitt.wallet.platform.composables

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import ch.admin.foitt.wallet.platform.utils.isScreenReaderOn
import ch.admin.foitt.wallet.theme.Sizes
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Accessibility button that is at the top of the screen usually hidden and is only accessible by
 * focus (tabbing through) and is shown then. If clicked it will jump to the item described by the index.
 */
@Composable
fun HiddenScrollToButton(
    text: String,
    lazyListState: LazyListState,
    index: Int,
) {
    if (LocalContext.current.isScreenReaderOn()) return // hidden buttons when screen reader is on is confusing
    val coroutineScope = rememberCoroutineScope()
    var isButtonVisible by remember { mutableStateOf(false) }
    val topInsets = WindowInsets.safeDrawing.asPaddingValues().calculateTopPadding()
    val isButtonEnabled by remember {
        derivedStateOf(policy = structuralEqualityPolicy()) {
            // check if our item is shown and not just beneath top safe drawing assuming that items are bigger than safe drawing top
            lazyListState.firstVisibleItemIndex < index - 1 ||
                lazyListState.layoutInfo.visibleItemsInfo.any {
                    it.index == index && it.offset > topInsets.value
                }
        }
    }
    Buttons.FilledPrimary(
        modifier = Modifier
            .zIndex(1f)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
            .padding(horizontal = Sizes.s04)
            .then(if (isButtonVisible) Modifier else Modifier.size(1.dp))
            .alpha(if (isButtonVisible) 1f else 0f)
            .onFocusChanged { event ->
                isButtonVisible = event.isFocused
            },
        text = text,
        onClick = {
            coroutineScope.launch {
                lazyListState.animateScrollToItem(index, -topInsets.value.roundToInt())
            }
        },
        enabled = isButtonEnabled,
    )
}

/**
 * Accessibility button that is at the bottom of the screen usually hidden and is only accessible by
 * focus (tabbing through) and is shown then. If clicked it will jump to the top of the list.
 */
@Composable
fun HiddenScrollToTopButton(
    text: String,
    lazyListState: LazyListState,
) {
    if (LocalContext.current.isScreenReaderOn()) return // hidden buttons when screen reader is on is confusing
    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(1f)
    ) {
        val coroutineScope = rememberCoroutineScope()
        var isButtonVisible by remember { mutableStateOf(false) }
        val isButtonEnabled by remember {
            derivedStateOf { lazyListState.canScrollBackward }
        }
        Buttons.FilledPrimary(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                .padding(horizontal = Sizes.s04)
                .align(Alignment.BottomCenter)
                .then(if (isButtonVisible) Modifier else Modifier.size(1.dp))
                .alpha(if (isButtonVisible) 1f else 0f)
                .onFocusChanged { event ->
                    isButtonVisible = event.isFocused
                },
            text = text,
            onClick = {
                coroutineScope.launch {
                    lazyListState.animateScrollToItem(0)
                }
            },
            enabled = isButtonEnabled,
        )
    }
}

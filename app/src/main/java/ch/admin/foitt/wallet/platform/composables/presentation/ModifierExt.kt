@file:Suppress("TooManyFunctions")

package ch.admin.foitt.wallet.platform.composables.presentation

import android.os.SystemClock
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.LayoutDirection
import ch.admin.foitt.wallet.platform.scaffold.presentation.LocalScaffoldPaddings
import ch.admin.foitt.wallet.platform.utils.isScreenReaderOn
import kotlinx.coroutines.delay

@Composable
fun Modifier.addTopScaffoldPadding(): Modifier {
    val topPadding = LocalScaffoldPaddings.current.calculateTopPadding()
    return this
        .padding(top = topPadding)
        .consumeWindowInsets(WindowInsets(top = topPadding))
}

/**
 * Acts as an anchor for talk back which is always focused when view is first composed. Counters the problem that buttons that do not
 * change for talk back (e.g. back button) don't give up focus when screen changes. Does only work with non-focusable composables.
 */
@Composable
fun Modifier.nonFocusableAccessibilityAnchor(): Modifier {
    val context = LocalContext.current
    if (!context.isScreenReaderOn()) return Modifier
    var isFocusable by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        delay(50) // delays are needed so that focus changes are applied
        focusManager.clearFocus(true)
        // make the composable shortly focusable to request focus but re-disable it that it is not focusable by tabbing through
        isFocusable = true
        delay(50)
        focusRequester.requestFocus()
        delay(50)
        isFocusable = false
    }
    return this.focusRequester(focusRequester)
        .focusable(isFocusable)
}

@Composable
fun Modifier.requestFocus(focusRequester: FocusRequester): Modifier {
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    return this.focusRequester(focusRequester)
        .focusable()
}

@Composable
fun Modifier.spaceBarKeyClickable(onSpace: () -> Unit): Modifier = composed {
    var lastSpace = remember {
        0L
    }
    onKeyEvent { keyEvent ->
        if (keyEvent.key == Key.Spacebar && SystemClock.elapsedRealtime() - lastSpace > 500L) {
            onSpace()
            lastSpace = SystemClock.elapsedRealtime()
            true
        } else {
            false
        }
    }
}

@Composable
fun Modifier.horizontalSafeDrawing() = windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))

@Composable
fun Modifier.verticalSafeDrawing() = windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical))

@Composable
fun Modifier.startSafeDrawing() = windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Start))

@Composable
fun Modifier.endSafeDrawing() = windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.End))

@Composable
fun Modifier.topSafeDrawing() = windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))

@Composable
fun Modifier.bottomSafeDrawing() = windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))

@Composable
fun Modifier.centerHorizontallyOnFullscreen() = this.offset(
    x = (
        WindowInsets.safeDrawing.only(WindowInsetsSides.End).asPaddingValues().calculateEndPadding(LayoutDirection.Ltr) -
            WindowInsets.safeDrawing.only(WindowInsetsSides.Start).asPaddingValues().calculateStartPadding(LayoutDirection.Ltr)
        ) / 2
)

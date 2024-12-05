package ch.admin.foitt.wallet.platform.composables.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Dp

/**
 * Wrapper that computes the height of the provided content on screen and reports it through the [onContentHeightMeasured] callback.
 * Note that this only works for one child composable in the content and throws an exception otherwise.
 */
@Composable
fun HeightReportingLayout(
    modifier: Modifier = Modifier,
    onContentHeightMeasured: (Dp) -> Unit,
    content: @Composable () -> Unit,
) {
    Layout(modifier = modifier, content = content) { measurables, constraints ->
        require(measurables.size == 1) { "HeightReportingLayout only supports one child composable" }
        val placeable = measurables[0].measure(constraints)
        onContentHeightMeasured(placeable.height.toDp())
        layout(placeable.width, placeable.height) {
            placeable.place(0, 0)
        }
    }
}

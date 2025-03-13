package ch.admin.foitt.wallet.platform.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ch.admin.foitt.wallet.theme.Sizes

/**
 * Scales one composable in the layout to fit the available vertical space (maxHeight) by going through following procedure:
 *
 * Measure all elements but the scalable composable
 * Calculate how much height the scalable composable can take
 * When measuring the scalable composable, set its height
 * Place every element on the screen
 */
@Composable
internal fun ScalableContentLayout(
    modifier: Modifier = Modifier,
    height: Dp,
    minScalableHeight: Dp = Sizes.loginMethodHeaderMinHeight,
    maxScalableHeight: Dp = Sizes.loginMethodHeaderMaxHeight,
    stickyContentHeight: Dp = 0.dp,
    layoutInsetHeight: Dp = 0.dp,
    scalableContentIndex: Int,
    content: @Composable () -> Unit,
) {
    Layout(
        modifier = modifier,
        content = content,
        measurePolicy = { measurables, constraints ->
            val scalableContentMeasurable = measurables[scalableContentIndex]
            val otherMeasurables = measurables - scalableContentMeasurable
            val otherPlaceables = otherMeasurables.map {
                it.measure(constraints)
            }

            val heightOfOtherPlaceables = otherPlaceables.sumOf { it.height } + stickyContentHeight.roundToPx()
            val remainingHeight = height.roundToPx() - layoutInsetHeight.roundToPx() - heightOfOtherPlaceables
            val heightOfScalableContent = remainingHeight.coerceIn(minScalableHeight.roundToPx()..maxScalableHeight.roundToPx())

            val scalableContentPlaceable = scalableContentMeasurable.measure(
                constraints.copy(
                    minHeight = heightOfScalableContent,
                    maxHeight = heightOfScalableContent
                )
            )

            val placeables = otherPlaceables.toMutableList()
            placeables.add(scalableContentIndex, scalableContentPlaceable)
            val totalHeight = placeables.sumOf { it.height }
            layout(constraints.maxWidth, totalHeight) {
                var yPos = 0
                placeables.forEach {
                    it.placeRelative(x = 0, y = yPos)
                    yPos += it.height
                }
            }
        }
    )
}

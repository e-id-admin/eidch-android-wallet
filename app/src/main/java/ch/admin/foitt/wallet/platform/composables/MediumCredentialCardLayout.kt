package ch.admin.foitt.wallet.platform.composables

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.offset
import kotlin.math.max

/**
 * Credential layout that gets a preferred height to layout its content. If content fits in it, the text will be bottom aligned.
 * If content does not fit, the layout will have a height to show the text only.
 */
@Composable
internal fun MediumCredentialCardLayout(
    modifier: Modifier = Modifier,
    preferredHeight: Dp,
    padding: PaddingValues = PaddingValues(),
    icon: (@Composable () -> Unit)?,
    title: (@Composable () -> Unit)?,
    subtitle: (@Composable () -> Unit)?,
) {
    Layout(
        modifier = modifier,
        contents = listOf(
            icon ?: {},
            title ?: {},
            subtitle ?: {},
        ),
        measurePolicy = { measurables, constraints ->
            val (iconMeasurable, titleMeasurable, subtitleMeasurable) = measurables
            val verticalPadding = padding.calculateVerticalPadding()
            val constraintsWithPadding = constraints.offset(
                horizontal = -padding.calculateHorizontalPadding().roundToPx(),
                vertical = -verticalPadding.roundToPx(),
            )

            var iconPlaceable = iconMeasurable.firstOrNull()?.measure(constraintsWithPadding)
            val titlePlaceable = titleMeasurable.firstOrNull()?.measure(constraintsWithPadding)
            val subtitlePlaceable =
                subtitleMeasurable.firstOrNull()?.measure(constraintsWithPadding)
            val placeables = listOfNotNull(iconPlaceable, titlePlaceable, subtitlePlaceable)
            val contentHeight = placeables.sumOf { it.height } + verticalPadding.roundToPx()

            val actualHeight = if (contentHeight > preferredHeight.roundToPx()) {
                iconPlaceable = null // hide icon if there is not enough space
                listOfNotNull(titlePlaceable, subtitlePlaceable)
                    .sumOf { it.height } + verticalPadding.roundToPx()
            } else {
                preferredHeight.roundToPx()
            }
            layout(constraints.maxWidth, actualHeight) {
                var currentY = padding.calculateTopPadding().roundToPx()
                val contentX = padding.calculateLeftPadding(LayoutDirection.Ltr).roundToPx()
                iconPlaceable?.placeRelative(x = contentX, y = currentY)
                currentY += iconPlaceable?.height ?: 0

                currentY += max(0, actualHeight - contentHeight)
                titlePlaceable?.placeRelative(x = contentX, y = currentY)
                currentY += titlePlaceable?.height ?: 0

                subtitlePlaceable?.placeRelative(x = contentX, y = currentY)
                currentY += subtitlePlaceable?.height ?: 0
            }
        }
    )
}

internal fun PaddingValues.calculateHorizontalPadding() =
    calculateLeftPadding(LayoutDirection.Ltr) + calculateRightPadding(LayoutDirection.Ltr)

internal fun PaddingValues.calculateVerticalPadding() =
    calculateTopPadding() + calculateBottomPadding()

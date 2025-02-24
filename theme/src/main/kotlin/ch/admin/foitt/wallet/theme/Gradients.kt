package ch.admin.foitt.wallet.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

object Gradients {
    fun bottomFadingBrush(endColor: Color) = Brush.verticalGradient(
        0.0f to Color.Transparent,
        1.0f to endColor,
    )

    fun topFadingBrush(startColor: Color) = Brush.verticalGradient(
        0.0f to startColor,
        1.0f to Color.Transparent,
    )

    fun diagonalCredentialBrush() = Brush.linearGradient(
        0.0f to Color.Black.copy(alpha = 0.3f),
        1.0f to Color.Transparent
    )

    fun leftBottomRadialCredentialBrush(size: Size) = Brush.radialGradient(
        0.0f to Color.Black.copy(alpha = 0.3f),
        1.0f to Color.Transparent,
        center = Offset(x = 0.18f * size.width, y = 1.04f * size.height),
        radius = 0.5f * size.minDimension,
    )

    fun leftBottomRadialLargeCredentialBrush(size: Size) = Brush.radialGradient(
        0.0f to Color.Black.copy(alpha = 0.3f),
        1.0f to Color.Transparent,
        center = Offset(x = 0f, y = size.height),
        radius = 0.6f * size.width,
    )

    fun bottomCenterRadialCredentialBrush(size: Size) = Brush.radialGradient(
        0.0f to Color.Black.copy(alpha = 0.1f),
        1.0f to Color.Transparent,
        center = Offset(x = 0.55f * size.width, y = 0.9f * size.height),
        radius = 0.3f * size.minDimension,
    )
}

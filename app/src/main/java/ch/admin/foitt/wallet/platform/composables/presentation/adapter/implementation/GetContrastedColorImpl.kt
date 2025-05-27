package ch.admin.foitt.wallet.platform.composables.presentation.adapter.implementation

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import ch.admin.foitt.wallet.platform.composables.presentation.adapter.GetContrastedColor
import javax.inject.Inject

class GetContrastedColorImpl @Inject constructor() : GetContrastedColor {
    override fun invoke(
        backgroundColor: Color,
        backgroundOverlayColor: Color,
        darkContentColor: Color,
        lightContentColor: Color
    ): Color {
        val composedBackgroundColor = ColorUtils.compositeColors(
            backgroundOverlayColor.toArgb(),
            backgroundColor.toArgb(),
        )

        val contrastWithBlack = ColorUtils.calculateContrast(darkContentColor.toArgb(), composedBackgroundColor)
        val contrastWithWhite = ColorUtils.calculateContrast(lightContentColor.toArgb(), composedBackgroundColor)

        return if (contrastWithWhite > contrastWithBlack) lightContentColor else darkContentColor
    }
}

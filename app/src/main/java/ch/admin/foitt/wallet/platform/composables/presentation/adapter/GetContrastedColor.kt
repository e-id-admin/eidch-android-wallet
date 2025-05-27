package ch.admin.foitt.wallet.platform.composables.presentation.adapter

import androidx.compose.ui.graphics.Color
import ch.admin.foitt.wallet.theme.Gradients

interface GetContrastedColor {
    operator fun invoke(
        backgroundColor: Color,
        backgroundOverlayColor: Color = Color.Black.copy(alpha = Gradients.CREDENTIAL_GRADIENT_ALPHA_01),
        darkContentColor: Color = Color.Black,
        lightContentColor: Color = Color.White,
    ): Color
}

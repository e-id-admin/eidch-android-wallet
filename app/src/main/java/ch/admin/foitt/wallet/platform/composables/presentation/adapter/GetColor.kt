package ch.admin.foitt.wallet.platform.composables.presentation.adapter

import androidx.compose.ui.graphics.Color

fun interface GetColor {
    operator fun invoke(colorString: String?): Color?
}

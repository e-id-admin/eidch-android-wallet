package ch.admin.foitt.wallet.platform.credential.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import ch.admin.foitt.wallet.R

@Composable
fun DemoWatermark(
    modifier: Modifier = Modifier,
    color: Color,
    large: Boolean = true,
) {
    val drawableId = when {
        color == Color.Black && large -> R.drawable.wallet_ic_demo_black
        color == Color.Black && !large -> R.drawable.wallet_ic_demo_black_small
        color == Color.White && large -> R.drawable.wallet_ic_demo_white
        color == Color.White && !large -> R.drawable.wallet_ic_demo_white_small
        else -> R.drawable.wallet_ic_demo_white
    }
    BackgroundCanvas(
        modifier = modifier,
        drawableId = drawableId,
    )
}

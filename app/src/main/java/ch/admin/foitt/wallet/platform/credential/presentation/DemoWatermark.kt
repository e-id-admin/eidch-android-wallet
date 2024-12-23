package ch.admin.foitt.wallet.platform.credential.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import ch.admin.foitt.wallet.R

@Composable
fun DemoWatermark(
    modifier: Modifier = Modifier,
    color: Color,
    large: Boolean = true,
) {
    val context = LocalContext.current
    val resources = context.resources
    val drawableId = when {
        color == Color.Black && large -> R.drawable.wallet_ic_demo_black
        color == Color.Black && !large -> R.drawable.wallet_ic_demo_black_small
        color == Color.White && large -> R.drawable.wallet_ic_demo_white
        color == Color.White && !large -> R.drawable.wallet_ic_demo_white_small
        else -> R.drawable.wallet_ic_demo_white
    }
    val bitmap = ResourcesCompat.getDrawable(resources, drawableId, null)!!.toBitmap().asImageBitmap()
    Canvas(
        modifier = modifier
            .fillMaxSize()
    ) {
        val paint = Paint().asFrameworkPaint().apply {
            shader = ImageShader(bitmap, TileMode.Repeated, TileMode.Repeated)
        }
        drawIntoCanvas {
            translate(
                left = (this.size.width - bitmap.width) / 2,
                top = (this.size.height - bitmap.height) / 2,
            ) {
                it.nativeCanvas.drawPaint(paint)
            }
        }
        paint.reset()
    }
}

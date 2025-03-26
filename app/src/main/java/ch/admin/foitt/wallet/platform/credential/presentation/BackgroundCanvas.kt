package ch.admin.foitt.wallet.platform.credential.presentation

import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap

@Composable
fun BackgroundCanvas(
    modifier: Modifier = Modifier,
    @DrawableRes drawableId: Int,
) {
    val context = LocalContext.current
    val bitmap = AppCompatResources.getDrawable(context, drawableId)?.toBitmap()?.asImageBitmap()

    bitmap?.let {
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
}

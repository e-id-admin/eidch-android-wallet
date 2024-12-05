package ch.admin.foitt.wallet.platform.utils

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.core.graphics.drawable.toBitmap
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.getOr
import timber.log.Timber
import java.io.ByteArrayOutputStream

fun Drawable.toPainter(): Painter? = runSuspendCatching {
    when (this) {
        is BitmapDrawable -> BitmapPainter(this.toBitmap().asImageBitmap(), filterQuality = FilterQuality.High)
        else -> {
            Timber.e("Unsupported drawable")
            null
        }
    }
}.getOr(null)

fun Drawable.toBase64EncodedBitmap(): Result<String, Throwable> = runSuspendCatching {
    val bitmap = this.toBitmap()
    val byteArrayOutputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
    val imageByteArray = byteArrayOutputStream.toByteArray()
    imageByteArray.toNonUrlEncodedBase64String()
}

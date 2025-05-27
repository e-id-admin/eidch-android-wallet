package ch.admin.foitt.wallet.platform.utils

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import qrcode.QRCode

fun String.generateQRBitmap(): ImageBitmap = QRCode
    .ofSquares()
    .withSize(32)
    .build(this)
    .renderToBytes().let { bytes ->
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size).asImageBitmap()
    }

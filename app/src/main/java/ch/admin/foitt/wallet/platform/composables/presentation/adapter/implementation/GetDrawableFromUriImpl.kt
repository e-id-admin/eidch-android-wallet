package ch.admin.foitt.wallet.platform.composables.presentation.adapter.implementation

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import ch.admin.foitt.wallet.platform.composables.presentation.adapter.GetDrawableFromUri
import ch.admin.foitt.wallet.platform.utils.base64NonUrlStringToByteArray
import coil.ImageLoader
import coil.request.ImageRequest
import com.github.michaelbull.result.coroutines.runSuspendCatching
import com.github.michaelbull.result.getOrElse
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject

internal class GetDrawableFromUriImpl @Inject constructor(
    private val imageLoader: ImageLoader,
    @ApplicationContext private val appContext: Context,
) : GetDrawableFromUri {
    override suspend fun invoke(uriString: String?): Drawable? = runSuspendCatching {
        val imageUri = Uri.parse(uriString)
        val imageData = when (imageUri.scheme) {
            "data" -> imageUri.toString().substringAfter("base64,").base64NonUrlStringToByteArray()
            "https" -> null
            else -> {
                Timber.e("Unsupported image scheme: ${imageUri.scheme}")
                null
            }
        }
        imageData?.let {
            val imageRequest = ImageRequest.Builder(appContext)
                .data(imageData)
                .build()
            val result = imageLoader.execute(imageRequest)

            return result.drawable
        }
    }.getOrElse {
        Timber.w(message = "Failed getting Drawable from Url", t = it)
        null
    }
}

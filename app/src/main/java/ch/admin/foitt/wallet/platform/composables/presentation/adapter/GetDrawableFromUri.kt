package ch.admin.foitt.wallet.platform.composables.presentation.adapter

import android.graphics.drawable.Drawable

fun interface GetDrawableFromUri {
    suspend operator fun invoke(uriString: String?): Drawable?
}

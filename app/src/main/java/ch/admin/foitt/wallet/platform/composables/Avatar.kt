package ch.admin.foitt.wallet.platform.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun Avatar(imagePainter: Painter?, size: AvatarSize) {
    Box(
        modifier = Modifier
            .size(size.toDp())
            .clip(CircleShape)
            .background(WalletTheme.colorScheme.surfaceContainerHigh)
            .padding(size.internalPadding),
        contentAlignment = Alignment.Center,
    ) {
        imagePainter?.let {
            Image(
                painter = imagePainter,
                contentScale = ContentScale.Fit,
                contentDescription = null
            )
        }
    }
}

private fun AvatarSize.toDp() = when (this) {
    AvatarSize.SMALL -> Sizes.s08
    AvatarSize.MEDIUM -> Sizes.s10
    AvatarSize.LARGE -> Sizes.s14
}

private val AvatarSize.internalPadding get() = when (this) {
    AvatarSize.SMALL -> Sizes.s01
    AvatarSize.MEDIUM -> Sizes.s02
    AvatarSize.LARGE -> Sizes.s03
}

enum class AvatarSize {
    SMALL,
    MEDIUM,
    LARGE,
}

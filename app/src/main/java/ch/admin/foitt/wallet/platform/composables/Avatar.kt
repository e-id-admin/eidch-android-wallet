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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun Avatar(
    modifier: Modifier = Modifier,
    imagePainter: Painter?,
    size: AvatarSize,
    background: Color = WalletTheme.colorScheme.surfaceContainerHighest,
    imageTint: Color? = null,
) {
    Box(
        modifier = modifier
            .size(size.toDp())
            .clip(CircleShape)
            .background(background)
            .padding(size.internalPadding),
        contentAlignment = Alignment.Center,
    ) {
        imagePainter?.let {
            Image(
                modifier = Modifier.testTag("ISSUER_ICON"),
                painter = imagePainter,
                contentScale = ContentScale.Fit,
                contentDescription = null,
                colorFilter = imageTint?.let {
                    ColorFilter.tint(imageTint)
                }
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
    AvatarSize.SMALL -> 7.dp
    AvatarSize.MEDIUM -> 10.dp
    AvatarSize.LARGE -> 14.dp
}

enum class AvatarSize {
    SMALL,
    MEDIUM,
    LARGE,
}

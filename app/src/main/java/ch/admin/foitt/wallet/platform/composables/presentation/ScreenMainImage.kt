package ch.admin.foitt.wallet.platform.composables.presentation

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun ScreenMainImage(
    @DrawableRes iconRes: Int,
    @DrawableRes backgroundRes: Int,
    iconWidthFraction: Float = 0.4f,
    modifier: Modifier = Modifier
) = Box(
    modifier = modifier
        .clip(WalletTheme.shapes.extraLarge)
) {
    Image(
        modifier = Modifier
            .fillMaxWidth()
            // Will only apply for android 12+
            .blur(radius = Sizes.s05),
        painter = painterResource(id = backgroundRes),
        contentDescription = null,
        alignment = Alignment.Center,
        contentScale = ContentScale.Crop,
    )
    Image(
        painter = painterResource(id = iconRes),
        contentDescription = null,
        modifier = Modifier
            .align(Alignment.Center)
            .fillMaxWidth(iconWidthFraction)
    )
}

@Composable
fun ScreenMainImage(
    @DrawableRes iconRes: Int,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) = Box(
    modifier = modifier
        .fillMaxSize()
        .clip(WalletTheme.shapes.extraLarge)
        .background(backgroundColor),
) {
    Image(
        painter = painterResource(id = iconRes),
        contentDescription = null,
        modifier = Modifier
            .align(Alignment.Center)
            .fillMaxWidth(0.4f),
    )
}

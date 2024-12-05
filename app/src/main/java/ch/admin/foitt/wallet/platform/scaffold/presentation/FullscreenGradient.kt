package ch.admin.foitt.wallet.platform.scaffold.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import ch.admin.foitt.wallet.R

@Composable
fun FullscreenGradient() {
    Image(
        modifier = Modifier.fillMaxSize(),
        painter = painterResource(id = R.drawable.wallet_background_gradient_04),
        contentDescription = null,
        contentScale = ContentScale.Crop,
    )
}

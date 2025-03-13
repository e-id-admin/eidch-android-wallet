package ch.admin.foitt.wallet.platform.credential.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.theme.Sizes
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun CredentialCardVerySmallSquare() = Box(
    modifier = Modifier
        .size(Sizes.credentialVerySmallSquare)
        .clip(WalletTheme.shapes.small),
) {
    Image(
        modifier = Modifier
            .fillMaxWidth()
            // Will only apply for android 12+
            .blur(radius = Sizes.s02),
        painter = painterResource(id = R.drawable.wallet_background_gradient_05),
        contentDescription = null,
        alignment = Alignment.Center,
        contentScale = ContentScale.Crop,
    )
    Image(
        modifier = Modifier
            .size(Sizes.s04)
            .align(Alignment.Center),
        painter = painterResource(id = R.drawable.wallet_ic_swiss_cross),
        contentDescription = null
    )
}

@WalletComponentPreview
@Composable
private fun CredentialCardVerySmallSquarePreview() {
    WalletTheme {
        CredentialCardVerySmallSquare()
    }
}

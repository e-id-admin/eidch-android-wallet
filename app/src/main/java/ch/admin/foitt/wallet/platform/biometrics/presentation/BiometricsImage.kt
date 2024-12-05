package ch.admin.foitt.wallet.platform.biometrics.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import ch.admin.foitt.wallet.R
import ch.admin.foitt.wallet.platform.composables.presentation.ScreenMainImage
import ch.admin.foitt.wallet.platform.preview.WalletComponentPreview
import ch.admin.foitt.wallet.platform.utils.TestTags
import ch.admin.foitt.wallet.theme.WalletTheme

@Composable
fun BiometricsAvailableImage() = ScreenMainImage(
    iconRes = R.drawable.wallet_ic_biometrics,
    backgroundRes = R.drawable.wallet_background_gradient_04,
    iconWidthFraction = 0.8f,
    modifier = Modifier.testTag("biometricsAvailableImage")
)

@Composable
fun BiometricsUnavailableImage() = ScreenMainImage(
    iconRes = R.drawable.wallet_ic_cross_circle,
    backgroundRes = R.drawable.wallet_background_gradient_04,
    modifier = Modifier.testTag(TestTags.BIOMETRICS_UNAVAILABLE_ICON.name)
)

@WalletComponentPreview
@Composable
fun BiometricsAvailableImagePreview() {
    WalletTheme {
        BiometricsAvailableImage()
    }
}

@WalletComponentPreview
@Composable
fun BiometricsUnavailableImagePreview() {
    WalletTheme {
        BiometricsUnavailableImage()
    }
}
